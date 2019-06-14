package com.beproffer.beproffer.presentation.specstorage.edit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;

import java.util.Map;

public class SpecialistStorageEditFragmentViewModel extends ViewModel {

    private StorageImageItem mStorageImageItem;

    private MutableLiveData<StorageImageItem> mStorageImageItemLiveData = new MutableLiveData<>();

    public void setStorageImageItem(StorageImageItem storageImageItem) {
        mStorageImageItem = storageImageItem;
        updateStorageImageItemLiveData();

    }

    LiveData<StorageImageItem> getStorageImageItem() {
        return mStorageImageItemLiveData;
    }

    public void onTypeButtonClick(View view) {
        int requiredMenuRes;
        switch (view.getId()) {
            case R.id.specialist_storage_edit_haircut_icon:
                requiredMenuRes = R.menu.nenu_hair_services;
                break;
            case R.id.specialist_storage_edit_nails_icon:
                requiredMenuRes = R.menu.nenu_nails_services;
                break;
            case R.id.specialist_storage_edit_makeup_icon:
                requiredMenuRes = R.menu.nenu_makeup_services;
                break;
            case R.id.specialist_storage_edit_barber_icon:
                requiredMenuRes = R.menu.nenu_barber_services;
                break;
            case R.id.specialist_storage_edit_tattoo_piercing_icon:
                requiredMenuRes = R.menu.nenu_tattoo_services;
                break;
            case R.id.specialist_storage_edit_fitness_icon:
                requiredMenuRes = R.menu.nenu_fitness_services;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(requiredMenuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            Map<String, String> serviceType = new DefineServiceType(view.getContext()).setRequest(menuItem);
            mStorageImageItem.setType(serviceType.get(Const.SERVTYPE));
            mStorageImageItem.setSubtype(serviceType.get(Const.SERVSBTP));
            updateStorageImageItemLiveData();
            return true;
        });
        popupMenu.show();
    }

    public void onGenderIconClick(View view) {
        String gender;
        switch (view.getId()){
            case R.id.specialist_storage_edit_gender_male_icon:
                gender = Const.MALE;
                break;
            case R.id.specialist_storage_edit_gender_female_icon:
                gender = Const.FEMALE;
                break;
            case R.id.specialist_storage_edit_gender_all_icon:
                gender = Const.ALLGEND;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mStorageImageItem.setGender(gender);
        updateStorageImageItemLiveData();
    }

    public void onDurationIconClick(View view){
        String duration;
        switch (view.getId()){
            case R.id.specialist_storage_edit_duration_30_min_icon:
                duration = Const.MIN30;
                break;
            case R.id.specialist_storage_edit_duration_45_min_icon:
                duration = Const.MIN45;
                break;
            case R.id.specialist_storage_edit_duration_60_min_icon:
                duration = Const.MIN60;
                break;
            case R.id.specialist_storage_edit_duration_90_min_icon:
                duration = Const.MIN90;
                break;
            case R.id.specialist_storage_edit_duration_120_min_icon:
                duration = Const.MIN120;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        mStorageImageItem.setDuration(duration);
        updateStorageImageItemLiveData();
    }

    private void updateStorageImageItemLiveData() {
        mStorageImageItemLiveData.setValue(mStorageImageItem);
    }

}
