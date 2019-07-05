package com.beproffer.beproffer.presentation.specstorage.edit;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageEditFragmentBinding;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;
import com.crashlytics.android.Crashlytics;

public class SpecialistGalleryEditFragment extends BaseUserInfoFragment {

    private SpecialistGalleryEditFragmentViewModel mViewModel;

    private SpecialistStorageEditFragmentBinding mBinding;
    private SpecialistGalleryImageItem mImageItem;
    private Uri mResultUri;

    private SpecialistStorageEditFragmentCallback mCallback = new SpecialistStorageEditFragmentCallback() {
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
        showProgress(true);
        mViewModel = ViewModelProviders.of(requireActivity()).get(SpecialistGalleryEditFragmentViewModel.class);
        mBinding.setViewModel(mViewModel);
        mBinding.setFragmentCallback(mCallback);

        initUserData();

        obtainStorageImageItem();
    }

    private void obtainStorageImageItem() {
        mViewModel.getStorageImageItem().observe(this, item -> {
            mImageItem = item;
            showProgress(false);
            mBinding.setItem(mImageItem);
        });
    }

    private void checkServiceImageData() {
        if (mImageItem.getUrl() == null && mResultUri == null) {
            showToast(R.string.toast_select_image);
            return;
        }
        if (mImageItem.getSubtype() == null) {
            showToast(R.string.toast_select_type);
            return;
        }
        if (mImageItem.getGender() == null) {
            showToast(R.string.toast_determine_gender);
            return;
        }
        if (mImageItem.getDuration() == null) {
            showToast(R.string.toast_select_duration);
            return;
        }
        if (mBinding.specialistStorageEditPrice.getText().toString().isEmpty() && mImageItem.getPrice() == null) {
            showToast(R.string.toast_enter_price);
            return;
        }
        if (mBinding.specialistStorageEditDescription.getText().toString().isEmpty() && mImageItem.getDescription() == null) {
            showToast(R.string.toast_enter_description);
            return;
        }
        saveNewImageData();
    }

    private void saveNewImageData() {

        ViewModelProviders.of(requireActivity()).get(UserDataViewModel.class).updateSpecialistGallery(mImageItem, mResultUri);

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
                    mImageItem.setUrl(mResultUri.toString());
                }
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            mBinding.setItem(mImageItem);
        }
    }
}
