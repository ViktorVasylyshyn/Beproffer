package com.beproffer.beproffer.presentation.browsing;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.data.firebase.SwipeImagesRepository;
import com.beproffer.beproffer.data.models.BrowsingItem;

import java.util.List;

public class BrowsingViewModel extends AndroidViewModel {

    private final SwipeImagesRepository mRepository;

    private LiveData<List<BrowsingItem>> mSwipeImageItemsList;

    public BrowsingViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SwipeImagesRepository(application);
        if(mSwipeImageItemsList == null)
            mSwipeImageItemsList = mRepository.getSwipeImageItemsListLiveData();

    }

    public LiveData<List<BrowsingItem>> getSwipeImageItemsList() {
        return mSwipeImageItemsList;
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Integer> getShowToast() {
        return mRepository.getShowToast();
    }

    public LiveData<Boolean> getPerformSearch() {
        return mRepository.getPerformSearch();
    }

    public void deleteObservedImageItem(BrowsingItem item) {
        mRepository.deleteObservedImageItem(item);
    }

    public void refreshAdapter(){
        mRepository.refreshAdapter();
    }

    public void clearBrowsingHistory(){
        mRepository.clearBrowsingHistory();
    }

    public void resetTriggers(@Nullable Boolean resetToastValue, @Nullable Boolean resetPerformSearch) {
        /*параметр может быть или null(не трогать) или true(обнулить значение)*/
        mRepository.resetTriggers(resetToastValue, resetPerformSearch);
    }
}
