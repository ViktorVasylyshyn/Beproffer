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
        if (mSwipeImageItemsList == null )
            mSwipeImageItemsList = mRepository.getSwipeImageItemsListLiveData();
        if(mSwipeImageItemsList.getValue() != null && mSwipeImageItemsList.getValue().size() == 0)
            mRepository.obtainImagesFromDb();
        return mSwipeImageItemsList;
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Integer> getShowToast() {
        return mRepository.getShowToast();
    }

    public LiveData<Integer> getPerformNavigation() {
        return mRepository.getPerformNavigation();
    }

    public void deleteObservedImageItem(SwipeImageItem item) {
        mRepository.deleteObservedImageItem(item);
    }

    public void clearBrowsingHistory(){
        mRepository.clearBrowsingHistory();
    }

    public void resetTriggers(@Nullable Boolean resetToastValue, @Nullable Boolean resetPerformNavigation) {
        /*параметр может быть или null(не трогать) или true(обнулить значение)*/
        mRepository.resetTriggers(resetToastValue, resetPerformNavigation);
    }


}
