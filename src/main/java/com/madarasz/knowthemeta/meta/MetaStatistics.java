package com.madarasz.knowthemeta.meta;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.madarasz.knowthemeta.database.Entity;
import com.madarasz.knowthemeta.database.DOs.Card;
import com.madarasz.knowthemeta.database.DOs.Deck;
import com.madarasz.knowthemeta.database.DOs.Faction;
import com.madarasz.knowthemeta.database.DOs.Meta;
import com.madarasz.knowthemeta.database.DOs.Standing;
import com.madarasz.knowthemeta.database.DOs.relationships.CardInDeck;
import com.madarasz.knowthemeta.database.DOs.stats.DeckIdentity;
import com.madarasz.knowthemeta.database.DOs.stats.DeckStats;
import com.madarasz.knowthemeta.database.DOs.stats.WinRateUsedCounter;
import com.madarasz.knowthemeta.database.DRs.CardRepository;
import com.madarasz.knowthemeta.database.DRs.DeckIdentityRepository;
import com.madarasz.knowthemeta.database.DRs.DeckStatsRepository;
import com.madarasz.knowthemeta.database.DRs.MetaRepository;
import com.madarasz.knowthemeta.database.DRs.StandingRepository;
import com.madarasz.knowthemeta.database.DRs.WinRateUsedCounterRepository;
import com.madarasz.knowthemeta.helper.Searcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * Calculating meta statistics.
 */
@Service
public class MetaStatistics {
    @Autowired CardRepository cardRepository;
    @Autowired MetaRepository metaRepository;
    @Autowired StandingRepository standingRepository;
    @Autowired WinRateUsedCounterRepository winRateUsedCounterRepository;
    @Autowired DeckStatsRepository deckStatsRepository;
    @Autowired DeckIdentityRepository deckIdentityRepository;
    @Autowired Searcher searcher;
    @Autowired DeckDistance deckDistance;
    private static final Logger log = LoggerFactory.getLogger(MetaStatistics.class);
    private static final double minimumCardPopularity = 0.05; // cards won't be tagged under this popularity
    private static final double minimumAdditionalWinrate = 0.12; // cards wont't be tagged as winning if they do not get at least faction_winrate+additional
    private static final List<String> pseudoBreakers = new ArrayList<String>(Arrays.asList("Always Be Running", "Boomerang", "D4v1d", "e3 Feedback Implants", "Gbahali", 
        "Grappling Hook", "Kongamato"));


    public void calculateStats(String metaTitle) {
        log.info("Calculating statistics for meta " + metaTitle);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // gather data
        Meta meta = metaRepository.findByTitle(metaTitle);
        Set<Standing> standings = standingRepository.findByMeta(metaTitle); // all standings in the meta
        Set<Card> identities = standings.stream().map(x -> x.getIdentity()).filter(x -> !x.getFaction().getFactionCode().contains("neutral")).collect(Collectors.toSet());
        Set<Card> cardsInPack = cardRepository.listForPack(meta.getCardpool().getCode());
        Set<Card> cards = cardRepository.findNonIDByMeta(metaTitle);
        Set<Faction> factions = standings.stream().map(x -> x.getIdentity().getFaction()).collect(Collectors.toSet());
        factions = factions.stream().filter(x -> !x.getFactionCode().contains("neutral")).collect(Collectors.toSet()); // filter out neutral factions
        Set<WinRateUsedCounter> existingCardStats = winRateUsedCounterRepository.listCardStatsForMeta(metaTitle);
        Set<WinRateUsedCounter> existingFactionStats = winRateUsedCounterRepository.listFactionStatsForMeta(metaTitle);
        Set<DeckStats> existingDeckStats = deckStatsRepository.findByMetaTitle(metaTitle);
        Set<DeckIdentity> existingDeckIdentities = deckIdentityRepository.findByMetaTitle(metaTitle);

        // get side winrates
        final int runnerWins = standingRepository.countRunnerWinsInMeta(metaTitle);
        final int runnerLosses = standingRepository.countRunnerLossesInMeta(metaTitle);
        final int runnerDraws = standingRepository.countRunnerDrawsInMeta(metaTitle);
        final int allRunnerMatches = runnerWins + runnerLosses + runnerDraws;
        final double runnerWinrate = (double)runnerWins / allRunnerMatches;
        final double corpWinrate = (double)runnerLosses / allRunnerMatches;
        log.debug(String.format("Runner wins: %d, losses: %d, draws: %d", runnerWins, runnerLosses, runnerDraws));
        meta.setRunnerWinRate(runnerWinrate);
        meta.setCorpWinRate(corpWinrate);
        // get side winrates with decks
        final int runnerDeckWins = standingRepository.countRunnerWinsWithDeckInMeta(metaTitle);
        final int allRunnerDeckMatches = standingRepository.countRunnerAllWithDeckInMeta(metaTitle);
        final int corpDeckWins = standingRepository.countCorpWinsWithDeckInMeta(metaTitle);
        final int allCorpDeckMatches = standingRepository.countCorpAllWithDeckInMeta(metaTitle);
        final double runnerDeckWinrate = (double)runnerDeckWins / allRunnerDeckMatches;
        final double corpDeckWinrate = (double)corpDeckWins / allCorpDeckMatches;
        log.debug(String.format("Runner deck matches: %d, corp deck matches: %d", allRunnerDeckMatches, allCorpDeckMatches));
        log.debug(String.format("Runner deck winrate: %.3f, corp deck winrate: %.3f", runnerDeckWinrate, corpDeckWinrate));
        meta.setRunnerDeckWinRate(runnerDeckWinrate);
        meta.setCorpDeckWinRate(corpDeckWinrate);

        // iterate on factions
        log.debug("Factions found: " + factions.size());
        for (Faction faction : factions) {
            WinRateUsedCounter tempStat = factionStatsFromStandings(standings, faction, meta);
            WinRateUsedCounter factionStat = searcher.getStatsByFactionName(existingFactionStats, faction.getName());
            if (factionStat == null) {
                // new stat
                factionStat = tempStat;
            } else {
                // update existing stat
                factionStat.copyFrom(tempStat);
            }
            winRateUsedCounterRepository.save(factionStat);
        }
        
        // iterate on identities
        log.debug("IDs found: " + identities.size());
        for (Card identity : identities) {
            WinRateUsedCounter tempStat = idStatsFromStandings(standings, identity, meta);
            WinRateUsedCounter idStat = searcher.getStatsByCardTitle(existingCardStats, identity.getTitle());
            if (idStat == null) {
                // new stat
                idStat = tempStat;
            } else {
                // update existing stat
                idStat.copyFrom(tempStat);
            }
            winRateUsedCounterRepository.save(idStat);

            // get deckstats for the identity
            // the same deck can be listed multiple times if used by multiple players
            DeckIdentity deckIdentity = searcher.getDeckIdentityByIdentity(existingDeckIdentities, identity.getTitle());
            Set<Standing> standingsWithDecks = standingRepository.findWithDecksByMetaAndID(metaTitle, identity.getTitle());
            if (standingsWithDecks.size() > 0) {
                if (deckIdentity == null) {
                    deckIdentity = new DeckIdentity(identity, meta);
                }
                for (Standing standing : standingsWithDecks) {
                    double successScore = calculateDeckScore(standing);
                    DeckStats deckStat = new DeckStats(standing.getDeck(), successScore, standing.getRank(), standing.getTournament().getId());
                    deckStat.setRankSummary(calculateRankSummary(standing));
                    deckStat.setDeckSummary(calculateDeckSummary(standing.getDeck()));
                    DeckStats existing = searcher.getDeckStatsByDeckRankTournament(existingDeckStats, deckStat.getDeck().getId(), deckStat.getRank(), deckStat.getTournamentId());
                    if (existing == null) {
                        // new
                        deckStatsRepository.save(deckStat);
                    } else if (existing.getSuccessScore() != deckStat.getSuccessScore() || !existing.getDeckSummary().equals(deckStat.getDeckSummary())) {
                        // update
                        existing.setSuccessScore(deckStat.getSuccessScore());
                        existing.setDeckSummary(deckStat.getDeckSummary());
                        deckStatsRepository.save(existing);
                    }
                    DeckStats member = searcher.getDeckStatsByDeckRankTournament(deckIdentity.getDecks(), 
                        deckStat.getDeck().getId(), deckStat.getRank(), deckStat.getTournamentId());
                    if (member == null) {
                        deckIdentity.addDeck(deckStat);
                    }
                }
                deckIdentity.sortDecks();
                deckDistance.calculateDeckCoordinates(deckIdentity);
                deckIdentityRepository.save(deckIdentity);
            }
        }

        // iterate on non-ID cards
        final int runnerDeckCount = meta.getRunnerDecksCount();
        final int corpDeckCount = meta.getCorpDecksCount();
        log.debug(String.format("Cards found: %d / %d - decks: %d", cards.size(), cardRepository.count(), runnerDeckCount));
        for (Card card : cards) {
            calculateCardStats(metaTitle, meta, cardsInPack, existingCardStats, runnerWinrate, corpWinrate, runnerDeckCount,
                    corpDeckCount, card);
        }

        meta.setStatsCalculated(true);
        metaRepository.save(meta);
        stopWatch.stop();
        log.info(String.format("Meta statistics calculation finished (%.3f sec)", stopWatch.getTotalTimeSeconds()));
    }

    private String calculateDeckSummary(Deck deck) {
        String summary = "";
        String factionCode = deck.getIdentity().getFaction().getFactionCode();
        for (CardInDeck cardInDeck : deck.getCards()) {
            if (!cardInDeck.getCard().getFaction().getFactionCode().equals(factionCode) && cardInDeck.getCard().getFaction_cost() > 0) {
                summary += cardInDeck.getQuantity() + "x " + cardInDeck.getCard().getTitle() + ", ";
            }
        }
        if (summary.length() == 0) {
            log.error("Error calculating summary for deck #" + deck.getId());
            return "";
        }
        return summary.substring(0, summary.length() - 2);
    }

    private String calculateRankSummary(Standing standing) {
        return String.format("<a href=\"https://alwaysberunning.net/tournaments/%d\">%s</a> - rank: #%d / %d", 
            standing.getTournament().getId(), standing.getTournament().getTitle(),
            standing.getRank(), standing.getTournament().getPlayers_count());
    }

    public double calculateDeckScore(Standing standing) {
        int allMatches = standing.getDrawCount() + standing.getLossCount() + standing.getWinCount();
        if (allMatches == 0) return 1 / Math.cbrt((double)standing.getRank() / Math.sqrt(standing.getTournament().getPlayers_count()));
        return (double)standing.getWinCount() / Math.sqrt(allMatches) / Math.cbrt((double)standing.getRank() / Math.sqrt(standing.getTournament().getPlayers_count()));
    }

    public WinRateUsedCounter calculateCardStats(Meta meta, Card card) {
        Set<WinRateUsedCounter> existingCardStats = winRateUsedCounterRepository.listCardStatsForMeta(meta.getTitle());
        WinRateUsedCounter cardStat = searcher.getStatsByCardTitle(existingCardStats, card.getTitle());
        if (cardStat != null) {
            return cardStat;
        }

        Set<Card> cardsInPack = cardRepository.listForPack(meta.getCardpool().getCode());
        return this.calculateCardStats(meta.getTitle(), meta, cardsInPack, existingCardStats, meta.getRunnerWinRate(), meta.getCorpWinRate(), 
            meta.getRunnerDecksCount(), meta.getCorpDecksCount(), card);
    }

    private WinRateUsedCounter calculateCardStats(String metaTitle, Meta meta, Set<Card> cardsInPack,
            Set<WinRateUsedCounter> existingCardStats, final double runnerWinrate, final double corpWinrate,
            final int runnerDeckCount, final int corpDeckCount, Card card) {
        final String cardTitle = card.getTitle();
        final Set<Standing> cardStandings = standingRepository.findByMetaAndCard(metaTitle, cardTitle);
        
        int winCount = 0;
        int drawCount = 0;
        int lossCount = 0;
        int perDeckCount = 0;
        final int deckCount = card.getSide_code().equals("runner") ? runnerDeckCount : corpDeckCount;
        final double factionWinRate = card.getSide_code().equals("runner") ? runnerWinrate : corpWinrate;
        List<String> tags = new ArrayList<String>();
        for (Standing standing : cardStandings) {
            winCount += standing.getWinCount();
            drawCount += standing.getDrawCount();
            lossCount += standing.getLossCount();
            perDeckCount += standing.getDeck().getCards().stream()
                .filter(x -> x.getCard().getTitle().equals(cardTitle)).findFirst().get().getQuantity();
        }
        final float popularity = ((float)cardStandings.size()) / deckCount;
        final double winrate = (double)winCount/(winCount+drawCount+lossCount)*100.0;
        // add tags
        if (popularity > minimumCardPopularity) {
            // popular in pack
            if (cardsInPack.stream().filter(x -> x.getTitle().equals(cardTitle)).findFirst().isPresent()) {
                tags.add("popular-in-pack");
            }
            // winning
            if (winrate > (factionWinRate + minimumAdditionalWinrate) * 100) {
                tags.add("winning");
            }
            // icebreaker
            if (card.getKeywords() != null && card.getKeywords().contains("Icebreaker")) {
                String subtype = card.getKeywords().split(" - ").length > 1 ? card.getKeywords().split(" - ")[1] : card.getKeywords();
                tags.add("icebreaker-" + subtype);
            }
            // pseudo icebreaker
            if (pseudoBreakers.stream().anyMatch(cardTitle::equals)) {
                tags.add("icebreaker-" + card.getType_code());
            }
            // ICE
            if (card.getType_code().equals("ice")) {
                String subtype = card.getKeywords() == null ? "" : "-" + card.getKeywords().split(" - ")[0];
                tags.add("ice" + subtype);
            }
        }
        
        // save/update
        WinRateUsedCounter cardStat = searcher.getStatsByCardTitle(existingCardStats, cardTitle);
        if (cardStat == null) {
            cardStat = new WinRateUsedCounter(meta, card);
        }
        cardStat.setAvgPerDeck((float)perDeckCount / cardStandings.size());
        cardStat.setTags(String.join(",", tags));
        cardStat.setUsedCounter(cardStandings.size());
        cardStat.setWinCounter(winCount);
        cardStat.setDrawCounter(drawCount);
        cardStat.setLossCounter(lossCount);
        winRateUsedCounterRepository.save(cardStat);
        return cardStat;
    }

    private WinRateUsedCounter idStatsFromStandings(Set<Standing> standings, Card card, Meta meta) {
        String cardTitle = card.getTitle();
        Set<Standing> filteredSet = standings.stream().filter(x -> x.getIdentity().getTitle().equals(cardTitle)).collect(Collectors.toSet());
        return statsFromStandings(card, meta, filteredSet);
    }

    private WinRateUsedCounter factionStatsFromStandings(Set<Standing> standings, Faction faction, Meta meta) {
        String factionName = faction.getName();
        Set<Standing> filteredSet = standings.stream().filter(x -> x.getIdentity().getFaction().getName().equals(factionName)).collect(Collectors.toSet());
        return statsFromStandings(faction, meta, filteredSet);
    }

    private WinRateUsedCounter statsFromStandings(Entity entity, Meta meta, Set<Standing> filteredSet) {
        int winCounter = filteredSet.stream().map(x -> x.getWinCount()).reduce(0, Integer::sum);
        int drawCounter = filteredSet.stream().map(x -> x.getDrawCount()).reduce(0, Integer::sum);
        int lossCounter = filteredSet.stream().map(x -> x.getLossCount()).reduce(0, Integer::sum);
        int usedCounter = filteredSet.size();
        return new WinRateUsedCounter(winCounter, drawCounter, lossCounter, usedCounter, meta, entity);
    }
}