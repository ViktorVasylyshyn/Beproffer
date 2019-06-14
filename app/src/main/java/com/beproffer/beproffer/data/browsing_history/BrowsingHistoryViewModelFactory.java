package com.beproffer.beproffer.data.browsing_history;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class BrowsingHistoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application application;
    private String targetType;

    public BrowsingHistoryViewModelFactory(Application application, String targetType){
        this.application = application;
        this.targetType = targetType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new BrowsingHistoryViewModel(application, targetType);
    }
}
