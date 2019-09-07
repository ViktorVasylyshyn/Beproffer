package com.beproffer.beproffer.presentation.browsing;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.beproffer.beproffer.data.firebase.BrowsingItemsRepository;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.BrowsingItemRef;

public class BrowsingViewModel extends AndroidViewModel {

    private final BrowsingItemsRepository mRepository;

    public BrowsingViewModel(@NonNull Application application) {
        super(application);
        mRepository = new BrowsingItemsRepository(application);
    }

    public LiveData<BrowsingItemRef> getBrowsingItemRefs() {
        return mRepository.getBrowsingItemRefs();
    }

    public LiveData<BrowsingImageItem> getBrowsingItemInfo() {
        return mRepository.getTargetBrowsingItemLiveData();
    }

    public LiveData<Boolean> getClearRefs(){
        return mRepository.getClearRefs();
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

    public void resetValues(@NonNull Boolean resetShowSearchPanelValue,
                            @NonNull Boolean resetViewMessageValue,
                            @NonNull Boolean resetClearRefs){
        mRepository.resetValues(resetShowSearchPanelValue, resetViewMessageValue, resetClearRefs);
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
