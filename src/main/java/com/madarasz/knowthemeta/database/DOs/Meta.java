package com.madarasz.knowthemeta.database.DOs;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.madarasz.knowthemeta.database.Entity;
import com.madarasz.knowthemeta.database.serializer.MetaSerializer;

import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.DateString;

@NodeEntity
@JsonSerialize(using = MetaSerializer.class)
public class Meta extends Entity {
    private CardPack cardpool;
    private MWL mwl;
    private Boolean newCards;
    private Boolean statsCalculated = false;
    private String title;
    @DateString("yyyy-MM-dd HH:mm:ss") @Nullable Date lastUpdate;
    private int tournamentCount = 0;
    private int runnerDecksCount = 0;
    private int corpDecksCount = 0;
    private int matchesCount = 0;
    private int standingsCount = 0;
    private double runnerWinRate;
    private double corpWinRate;
    private double runnerDeckWinRate;
    private double corpDeckWinRate;

    public Meta(){
    }

    public Meta(CardPack cardpool, MWL mwl, Boolean newCards, String title) {
        this.cardpool = cardpool;
        this.mwl = mwl;
        this.newCards = newCards;
        this.title = title;
    }

    public CardPack getCardpool() {
        return cardpool;
    }

    public void setCardpool(CardPack cardpool) {
        this.cardpool = cardpool;
    }

    public MWL getMwl() {
        return mwl;
    }

    public void setMwl(MWL mwl) {
        this.mwl = mwl;
    }

    public Boolean getNewCards() {
        return newCards;
    }

    public void setNewCards(Boolean newCards) {
        this.newCards = newCards;
    }

    public Boolean getStatsCalculated() {
        return statsCalculated;
    }

    public void setStatsCalculated(Boolean statsCalculated) {
        this.statsCalculated = statsCalculated;
    }

    public int getTournamentCount() {
        return tournamentCount;
    }

    public void setTournamentCount(int tournamentCount) {
        this.tournamentCount = tournamentCount;
    }

    public int getRunnerDecksCount() {
        return runnerDecksCount;
    }

    public void setRunnerDecksCount(int runnerDecksCount) {
        this.runnerDecksCount = runnerDecksCount;
    }

    public int getCorpDecksCount() {
        return corpDecksCount;
    }

    public void setCorpDecksCount(int corpDecksCount) {
        this.corpDecksCount = corpDecksCount;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public void setMatchesCount(int matchesCount) {
        this.matchesCount = matchesCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getStandingsCount() {
        return standingsCount;
    }

    public void setStandingsCount(int standingsCount) {
        this.standingsCount = standingsCount;
    }

    @Override
    public String toString() {
        return "Meta [cardpool=" + cardpool.getName() +  ", mwl=" + mwl.getName() + ", title=" + title + ", new cards=" + newCards + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cardpool == null) ? 0 : cardpool.getName().hashCode());
        result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
        result = prime * result + ((mwl == null) ? 0 : mwl.getName().hashCode());
        result = prime * result + ((newCards == null) ? 0 : newCards.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Meta other = (Meta) obj;
        if (cardpool == null) {
            if (other.cardpool != null)
                return false;
        } else if (!cardpool.getName().equals(other.cardpool.getName()))
            return false;
        if (lastUpdate == null) {
            if (other.lastUpdate != null)
                return false;
        } else if (!lastUpdate.equals(other.lastUpdate))
            return false;
        if (mwl == null) {
            if (other.mwl != null)
                return false;
        } else if (!mwl.getName().equals(other.mwl.getName()))
            return false;
        if (newCards == null) {
            if (other.newCards != null)
                return false;
        } else if (!newCards.equals(other.newCards))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }

    public double getRunnerWinRate() {
        return runnerWinRate;
    }

    public void setRunnerWinRate(double runnerWinRate) {
        this.runnerWinRate = runnerWinRate;
    }

    public double getCorpWinRate() {
        return corpWinRate;
    }

    public void setCorpWinRate(double corpWinRate) {
        this.corpWinRate = corpWinRate;
    }

    public double getRunnerDeckWinRate() {
        return runnerDeckWinRate;
    }

    public void setRunnerDeckWinRate(double runnerDeckWinRate) {
        this.runnerDeckWinRate = runnerDeckWinRate;
    }

    public double getCorpDeckWinRate() {
        return corpDeckWinRate;
    }

    public void setCorpDeckWinRate(double corpDeckWinRate) {
        this.corpDeckWinRate = corpDeckWinRate;
    }

}