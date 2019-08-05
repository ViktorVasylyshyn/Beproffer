package com.beproffer.beproffer.data.browsing_history;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "browsing_history")

public class BrowsingHistoryModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private final String url;

    private final String type;

    public BrowsingHistoryModel(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}
