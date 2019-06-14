package com.beproffer.beproffer.data.browsing_history;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class BrowsingHistoryViewModel extends AndroidViewModel {
    private BrowsingHistoryRepository repository;
    private LiveData<List<String>> targetBrowsingHistory;

    public BrowsingHistoryViewModel(@NonNull Application application, String targetType) {
        super(application);
        repository = new BrowsingHistoryRepository(application, targetType);
        targetBrowsingHistory = repository.getTargetBrowsingHistory();
    }

    public void insert(BrowsingHistoryModel model){
        repository.insert(model);
    }

    public void deletoWholeBrowsingHistory(){
        repository.deleteWholeBrowsingHistory();
    }

    public LiveData<List<String>> getTargetBrowsingHistory(){
        return targetBrowsingHistory;
    }
}
