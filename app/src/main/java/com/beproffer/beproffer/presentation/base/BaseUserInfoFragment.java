package com.beproffer.beproffer.presentation.base;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.beproffer.beproffer.util.Const;

public class BaseUserInfoFragment extends BaseFragment {

    public UserDataViewModel mUserDataViewModel;

    public UserInfo mCurrentUserInfo;

    @Override
    public void onStart() {
        super.onStart();
    }

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
            mUserDataViewModel.resetTrigger(null, true, null);
            hideKeyboard(requireActivity());
        });

        mUserDataViewModel.getPopBackStack().observe(getViewLifecycleOwner(), back -> {
            if (back == null)
                return;
            mUserDataViewModel.resetTrigger(null, null, true);
            Handler handlerWordAnim = new Handler();
            handlerWordAnim.postDelayed(this::popBackStack, Const.POPBACKSTACK_WAITING);

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
        * должна начать совершаться, когда данные юзера получены*/
    }
}
