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

    @Query("SELECT url FROM browsing_history WHERE type = 'HAI'")
    LiveData<List<String>> getBrowsingHistoryHair();

    @Query("SELECT url FROM browsing_history WHERE type = 'NAI'")
    LiveData<List<String>> getBrowsingHistoryNails();

    @Query("SELECT url FROM browsing_history WHERE type = 'MAK'")
    LiveData<List<String>> getBrowsingHistoryMakeup();

    @Query("SELECT url FROM browsing_history WHERE type = 'TAT'")
    LiveData<List<String>> getBrowsingHistoryTattoo();

    @Query("SELECT url FROM browsing_history WHERE type = 'BAR'")
    LiveData<List<String>> getBrowsingHistoryBarber();

    @Query("SELECT url FROM browsing_history WHERE type = 'FIT'")
    LiveData<List<String>> getBrowsingHistoryFitness();
}
