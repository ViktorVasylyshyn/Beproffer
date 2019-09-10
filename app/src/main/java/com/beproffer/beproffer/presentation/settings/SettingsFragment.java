package com.beproffer.beproffer.presentation.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SettingsFragmentLayoutBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.browsing.BrowsingViewModel;
import com.beproffer.beproffer.presentation.sign_in_up.change_password.ResetPasswordFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends BaseUserInfoFragment {

    private SettingsFragmentLayoutBinding mBinding;

    private BrowsingViewModel mBrowsingViewModel;

    private FirebaseAuth mAuth;

    private final SettingsFragmentCallback mCallback = new SettingsFragmentCallback() {
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
        PopupMenu popupMenu = new PopupMenu(requireActivity(), mBinding.settingsClearBrowsingHistoryButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu_clear_browsing_history, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_clear_browsing_history_clear) {
                mBinding.settingsClearBrowsingHistoryButton.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.settingsClearBrowsingHistoryButton.setTextColor(getResources().getColor(R.color.color_base_text_70per));
                mBinding.settingsClearBrowsingHistoryButton.setClickable(false);

                if (mBrowsingViewModel == null) {
                    mBrowsingViewModel = ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class);
                }
                /*актив\неактив прогресс бар*/
                mBrowsingViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
                    if (progress == null)
                        return;
                    showProgress(progress);
                });
                /*показ тостов*/
                mBrowsingViewModel.getShowToast().observe(getViewLifecycleOwner(), resId -> {
                    if (resId == null) {
                        return;
                    }
                    showToast(resId);
                });
                mBrowsingViewModel.clearBrowsingHistory();
            }
            return true;
        });
        popupMenu.show();
    }

    private void deleteProfile() {
        if (mBinding.settingsDeleteAccountEmail.getText().toString().isEmpty()) {
            mBinding.settingsDeleteAccountEmail.setError(getResources().getText(R.string.error_message_enter_email));
            mBinding.settingsDeleteAccountEmail.requestFocus();
            return;
        }
        hideKeyboard(requireActivity());
        try {
            /*согласно этому удалению - юзер удаляется из аутентификации, в дерево users\deleted
             помещается соответствующая запись с его айди и типом юзера. но при этом, его данные остаются в
             базе данных и в хранилище картинок. на даный момент предполагается ручное
              удаление данных юзера. со временем сделать удаление более граммотно*/
            FirebaseUser currentUser = getFirebaseUser();
            if (currentUser != null && mBinding.settingsDeleteAccountEmail.getText().toString().equals(currentUser.getEmail())) {
                showProgress(true);
                FirebaseDatabase.getInstance().getReference().
                        child(Const.USERS).
                        child(Const.DELETED).
                        child(currentUser.getUid()).
                        setValue(mCurrentUserInfo.getType())
                        .addOnSuccessListener(task -> currentUser.delete().addOnSuccessListener(deleted -> {
                            showProgress(false);
                            mUserDataViewModel.resetUserData();
                            performOnBottomNavigationBarItemClick(R.id.bnm_images_gallery, null);
                            showToast(R.string.toast_profile_deleted);
                        }).addOnFailureListener(e -> {
                            showProgress(false);
                            mBinding.settingsDeleteAccountButton.setError(e.getMessage());
                            showToast(R.string.toast_error_has_occurred);
                        })).addOnFailureListener(e -> {
                    showProgress(false);
                    showToast(R.string.toast_error_has_occurred);
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
        changeFragment(new ResetPasswordFragment(), true, true, false, null);
    }
}
