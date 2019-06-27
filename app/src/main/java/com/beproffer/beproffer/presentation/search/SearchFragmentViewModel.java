package com.beproffer.beproffer.presentation.search;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;

import java.util.Map;

public class SearchFragmentViewModel extends ViewModel {

    private MutableLiveData<Map<String, String>> mSearchRequestLiveData = new MutableLiveData<>();
    private MutableLiveData<String> mSearchRequestGenderLiveData = new MutableLiveData<>();

    LiveData<Map<String, String>> getSearchRequest() {
        return mSearchRequestLiveData;
    }
    LiveData<String> getSearchRequestGender(){return mSearchRequestGenderLiveData;}

    public void handleServiceTypeClick(View view) {
        defineServiceType(view);
    }


    private void defineServiceType(View view) {
        int requiredMenuRes = 0;
        switch (view.getId()) {
            case R.id.search_haircut_icon:
                requiredMenuRes = R.menu.nenu_hair_services;
                break;
            case R.id.search_nails_icon:
                requiredMenuRes = R.menu.nenu_nails_services;
                break;
            case R.id.search_makeup_icon:
                requiredMenuRes = R.menu.nenu_makeup_services;
                break;
            case R.id.search_tattoo_piercing_icon:
                requiredMenuRes = R.menu.nenu_tattoo_services;
                break;
            case R.id.search_barber_icon:
                requiredMenuRes = R.menu.nenu_barber_services;
                break;
            case R.id.search_fitness_icon:
                requiredMenuRes = R.menu.nenu_fitness_services;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(requiredMenuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            mSearchRequestLiveData.postValue(new DefineServiceType(view.getContext()).setRequest(menuItem));
            /*this map have service's type, subtype and title*/
            return true;
        });
        popupMenu.show();
    }

    public void handleGenderClick(View view){
        String gender;
        switch (view.getId()){
            case R.id.search_gender_male_icon:
                gender = Const.MALE;
                break;
            case R.id.search_gender_female_icon:
                gender = Const.FEMALE;
                break;
            case R.id.search_gender_any_icon:
                gender = Const.ALLGEND;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mSearchRequestGenderLiveData.setValue(gender);
    }
}
