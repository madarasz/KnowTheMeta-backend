package com.madarasz.knowthemeta.brokers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.madarasz.knowthemeta.database.DOs.Card;
import com.madarasz.knowthemeta.database.DOs.Deck;
import com.madarasz.knowthemeta.database.DOs.Meta;
import com.madarasz.knowthemeta.database.DOs.Standing;
import com.madarasz.knowthemeta.database.DOs.Tournament;
import com.madarasz.knowthemeta.database.DRs.queryresult.CardCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ABRBroker {

    @Autowired HttpBroker httpBroker;
    @Autowired NetrunnerDBBroker netrunnerDBBroker;
    private final static String ABR_TOURNAMENT_API_URL = 
        "https://alwaysberunning.net/api/tournaments?concluded=1&approved=1&format=standard&cardpool={CARDPOOL_CODE}&mwl_id={MWL_ID}";
    private final static String ABR_STANDING_API_URL = "https://alwaysberunning.net/api/entries?id=";
    private final static Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy.MM.dd.").create();
    private final static Logger log = LoggerFactory.getLogger(ABRBroker.class);
    private final static Pattern deckUrlPattern = Pattern.compile("https://netrunnerdb.com/en/decklist/(\\d+)");

    public List<Tournament> getTournamentData(Meta meta) {
        log.info("Getting ABR tournament data for meta: " + meta.getTitle());
        JsonArray tournamentData = httpBroker.readJSONFromURL(ABR_TOURNAMENT_API_URL
            .replaceAll("\\{CARDPOOL_CODE\\}", meta.getCardpool().getCode()).replaceAll("\\{MWL_ID\\}", Integer.toString(meta.getMwl().getId()))).getAsJsonArray();
        Type collectionType = new TypeToken<List<Tournament>>(){}.getType();
        List<Tournament> result = gson.fromJson(tournamentData.toString(), collectionType);
        for (Tournament tournament : result) {
            tournament.setMeta(meta);
        }
        return result;
    }

    public List<Standing> getStadingData(Tournament tournament, List<CardCode> identities, List<Deck> existingDecks) {
        List<Standing> result = new ArrayList<Standing>();
        JsonArray standingData = httpBroker.readJSONFromURL(ABR_STANDING_API_URL+tournament.getId()).getAsJsonArray();

        // iterate on items
        standingData.forEach(item -> {
            JsonObject stadingItem = (JsonObject) item;
            int rank = (stadingItem.get("rank_top").isJsonNull()) ? stadingItem.get("rank_swiss").getAsInt() : stadingItem.get("rank_top").getAsInt();
            String runnerId = stadingItem.get("runner_deck_identity_id").getAsString();
            String corpId = stadingItem.get("corp_deck_identity_id").getAsString();
            Card runner = identities.stream().filter(id -> id.getCode().equals(runnerId)).findFirst().get().getCard();
            Card corp = identities.stream().filter(id -> id.getCode().equals(corpId)).findFirst().get().getCard();
            String runnerDeckUrl = stadingItem.get("runner_deck_url").getAsString();
            String corpDeckUrl = stadingItem.get("corp_deck_url").getAsString();
            if (runnerDeckUrl.length() > 0) {
                Deck runnerDeck = netrunnerDBBroker.loadDeck(this.deckIdFromUrl(runnerDeckUrl), existingDecks);
                result.add(new Standing(tournament, runner, runnerDeck, rank, true));
            } else {
                result.add(new Standing(tournament, runner, rank, true));
            } 
            if (corpDeckUrl.length() > 0) {
                Deck corpDeck = netrunnerDBBroker.loadDeck(this.deckIdFromUrl(corpDeckUrl), existingDecks);
                result.add(new Standing(tournament, corp, corpDeck, rank, false));
            } else {
                result.add(new Standing(tournament, corp, rank, false));
            }     
        });
        return result;
    }

    private int deckIdFromUrl(String url) {
        Matcher matcher = deckUrlPattern.matcher(url);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            log.error("Not suitable decklist URL:" + url);
            throw new IllegalArgumentException();
        }
    }
}