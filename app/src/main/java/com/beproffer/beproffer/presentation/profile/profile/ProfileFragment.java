package com.beproffer.beproffer.presentation.profile.profile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.ProfileFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends BaseUserInfoFragment {

    private ProfileFragmentBinding mBinding;

    private ProfileFragmentCallback mCallback = new ProfileFragmentCallback() {

        @Override
        public void onPerformNavigationClick(View view) {
            performNavigation(view);
        }

        @Override
        public void onLogOutClick() {
            logOut();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        if(FirebaseAuth.getInstance() == null){
            ((MainActivity)requireActivity()).performNavigation(R.id.action_global_signInFragment, null);
        }else {
            initUserData();
        }
    }

    @Override
    public void applyUserData() {
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            adaptProfileForSpecialist();
        }
        mBinding.setUserInfo(mCurrentUserInfo);
        showProgress(false);
    }

    private void adaptProfileForSpecialist() {
        mBinding.profileStorage.setVisibility(View.VISIBLE);
        mBinding.profileContactRequests.setVisibility(View.VISIBLE);
    }

    public void performNavigation(View view) {
        int res = 0;
        switch (view.getId()) {
            case R.id.profile_contacts:
                res = R.id.action_profileFragment_to_confirmedContactsFragment;
                break;
            case R.id.profile_contact_requests:
                res = R.id.action_profileFragment_to_contactRequestsFragment;
                break;
            case R.id.profile_storage:
                res = R.id.action_profileFragment_to_specialistStorageFragment;
                break;
            case R.id.profile_edit:
                res = R.id.action_profileFragment_to_profileEditFragment;
                break;
            case R.id.profile_settings:
                res = R.id.action_profileFragment_to_settingsFragment;
                break;
            case R.id.profile_info:
                res = R.id.action_profileFragment_to_infoFragment;
                break;
            default:
                showToast(R.string.toast_error_has_occurred);
        }
        ((MainActivity) requireActivity()).performNavigation(res, null);
    }

    public void logOut() {
        mUserDataViewModel.resetUserData();
        FirebaseAuth.getInstance().signOut();
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_swipeImageFragment, null);
    }
}
