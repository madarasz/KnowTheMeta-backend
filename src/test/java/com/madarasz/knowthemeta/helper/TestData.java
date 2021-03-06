package com.madarasz.knowthemeta.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.madarasz.knowthemeta.database.DOs.Card;
import com.madarasz.knowthemeta.database.DOs.CardCycle;
import com.madarasz.knowthemeta.database.DOs.CardPack;
import com.madarasz.knowthemeta.database.DOs.Deck;
import com.madarasz.knowthemeta.database.DOs.Faction;
import com.madarasz.knowthemeta.database.DOs.MWL;
import com.madarasz.knowthemeta.database.DOs.Meta;
import com.madarasz.knowthemeta.database.DOs.Standing;
import com.madarasz.knowthemeta.database.DOs.Tournament;
import com.madarasz.knowthemeta.database.DOs.User;
import com.madarasz.knowthemeta.database.DOs.relationships.CardInDeck;
import com.madarasz.knowthemeta.database.DOs.relationships.CardInPack;
import com.madarasz.knowthemeta.database.DOs.relationships.MWLCard;

public class TestData {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public CardCycle testCycle1;
    public CardCycle testCycle2;
    public CardPack testPack1;
    public CardPack testPack2;
    public CardPack testPack3;
    public Card testCard1;
    public Card testCard2;
    public CardInPack testCardInPack1;
    public CardInPack testCardInPack2;
    public CardInPack testCardInPack3;
    public Set<CardCycle> cycleTestSet = new HashSet<CardCycle>();
    public Set<CardPack> packTestSet = new HashSet<CardPack>();
    public List<CardInPack> cardInPackTestSet = new ArrayList<CardInPack>();
    public MWL testMwl;
    public MWLCard testMWLCard;
    public Set<MWL> mwlTestSet = new HashSet<MWL>();
    public Meta testMeta;
    public Tournament testTournament;
    public List<Tournament> testTournamentList = new ArrayList<Tournament>();
    public List<Standing> testStandingsFromABR = new ArrayList<Standing>();
    public User testPlayer;
    public Deck testDeck;
    public Standing testStanding1;
    public Standing testStanding2;
    public Standing testStanding3;
    public List<Standing> testStandingSet = new ArrayList<Standing>();
    public Faction testFaction;

    public TestData() {
        testCycle1 = new CardCycle("test", "First Test Cycle", 9998, false);
        testCycle2 = new CardCycle("test_reprint", "Test Reprint Cycle", 9999, false);
        try {
            testPack1 = new CardPack("first", "The beginning", 1, testCycle1, dateFormat.parse("2020-01-01"));
            testPack2 = new CardPack("second", "Meh", 2, testCycle1, dateFormat.parse("2020-03-15"));
            testPack3 = new CardPack("third", "Reprinto", 1, testCycle2, dateFormat.parse("2020-05-03"));
            testMwl = new MWL("test_MWL", "First MWL", true, dateFormat.parse("2020-06-06"), 999);
            testTournament = new Tournament(666999, "Test Tournament", dateFormat.parse("2019-12-12"), 6, 0, true);
            testDeck = new Deck(9999991, "Test deck", dateFormat.parse("2019-12-25"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        testFaction = new Faction("test-anarch", "Test Anarch", true);
        testCard1 = new Card(2, 3, testFaction, 2, 0, 0, 1, 2, 0, 0, 0, "Icebreaker - Fracter", "runner", "1[credit]: Break <strong>barrier</strong> subroutine.", "Test Fracter", "program", false, 0);
        testCard2 = new Card(0, 1, testFaction, 0, 15, 45, 0, 0, 0, 0, 0, "G-mod", "runner", "Whenever you install a <strong>virus</strong> program, the Corp trashes the top card of R&D.", "Test Anarch", "identity", false, 0);
        testCardInPack1 = new CardInPack(testCard1, testPack1, "66661", "https://netrunnerdb.com/card_image/66661.png");
        testCardInPack2 = new CardInPack(testCard2, testPack2, "66662", "http://www.cardgamedb.com/forums/uploads/an/med_ADN17_21.png");
        testCardInPack3 = new CardInPack(testCard1, testPack3, "99991", "https://netrunnerdb.com/card_image/99991.png");
        testMWLCard = new MWLCard(testMwl, testCard1, false, 0, true, false);
        testMeta = new Meta(testPack3, testMwl, true, "Test meta");
        testPlayer = new User(111222, "Test player");
        testDeck.setPlayer(testPlayer);
        testDeck.setIdentity(testCard2);
        testDeck.addCard(new CardInDeck(testDeck, testCard1, 2));
        testStanding1 = new Standing(testTournament, testCard2, testDeck, 1, true);
        testStanding2 = new Standing(testTournament, testCard2, 1, false); // TODO: corp test identity
        testStanding3 = new Standing(testTournament, testCard2, testDeck, 2, true);
        testStandingSet.add(testStanding1);
        testStandingSet.add(testStanding2);
        testStandingSet.add(testStanding3);
        testMwl.addCard(testMWLCard);
        cycleTestSet.add(testCycle1);
        cycleTestSet.add(testCycle2);
        packTestSet.add(testPack1);
        packTestSet.add(testPack2);
        packTestSet.add(testPack3);
        cardInPackTestSet.add(testCardInPack1);
        cardInPackTestSet.add(testCardInPack2);
        cardInPackTestSet.add(testCardInPack3);
        mwlTestSet.add(testMwl);
        testTournamentList.add(testTournament);
    }

    
}