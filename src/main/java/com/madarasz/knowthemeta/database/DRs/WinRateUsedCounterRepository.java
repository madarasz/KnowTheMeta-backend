package com.madarasz.knowthemeta.database.DRs;

import java.util.List;
import java.util.Set;

import com.madarasz.knowthemeta.database.DOs.stats.WinRateUsedCounter;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.CrudRepository;

public interface WinRateUsedCounterRepository extends CrudRepository<WinRateUsedCounter, Long> {
    @Query("MATCH (c:Card {type_code:'identity'})-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) RETURN r,r2,m,w,c")
    Set<WinRateUsedCounter> listIDStatsForMeta(String metaTitle);

    @Query("MATCH (c:Card)-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) RETURN r,r2,m,w,c")
    Set<WinRateUsedCounter> listCardStatsForMeta(String metaTitle);

    @Query("MATCH (f:Faction)-[r3:FACTION]-(c:Card)-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) WHERE c.type_code<>'identity' AND SIZE(w.tags)>0 RETURN r,r2,r3,f,m,w,c")
    Set<WinRateUsedCounter> listNonIDCardStatsForMeta(String metaTitle);

    @Query("MATCH (f:Faction)-[r3:FACTION]-(c:Card {type_code:'identity'})-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) RETURN r,r2,r3,f,m,w,c ORDER BY w.usedCounter DESC")
    List<WinRateUsedCounter> listIDStatsForMetaOrdered(String metaTitle);

    @Query("MATCH (f:Faction)-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) RETURN r,r2,m,w,f")
    Set<WinRateUsedCounter> listFactionStatsForMeta(String metaTitle);

    @Query("MATCH (f:Faction)-[r:STAT_ABOUT]-(w:WinRateUsedCounter)-[r2:META]-(m:Meta {title:$0}) RETURN r,r2,m,w,f ORDER BY w.usedCounter DESC")
    List<WinRateUsedCounter> listFactionStatsForMetaOrdered(String metaTitle);

    @Query("MATCH (:Card {type_code:'identity'})-[:STAT_ABOUT]-(w:WinRateUsedCounter) RETURN COUNT(w)")
    int countIDStats();

    @Query("MATCH (c:Card)-[:STAT_ABOUT]-(w:WinRateUsedCounter) WHERE c.type_code <> 'identity' RETURN COUNT(w)")
    int countCardStats();

    @Query("MATCH (:Faction)-[:STAT_ABOUT]-(w:WinRateUsedCounter) RETURN COUNT(w)")
    int countFactionStats();
}