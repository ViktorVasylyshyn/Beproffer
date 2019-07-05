package com.beproffer.beproffer.presentation.base;

import android.arch.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.google.firebase.auth.FirebaseUser;

public class BaseUserInfoFragment extends BaseFragment {

    public UserDataViewModel mUserDataViewModel;

    public FirebaseUser mCurrentUser;

    public UserInfo mCurrentUserInfo;

    public void initUserData() {
        if (!checkInternetConnection()) {
            /*сделать здесь переход, на какой нить фрагмент*/
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        mUserDataViewModel = ViewModelProviders.of(requireActivity()).get(UserDataViewModel.class);
        mUserDataViewModel.getFirebaseAuthLiveData().observe(this, user -> {
            if (user != null) {
                mCurrentUser = user;
                obtainUserInfo();
            }
        });
        
        mUserDataViewModel.getShowProgress().observe(this, progress -> {
            if(progress == null)
                return;
            showProgress(progress);
        });

        mUserDataViewModel.getShowToast().observe(this, resId ->{
            if(resId == null)
                return;
            showToast(resId);
            mUserDataViewModel.resetTrigger(false, null, null );
        });

        mUserDataViewModel.getHideKeybourd().observe(this, hide ->{
            if(hide == null)
                return;
            hideKeyboard(requireActivity());
            mUserDataViewModel.resetTrigger(null, false, null );
        });

        mUserDataViewModel.getPopBackStack().observe(this, back ->{
            if(back == null)
                return;
            popBackStack();
            mUserDataViewModel.resetTrigger(null, null, false );
        });
    }

    public void obtainUserInfo() {
        mUserDataViewModel.getUserInfoLiveData().observe(this, userInfo -> {
            if (userInfo != null) {
                mCurrentUserInfo = userInfo;
                applyUserData();
            }
        });
    }

    public void applyUserData() {
    }
}
