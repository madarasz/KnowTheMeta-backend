package com.madarasz.knowthemeta;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.madarasz.knowthemeta.meta.MetaOperations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

/**
 * Spring MVC Controller integration test
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpRequestIntegrationTests {
    @MockBean
    private MetaOperations operations;
    @MockBean
    private TimeStamper timeStamper;
    @MockBean
    private NetrunnerDBUpdater netrunnerDBUpdater;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testPageTitle() {
        assertTrue(this.restTemplate.getForObject("http://localhost:" + port + "/", String.class).contains("Admin page"));
    }

    @Test
    public void testNetrunnerDBUpdate() throws Exception {
        // setup
        doReturn((double)3).when(netrunnerDBUpdater).updateFromNetrunnerDB();
        // run and verify
        assertTrue(this.restTemplate.getForObject("http://localhost:" + port + "/load-netrunnerdb", String.class).contains("Updated from NetrunnerDB"));
        verify(netrunnerDBUpdater, times(1)).updateFromNetrunnerDB();
        verify(timeStamper, times(1)).setTimeStamp(anyString());
    }

    @Test
    public void testAddMeta() {
        // run and verify
        assertTrue(this.restTemplate.getForObject("http://localhost:" + port + "/add-meta?metaTitle=a&metaMWL=b&metaPack=c", String.class).contains("Meta added"));
        verify(operations, times(1)).addMeta(anyString(), anyString(), anyBoolean(), anyString());
    }

    @Test
    public void testDeleteMeta() {
        // run and verify
        assertTrue(this.restTemplate.getForObject("http://localhost:" + port + "/delete-meta?title=a", String.class).contains("Meta deleted"));
        verify(operations, times(1)).deleteMeta(anyString());
    }

    @Test
    public void testGetMeta() {
        // run and verify
        assertTrue(this.restTemplate.getForObject("http://localhost:" + port + "/get-meta?title=a", String.class).contains("Admin page"));
        verify(operations, times(1)).getMetaData(any());
    }
}