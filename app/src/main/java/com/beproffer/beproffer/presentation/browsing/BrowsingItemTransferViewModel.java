package com.beproffer.beproffer.presentation.browsing;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.beproffer.beproffer.data.models.BrowsingItem;

public class BrowsingItemTransferViewModel extends ViewModel {

    private final MutableLiveData<BrowsingItem> item = new MutableLiveData<>();

    public void setBrowsingItem(BrowsingItem item){
        this.item.setValue(item);
    }

    public LiveData<BrowsingItem> getBrowsingItem(){
        return item;
    }
}
