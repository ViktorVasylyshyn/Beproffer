package com.beproffer.beproffer.data.browsing_history;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "browsing_history")

public class BrowsingHistoryModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private final String url;

    public BrowsingHistoryModel(String url) {
        this.url = url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
