package com.madarasz.knowthemeta.database.DOs.admin;

import java.util.Date;

import com.madarasz.knowthemeta.database.Entity;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.DateString;

@NodeEntity
public class AdminStamp extends Entity {
    @Index(unique = true) private String entry;
    @DateString("yyyy-MM-dd HH:mm:ss") private Date timestamp;

    public AdminStamp() {
    }

    public AdminStamp(String entry, Date timestamp) {
        this.entry = entry;
        this.timestamp = timestamp;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    
}