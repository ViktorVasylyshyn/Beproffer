package com.beproffer.beproffer.presentation.profile.profile_edit;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.SaveUserData;
import com.beproffer.beproffer.databinding.ProfileEditFragmentBinding;
import com.beproffer.beproffer.util.ChangeGenderHelper;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserDataFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileEditFragment extends BaseUserDataFragment {

    private ProfileEditFragmentBinding mBinding;

    private Uri mResultUri;

    private ProfileEditFragmentCallback mCallback = new ProfileEditFragmentCallback() {
        @Override
        public void onSetProfileImageClick() {
            setProfileImage();
        }

        @Override
        public void onCheckUserDataClick() {
            checkUserData();
        }

        @Override
        public void onChangeUserGenderClick() {
            changeUserGender();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.profile_edit_fragment, container
                , false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);

        initUserData();
    }

    @Override
    public void applyUserData() {
        mBinding.setUserData(mCurrentUserData);
        if (mCurrentUserType.equals(Const.SPEC)) {
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
        }
        showProgress(false);
    }

    public void checkUserData() {
        if (null == mResultUri && mCurrentUserData.getUserProfileImageUrl() == null) {
            showToast(R.string.toast_select_image);
            return;
        }
        if (mBinding.editFragmentName.getText().toString().isEmpty() && mCurrentUserData.getUserName() == null) {
            showToast(R.string.toast_enter_name);
            return;
        }
        if (mCurrentUserData.getUserGender() == null) {
            showToast(R.string.toast_determine_your_gender);
            return;
        }
        if (mCurrentUserData.getUserType().equals(Const.SPEC)) {
            if (mCurrentUserData.getUserPhone() != null || !mBinding.editFragmentPhone.getText().toString().isEmpty()) {
                saveUserData();
            } else {
                showToast(R.string.toast_enter_phone);
            }
        } else {
            saveUserData();
        }
    }

    private void saveUserData() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if(mCurrentUserData.getUserPhone().length() < 5 ||  mCurrentUserData.getUserPhone().length() > 13){
            mBinding.editFragmentPhone.requestFocus();
            mBinding.editFragmentPhone.setError(getResources().getText(R.string.error_message_wrong_phone_number_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
            return;
        }
        if(mCurrentUserData.getUserName().length() < 4){
            mBinding.editFragmentName.requestFocus();
            mBinding.editFragmentName.setError(getResources().getText(R.string.error_message_wrong_name_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_outfield_use_correct_name_format);
            return;
        }
        showProgress(true);
        if (mResultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference()
                    .child(Const.PROF)
                    .child(mCurrentUserData.getUserType())
                    .child(mCurrentUserData.getUserId())
                    .child(mCurrentUserData.getUserId());
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(requireActivity().getApplication().getContentResolver(), mResultUri);
            } catch (IOException e) {
                showProgress(false);
                showToast(R.string.toast_error_has_occurred);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(e -> {
                showToast(R.string.toast_error_has_occurred);
                showProgress(false);
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(url -> {
                mCurrentUserData.setUserProfileImageUrl(url.toString());
                finalizeUserDataSaving();
            }));
        } else {
            finalizeUserDataSaving();
        }
    }

    private void finalizeUserDataSaving() {
        mUserDataViewModel.setUserData(mCurrentUserData);

        new SaveUserData().saveUserDataToDatabase(mCurrentUserData.getUserId()
                , mCurrentUserData, requireActivity()
                , mShowProgress
                , 0);

        ((MainActivity) requireActivity()).popBackStack();
    }

    public void setProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Const.REQUEST_CODE_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_1) {
            try {
                if (data == null) {
                    return;
                }
                mResultUri = data.getData();
                mCurrentUserData.setUserProfileImageUrl(mResultUri.toString());
                mBinding.setUserData(mCurrentUserData);
            } catch (NullPointerException e) {
                showToast(R.string.toast_error_has_occurred);
            }
        }
    }

    public void changeUserGender() {
        mCurrentUserData.setUserGender(new ChangeGenderHelper().changeGender(mCurrentUserData.getUserGender()));
        mBinding.setUserData(mCurrentUserData);
    }
}