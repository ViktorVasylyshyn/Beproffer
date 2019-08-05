package com.beproffer.beproffer.presentation.profile.profile_edit;

import android.app.Activity;
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
import com.beproffer.beproffer.databinding.ProfileEditFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;

public class ProfileEditFragment extends BaseUserInfoFragment {

    private ProfileEditFragmentBinding mBinding;

    private Uri mResultUri;

    private final ProfileEditFragmentCallback mCallback = new ProfileEditFragmentCallback() {
        @Override
        public void onSetProfileImageClick() {
            setProfileImage();
        }

        @Override
        public void onCheckUserDataClick() {
            checkUserData();
        }

        @Override
        public void denyChanges() {
            ((MainActivity)requireActivity()).popBackStack();
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
        mBinding.setUserInfo(mCurrentUserInfo);
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
        }
    }

    private void checkUserData() {
        if (null == mResultUri && mCurrentUserInfo.getUserProfileImageUrl() == null) {
            showToast(R.string.toast_select_image);
            mBinding.editFragmentBottomHint.setText(R.string.hint_any_image_size);
            return;
        }
        if (mBinding.editFragmentName.getText().toString().isEmpty() && mCurrentUserInfo.getUserName() == null) {
            showToast(R.string.toast_enter_name);
            return;
        }
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            if (mCurrentUserInfo.getUserPhone() != null || !mBinding.editFragmentPhone.getText().toString().isEmpty()) {
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
        if(mProcessing.get()){
            showToast(R.string.toast_processing);
            return;
        }
        if (mCurrentUserInfo.getUserPhone() != null && (mCurrentUserInfo.getUserPhone().length() < 5 || mCurrentUserInfo.getUserPhone().length() > 13)) {
            mBinding.editFragmentPhone.requestFocus();
            mBinding.editFragmentPhone.setError(getResources().getText(R.string.error_message_wrong_phone_number_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
            return;
        }
        if (mCurrentUserInfo.getUserName().length() < 4) {
            mBinding.editFragmentName.requestFocus();
            mBinding.editFragmentName.setError(getResources().getText(R.string.error_message_wrong_name_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_outfield_use_correct_name_format);
            return;
        }
        mUserDataViewModel.updateUserInfo(mCurrentUserInfo, mResultUri);
    }

    private void setProfileImage() {
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
                mCurrentUserInfo.setUserProfileImageUrl(mResultUri.toString());
                mBinding.setUserInfo(mCurrentUserInfo);
            } catch (NullPointerException e) {
                showToast(R.string.toast_error_has_occurred);
            }
        }
    }
}