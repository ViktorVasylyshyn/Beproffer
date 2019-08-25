package com.beproffer.beproffer.data.browsing_history;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BrowsingHistoryDao {

    @Insert
    void insert(BrowsingHistoryModel model);

    @Query("DELETE FROM browsing_history")
    void deleteWholeBrowsingHistory();

    @Query("SELECT url FROM browsing_history")
    LiveData<List<String>> getBrowsingHistory();
}
