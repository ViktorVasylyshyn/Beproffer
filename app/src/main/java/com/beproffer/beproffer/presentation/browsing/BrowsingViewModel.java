package com.beproffer.beproffer.presentation.browsing;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.beproffer.beproffer.data.firebase.BrowsingItemsRepository;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;

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

    public LiveData<Boolean> getShowSearchPanel() {
        return mRepository.getShowSearchPanel();
    }

    public LiveData<Integer> getShowViewMessage() {
        return mRepository.getShowMessage();
    }

    public void resetValues(@NonNull Boolean resetShowSearchPanelValue, @NonNull Boolean resetViewMessageValue){
        mRepository.resetValues(resetShowSearchPanelValue, resetViewMessageValue);
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
}
