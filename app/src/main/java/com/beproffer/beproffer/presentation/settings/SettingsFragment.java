package com.beproffer.beproffer.presentation.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SettingsFragmentLayoutBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.swimg.SwipeImagesViewModel;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends BaseUserInfoFragment {

    private SettingsFragmentLayoutBinding mBinding;

    private boolean mDeleteViewsHistoryAccess;

    private SwipeImagesViewModel mSwipeImagesViewModel;

    private FirebaseAuth mAuth;

    private SettingsFragmentCallback mCallback = new SettingsFragmentCallback() {
        @Override
        public void clearBrowsingHistoryClick() {
            clearBrowsingHistory();
        }

        @Override
        public void deleteProfileClick() {
            deleteProfile();
        }

        @Override
        public void resetPasswordClick() {
            resetPassword();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.settings_fragment_layout, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mAuth = FirebaseAuth.getInstance();
        initUserData();
    }

    private void clearBrowsingHistory() {
        if (mDeleteViewsHistoryAccess) {
            if (mSwipeImagesViewModel == null) {
                mSwipeImagesViewModel = ViewModelProviders.of(requireActivity()).get(SwipeImagesViewModel.class);
            }
            /*актив\неактив прогресс бар*/
            mSwipeImagesViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
                if (progress == null)
                    return;
                showProgress(progress);
            });
            /*показ тостов*/
            mSwipeImagesViewModel.getShowToast().observe(getViewLifecycleOwner(), resId -> {
                if (resId == null)
                    return;
                showToast(resId);
            });
            mSwipeImagesViewModel.clearBrowsingHistory();
        } else {
            showToast(R.string.toast_browsing_history_delete_access);
            mDeleteViewsHistoryAccess = true;
        }
    }

    private void deleteProfile() {
        if (mBinding.settingsDeleteAccountEmail.getText().toString().isEmpty()) {
            mBinding.settingsDeleteAccountEmail.setError(getResources().getText(R.string.error_message_enter_email));
            mBinding.settingsDeleteAccountEmail.requestFocus();
            return;
        }

        try {
            /*согласно этому удалению - юзер удаляется из аутентификации, в дерево users\deleted
             помещается соответствующая запись с его айди и типом юзера. но при этом, его данные остаются в
             базе данных и в хранилище картинок. на даный момент предполагается ручное
              удаление данных юзера. со временем сделать удаление более граммотно*/
            FirebaseUser currentUser = getFirebaseUser();
            if (currentUser != null && mBinding.settingsDeleteAccountEmail.getText().toString().equals(currentUser.getEmail())) {
                showProgress(true);
                currentUser.delete().addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference().
                                child(Const.USERS).
                                child(Const.DELETED).
                                child(currentUser.getUid()).
                                setValue(mCurrentUserInfo.getUserType()).addOnCompleteListener(task1 -> {
                            mUserDataViewModel.resetUserData();

                            if (task1.isCanceled())
                                Log.d(Const.INFO, task1.toString());

                            showToast(R.string.toast_profile_deleted);
                            performNavigation(R.id.action_global_swipeImageFragment);
                        });
                    } else {
                        showToast(R.string.toast_error_has_occurred);
                    }
                });
            } else {
                showProgress(false);
                showToast(R.string.error_message_delete_profile_wrong_email);
            }
        } catch (NullPointerException e) {
            showProgress(false);
            showToast(R.string.toast_error_has_occurred);
        }
    }

    private void resetPassword() {
        mAuth.signOut();
        performNavigation(R.id.action_settingsFragment_to_resetPasswordFragment);
    }
}
