package com.madarasz.knowthemeta.helper;

import java.util.Collection;
import java.util.Optional;

import com.madarasz.knowthemeta.database.DOs.Card;
import com.madarasz.knowthemeta.database.DOs.CardCycle;
import com.madarasz.knowthemeta.database.DOs.CardPack;
import com.madarasz.knowthemeta.database.DOs.Deck;
import com.madarasz.knowthemeta.database.DOs.Faction;
import com.madarasz.knowthemeta.database.DOs.MWL;
import com.madarasz.knowthemeta.database.DOs.Standing;
import com.madarasz.knowthemeta.database.DOs.User;
import com.madarasz.knowthemeta.database.DOs.relationships.CardInPack;
import com.madarasz.knowthemeta.database.DOs.stats.DeckIdentity;
import com.madarasz.knowthemeta.database.DOs.stats.DeckStats;
import com.madarasz.knowthemeta.database.DOs.stats.WinRateUsedCounter;

import org.springframework.stereotype.Service;

/**
 * Filters collections of KnowTheMeta entities.
 * Returns first match. Returns null if no match exists.
 */
@Service
public class Searcher {

    public Card getCardByCode(Collection<CardInPack> searchFrom, String cardCode) {
        Optional<CardInPack> result = searchFrom.stream().filter(x -> x.getCode().equals(cardCode)).findFirst();
        if (result.isPresent()) {
            return result.get().getCard();
        } 
        return null;
    }

    public CardInPack getCardInPackByTitle(Collection<CardInPack> searchFrom, String cardTitle) {
        Optional<CardInPack> result = searchFrom.stream().filter(x -> x.getCard().getTitle().equals(cardTitle)).findFirst();
        return safeGet(result);
    }

    public Standing getStadingByRankSide(Collection<Standing> searchFrom, int rank, Boolean isRunner) {
        Optional<Standing> result = searchFrom.stream().filter(x -> x.getRank() == rank && x.getIsRunner() == isRunner).findFirst();
        return safeGet(result);
    }

    public long countStadingsByRankSide(Collection<Standing> searchFrom, int rank, Boolean isRunner) {
        return searchFrom.stream().filter(x -> x.getRank() == rank && x.getIsRunner() == isRunner).count();
    }

    public Deck getDeckById(Collection<Deck> searchFrom, int NetrunnerDBId) {
        Optional<Deck> result = searchFrom.stream().filter(x -> x.getId() == NetrunnerDBId).findFirst();
        return safeGet(result);
    }

    public User getUserById(Collection<User> searchFrom, int NetrunnerDBId) {
        Optional<User> result = searchFrom.stream().filter(x -> x.getUser_id() == NetrunnerDBId).findFirst();
        return safeGet(result);
    }

    public MWL getMWLByCode(Collection<MWL> searchFrom, String mwlCode) {
        Optional<MWL> result = searchFrom.stream().filter(x -> x.getCode().equals(mwlCode)).findFirst();
        return safeGet(result);
    }

    public CardCycle getCycleByCode(Collection<CardCycle> searchFrom, String cycleCode) {
        Optional<CardCycle> result = searchFrom.stream().filter(x -> x.getCode().equals(cycleCode)).findFirst();
        return safeGet(result);
    }

    public CardPack getPackByCode(Collection<CardPack> searchFrom, String packCode) {
        Optional<CardPack> result = searchFrom.stream().filter(x -> x.getCode().equals(packCode)).findFirst();
        return safeGet(result);
    }

    public Faction getFactionByCode(Collection<Faction> searchFrom, String factionCode) {
        Optional<Faction> result = searchFrom.stream().filter(x -> x.getFactionCode().equals(factionCode)).findFirst();
        return safeGet(result);
    }

    public WinRateUsedCounter getStatsByCardTitle(Collection<WinRateUsedCounter> searchFrom, String cardTitle) {
        Optional<WinRateUsedCounter> result = searchFrom.stream().filter(x -> ((Card)x.getStatAbout()).getTitle().equals(cardTitle)).findFirst();
        return safeGet(result);
    }

    public WinRateUsedCounter getStatsByFactionName(Collection<WinRateUsedCounter> searchFrom, String factionName) {
        Optional<WinRateUsedCounter> result = searchFrom.stream().filter(x -> ((Faction)x.getStatAbout()).getName().equals(factionName)).findFirst();
        return safeGet(result);
    }

    public DeckStats getDeckStatsByDeckRankTournament(Collection<DeckStats> searchFrom, int deckId, int rank, int tournamentId) {
        Optional<DeckStats> result = searchFrom.stream().filter(x -> x.getDeck().getId() == deckId && x.getRank() == rank && x.getTournamentId() == tournamentId)
            .findFirst();
        return safeGet(result);
    }

    public DeckIdentity getDeckIdentityByIdentity(Collection<DeckIdentity> searchFrom, String identityTitle) {
        Optional<DeckIdentity> result = searchFrom.stream().filter(x -> x.getIdentity().getTitle().equals(identityTitle)).findFirst();
        return safeGet(result);
    }

    private <T> T safeGet(Optional<T> result) {
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }
}