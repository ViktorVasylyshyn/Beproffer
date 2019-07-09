package com.beproffer.beproffer.presentation.specstorage.edit;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageEditFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;
import com.crashlytics.android.Crashlytics;

import java.util.Map;

public class SpecialistGalleryEditFragment extends BaseUserInfoFragment {


    private SpecialistStorageEditFragmentBinding mBinding;
    private SpecialistGalleryImageItem mUpdatedImageItem;
    private String mPrimordialItemType;
    private String mPrimordialItemSubtype;
    private Uri mResultUri;

    private SpecialistStorageEditFragmentCallback mCallback = new SpecialistStorageEditFragmentCallback() {
        @Override
        public void onTypeButtonClick(View view) {
            setServiceType(view);
        }

        @Override
        public void onGenderButtonClick(View view) {
            setServiceGender(view);
        }

        @Override
        public void onDurationButtonClick(View view) {
            setServiceDuration(view);
        }

        @Override
        public void setImage() {
            setServiceImage();
        }

        @Override
        public void confirmImageData() {
            checkServiceImageData();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.specialist_storage_edit_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setFragmentCallback(mCallback);

        initUserData();

        obtainStorageImageItem();
    }

    private void obtainStorageImageItem() {
        mUserDataViewModel.getEditableGalleryItem().observe(this, item -> {
            /*получаем изначальные type и subtype, чтобы потом проверить -
            * изменились ли они после изменения обьекта. если да - это значит что актуальные данные
             * о обьекте записалсь в новую ветку в бд. соответственно из старой их нужно удалить.*/
            try {
                if(item.getType() != null && item.getSubtype() != null){
                    mPrimordialItemType = item.getType();
                    mPrimordialItemSubtype = item.getSubtype();
                }
            } catch (NullPointerException e){
                Crashlytics.getInstance().crash();
            }
            mUpdatedImageItem = item;
            mBinding.setItem(mUpdatedImageItem);
        });
    }

    private void checkServiceImageData() {
        if (mUpdatedImageItem.getUrl() == null && mResultUri == null) {
            showToast(R.string.toast_select_image);
            return;
        }
        if (mUpdatedImageItem.getSubtype() == null) {
            showToast(R.string.toast_select_type);
            return;
        }
        if (mUpdatedImageItem.getGender() == null) {
            showToast(R.string.toast_determine_gender);
            return;
        }
        if (mUpdatedImageItem.getDuration() == null) {
            showToast(R.string.toast_select_duration);
            return;
        }
        if (mBinding.specialistStorageEditPrice.getText().toString().isEmpty() && mUpdatedImageItem.getPrice() == null) {
            showToast(R.string.toast_enter_price);
            return;
        }
        if (mBinding.specialistStorageEditDescription.getText().toString().isEmpty() && mUpdatedImageItem.getDescription() == null) {
            showToast(R.string.toast_enter_description);
            return;
        }
        saveNewImageData();
    }


    public void setServiceType(View view) {
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
            mUpdatedImageItem.setType(serviceType.get(Const.SERVTYPE));
            mUpdatedImageItem.setSubtype(serviceType.get(Const.SERVSBTP));
            mBinding.setItem(mUpdatedImageItem);
            return true;
        });
        popupMenu.show();
    }

    private void saveNewImageData() {
        /*сравнением тип услуги начального обьекта и измененного.проверяем, изменился ли тип услуги.
         * если изменился - удаляем данные по старому адресу*/
        mUserDataViewModel.updateSpecialistGallery(mUpdatedImageItem, mResultUri);
        mUserDataViewModel.deleteNotRelevantImageData(mUpdatedImageItem, mPrimordialItemType, mPrimordialItemSubtype);
    }

    public void setServiceGender(View view) {
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
        mUpdatedImageItem.setGender(gender);
        mBinding.setItem(mUpdatedImageItem);
    }

    public void setServiceDuration(View view){
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
        mUpdatedImageItem.setDuration(duration);
        mBinding.setItem(mUpdatedImageItem);
    }

    private void setServiceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Const.REQUEST_CODE_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_1) {
            try {
                mResultUri = data.getData();
                if (mResultUri != null) {
                    mUpdatedImageItem.setUrl(mResultUri.toString());
                }
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            mBinding.setItem(mUpdatedImageItem);
        }
    }
}
