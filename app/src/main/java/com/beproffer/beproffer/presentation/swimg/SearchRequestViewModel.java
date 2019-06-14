package com.beproffer.beproffer.presentation.swimg;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import java.util.Map;

public class SearchRequestViewModel extends ViewModel {

    private MutableLiveData<Map<String, String>> searchRequestMutable = new MutableLiveData<>();

    public LiveData<Map<String, String>> getSearchRequestLiveData() {
        return searchRequestMutable;
    }

    public void setSearchRequestLiveData(Map<String, String> searchRequestMap){
        searchRequestMutable.setValue(searchRequestMap);
    }
}
