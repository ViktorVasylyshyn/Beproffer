package com.beproffer.beproffer.data.browsing_history;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
