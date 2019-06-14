package com.beproffer.beproffer.presentation.base;

import android.arch.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserData;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.google.firebase.auth.FirebaseUser;

public class BaseUserDataFragment extends BaseFragment {

    public UserDataViewModel mUserDataViewModel;

    public FirebaseUser mCurrentUser;

    public String mCurrentUserType;

    public UserData mCurrentUserData;

    public void initUserData() {
        if(!checkInternetConnection()){
            /*сделать здесь переход, на какой нить фрагмент*/
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        showProgress(true);
        mUserDataViewModel = ViewModelProviders.of(requireActivity()).get(UserDataViewModel.class);
        mUserDataViewModel.getFirebaseAuthLiveData().observe(this, user -> {
            if (user != null) {
                mCurrentUser = user;
                obtainUserType();
            }
        });
    }

    public void obtainUserType() {
        mUserDataViewModel.getCurrentUserType().observe(this, type -> {
            if (type != null) {
                mCurrentUserType = type;
                obtainUserData();
            }
        });
        mUserDataViewModel.defineUserType(requireContext(), mCurrentUser.getUid());
    }

    public void obtainUserData() {
        mUserDataViewModel.getUserData().observe(this, userData -> {
            if (userData != null) {
                mCurrentUserData = userData;
                applyUserData();
            }
        });
    }

    public void applyUserData() {
        showProgress(false);
    }
}
