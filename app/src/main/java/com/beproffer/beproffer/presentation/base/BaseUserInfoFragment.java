package com.beproffer.beproffer.presentation.base;

import android.arch.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.presentation.UserDataViewModel;

public class BaseUserInfoFragment extends BaseFragment {

    public UserDataViewModel mUserDataViewModel;

    public UserInfo mCurrentUserInfo;

    public void initUserData() {
        if (!checkInternetConnection()) {
            /*сделать здесь переход, на какой нить фрагмент*/
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if(getFirebaseUser()!=null)
            initObservers();
    }

    private void initObservers(){
        mUserDataViewModel = ViewModelProviders.of(requireActivity()).get(UserDataViewModel.class);
        mUserDataViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress == null)
                return;
            showProgress(progress);
        });
        mUserDataViewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            if (processing == null)
                return;
            processing(processing);
        });
        mUserDataViewModel.getShowToast().observe(getViewLifecycleOwner(), resId -> {
            if (resId == null)
                return;
            showToast(resId);
            mUserDataViewModel.resetTrigger(true, null, null);
        });

        mUserDataViewModel.getHideKeyboard().observe(getViewLifecycleOwner(), hide -> {
            if (hide == null)
                return;
            hideKeyboard(requireActivity());
            mUserDataViewModel.resetTrigger(null, true, null);
        });

        mUserDataViewModel.getPopBackStack().observe(getViewLifecycleOwner(), back -> {
            if (back == null)
                return;
            popBackStack();
            mUserDataViewModel.resetTrigger(null, null, true);
        });
        obtainUserInfo();
    }

    public void obtainUserInfo() {
        mUserDataViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                mCurrentUserInfo = userInfo;
                applyUserData();
            }
        });
    }

    public void applyUserData() {
        /*предполагается что во фрагментах наследниках, в этом методе будет размещаться логика, которая
        * должна начать соверщаться, когда данные юзера получены*/
    }
}
