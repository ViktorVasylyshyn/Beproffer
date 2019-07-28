package com.beproffer.beproffer.presentation.swimg;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.data.firebase.SwipeImagesRepository;
import com.beproffer.beproffer.data.models.SwipeImageItem;

import java.util.List;

public class SwipeImagesViewModel extends AndroidViewModel {

    private SwipeImagesRepository mRepository;

    private LiveData<List<SwipeImageItem>> mSwipeImageItemsList;

    public SwipeImagesViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SwipeImagesRepository(application);
        if(mSwipeImageItemsList == null)
            mSwipeImageItemsList = mRepository.getSwipeImageItemsListLiveData();

    }

    public LiveData<List<SwipeImageItem>> getSwipeImageItemsList() {
        return mSwipeImageItemsList;
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Integer> getShowToast() {
        return mRepository.getShowToast();
    }

    public LiveData<Boolean> getPerformNavigation() {
        return mRepository.getPerformSearch();
    }

    public LiveData<Boolean> getRefreshAdapter(){
        return mRepository.getRefreshAdapter();
    }

    public void deleteObservedImageItem(SwipeImageItem item) {
        mRepository.deleteObservedImageItem(item);
    }

    public void refreshAdapter(){
        mRepository.refreshAdapter();
    }

    public void clearBrowsingHistory(){
        mRepository.clearBrowsingHistory();
    }

    public void resetTriggers(@Nullable Boolean resetToastValue, @Nullable Boolean resetPerformNavigation) {
        /*параметр может быть или null(не трогать) или true(обнулить значение)*/
        mRepository.resetTriggers(resetToastValue, resetPerformNavigation);
    }
}
