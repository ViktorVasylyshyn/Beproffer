package com.beproffer.beproffer.presentation.base;

import android.os.Handler;

import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.beproffer.beproffer.presentation.activities.MainActivity;
import com.beproffer.beproffer.util.Const;

public class BaseUserInfoFragment extends BaseFragment {

    protected UserDataViewModel mUserDataViewModel;

    protected UserInfo mCurrentUserInfo;

    protected void initUserData() {
        if (!checkInternetConnection()) {
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
            mUserDataViewModel.resetValues(true, false, false, false);
            showToast(resId);
        });

        mUserDataViewModel.getHideKeyboard().observe(getViewLifecycleOwner(), hide -> {
            if (hide != null && hide) {
                mUserDataViewModel.resetValues(false, true, false, false);
                hideKeyboard(requireActivity());
            }
        });

        mUserDataViewModel.getPopBackStack().observe(getViewLifecycleOwner(), back -> {
            if (back != null && back) {
                mUserDataViewModel.resetValues(false, false, true, false);
                Handler handler = new Handler();
                handler.postDelayed(this::popBackStack, Const.POPBACKSTACK_WAITING);
            }
        });

        mUserDataViewModel.getMessageResId().observe(getViewLifecycleOwner(), messageResId -> {
            if (messageResId == null)
                return;
            mUserDataViewModel.resetValues(false, false, false, true);
            showErrorMessage(messageResId);
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

    protected void applyUserData() {
        /*предполагается что во фрагментах наследниках, в этом методе будет размещаться логика, которая
         * должна начать совершаться, когда данные юзера получены*/
    }

    protected void showErrorMessage(int messageResId) {
        /*разная реализация, для разных фрагментов*/
    }
}
