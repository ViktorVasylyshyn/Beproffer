package com.beproffer.beproffer.presentation.spec_gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.presentation.spec_gallery.edit.SpecialistGalleryEditFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

/**Показ специалисту его галереи с изображениями сервисов.*/

public class SpecialistGalleryFragment extends BaseGalleryFragment {

    private final SpecialistGalleryFragmentCallback mCallback = this::addNewImage;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setShowButton(mShowButton);
        initUserData();
    }

    @Override
    public void applyUserData() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mImageItemsList == null) {
            mUserDataViewModel.getServiceItemsList().observe(this, data -> {
                mImageItemsList = new ArrayList<>(); //TODO попробовать убрать
                mShowButton.set(true);
                if (data != null) {
                    mImageItemsList = new ArrayList<>();
                    for (Map.Entry<String, BrowsingImageItem> entry : data.entrySet()) {
                        mImageItemsList.add(entry.getValue());
                    }
                    mImageAdapter.setData(mImageItemsList);
                }
            });
        }
    }

    private void addNewImage() {
        if (mImageItemsList != null && mImageItemsList.size() >= 5) {
            showToast(R.string.toast_would_you_like_to_donate);
            cooldown(mBinding.specialistGalleryAddImageButton);
            return;
        }

        mUserDataViewModel.setEditableGalleryItem(new BrowsingImageItem(
                null,
                FirebaseDatabase.getInstance().getReference().child(Const.USERS).child(Const.SPEC)
                        .child(mCurrentUserInfo.getId()).child(Const.SERVICES).push().getKey(),
                null,
                null,
                null,
                null,
                null,
                null,
                null));
        changeFragment(new SpecialistGalleryEditFragment(), true, false, false, null);
    }

    @Override
    void showServiceInfo(BrowsingImageItem editableItem) {
        mUserDataViewModel.setEditableGalleryItem(editableItem);
        changeFragment(new SpecialistGalleryEditFragment(), true, false, false, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard(requireActivity());
    }
}

