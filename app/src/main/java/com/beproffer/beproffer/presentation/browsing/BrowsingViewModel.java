package com.beproffer.beproffer.presentation.browsing;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.data.firebase.BrowsingItemsRepository;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.data.models.BrowsingImageItem;

import java.util.List;

public class BrowsingViewModel extends AndroidViewModel {

    private final BrowsingItemsRepository mRepository;

    private LiveData<List<BrowsingItemRef>> mBrowsingItemRefsList;

    public BrowsingViewModel(@NonNull Application application) {
        super(application);
        mRepository = new BrowsingItemsRepository(application);
        if (mBrowsingItemRefsList == null)
            mBrowsingItemRefsList = mRepository.getBrowsingItemRefListLiveData();

    }

    public LiveData<List<BrowsingItemRef>> getBrowsingItemRefsList() {
        return mBrowsingItemRefsList;
    }

    public LiveData<BrowsingImageItem> getBrowsingItemInfo() {
        return mRepository.getTargetBrowsingItemLiveData();
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

    public void deleteObservedItem(BrowsingItemRef item) {
        mRepository.deleteObservedItem(item);
    }

    public void obtainBrowsingItemDetailInfo(BrowsingItemRef itemRef) {
        mRepository.obtainBrowsingItemDetailInfo(itemRef);
    }

    public void refreshAdapter() {
        mRepository.refreshAdapter();
    }

    public void clearBrowsingHistory() {
        mRepository.clearBrowsingHistory();
    }

    public void resetTriggers(@Nullable Boolean resetToastValue, @Nullable Boolean resetPerformSearch) {
        /*параметр может быть или null(не трогать) или true(обнулить значение)*/
        mRepository.resetTriggers(resetToastValue, resetPerformSearch);
    }
}
