package com.beproffer.beproffer.presentation.base;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.beproffer.beproffer.util.Const;

public class BaseUserInfoFragment extends BaseFragment {

    protected UserDataViewModel mUserDataViewModel;

    protected UserInfo mCurrentUserInfo;

    protected void initUserData() {
        if (!checkInternetConnection()) {
            /*сделать здесь переход, на какой нить фрагмент*/
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if (getFirebaseUser() != null)
            initObservers();
    }

    private void initObservers() {
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

    private void obtainUserInfo() {
        mUserDataViewModel.getUserInfoLiveData().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                mCurrentUserInfo = userInfo;
                applyUserData();
            }
        });
    }

    public void setBadgeIfNeed(){
        if (mCurrentUserInfo.getType().equals(Const.SPEC)) {
            mUserDataViewModel.getIncomingContactRequests().observe(getViewLifecycleOwner(), list -> {
                if (list != null) {
                    if (!list.isEmpty()) {
                        setBadge(Const.CONTBNBINDEX);
                    }
                }
            });
        }
    }

    protected void applyUserData() {
        /*предполагается что во фрагментах наследниках, в этом методе будет размещаться логика, которая
         * должна начать совершаться, когда данные юзера получены*/
    }

    protected void setBadge(int index) {
        ((MainActivity) requireActivity()).setBadgeMain(index);
    }

    protected void removeBadge(int index) {
        ((MainActivity) requireActivity()).removeBadge(index);
    }
}
