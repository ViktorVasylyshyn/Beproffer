package com.beproffer.beproffer.presentation.swimg;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.beproffer.beproffer.data.models.SwipeImageItem;

public class ImageItemTransferViewModel extends ViewModel {

    private final MutableLiveData<SwipeImageItem> item = new MutableLiveData<>();

    public void setImageItem(SwipeImageItem item){
        this.item.setValue(item);
    }

    public LiveData<SwipeImageItem> getImageItem(){
        return item;
    }
}
