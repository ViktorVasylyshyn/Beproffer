package com.beproffer.beproffer.presentation.profile.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.ProfileFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.info.InfoFragment;
import com.beproffer.beproffer.presentation.profile.profile_edit.ProfileEditFragment;
import com.beproffer.beproffer.presentation.settings.SettingsFragment;
import com.beproffer.beproffer.presentation.spec_gallery.SpecialistGalleryFragment;
import com.beproffer.beproffer.presentation.swimg.SwipeImagesViewModel;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends BaseUserInfoFragment {

    private ProfileFragmentBinding mBinding;

    private final ProfileFragmentCallback mCallback = new ProfileFragmentCallback() {

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
        if (getFirebaseUser() == null) {
            ((MainActivity) requireActivity()).onBottomNavigationBarItemClicked(R.id.bnm_images_gallery, R.string.toast_error_has_occurred);
        } else {
            initUserData();
        }
    }

    @Override
    public void applyUserData() {
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            adaptProfileForSpecialist();
        }
        mBinding.setUserInfo(mCurrentUserInfo);

        hintsForUsers();

        showProgress(false);
    }

    private void adaptProfileForSpecialist() {
        mBinding.profileStorage.setVisibility(View.VISIBLE);
    }

    private void performNavigation(View view) {
        Fragment fragment;
        switch (view.getId()) {
            case R.id.profile_storage:
                fragment = new SpecialistGalleryFragment();
                break;
            case R.id.profile_edit:
                fragment = new ProfileEditFragment();
                break;
            case R.id.profile_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.profile_info:
                fragment = new InfoFragment();
                break;
            default:
                showToast(R.string.toast_error_has_occurred);
                return;
        }
        changeFragment(fragment, true, false);
    }

    private void hintsForUsers() {
        /*просто анимация, типа юзерфрендли и все такое. нужно подтолкнуть человека к тем действиям, которые от
         * него требуютсяю в случае специалиста - подмигивание значка галлереи. если там все нормально -
         * проверяем есть ли описание профиля. предположим, что если описания нет, то профиль заполнен
         * не до концаю для кастомера же проверяем только описание*/
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            mUserDataViewModel.getSpecialistGalleryData().observe(this, data -> {
                if (data != null && data.size() < Const.IMAGES_BASE_SET_COUNT) {
                    Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.hint_blinking_icon_anim);
                    mBinding.profileStorage.startAnimation(animation);
                    mBinding.profileBottomHint.setText(R.string.hint_specialist_add_more_images);
                } else {
                    if (mCurrentUserInfo.getUserInfo() == null || mCurrentUserInfo.getUserInfo().isEmpty()) {
                        iconsBlinkingAnim();
                    } else {
                        joinUsHint();
                    }
                }
            });
        } else {
            if (mCurrentUserInfo.getUserInfo() == null || mCurrentUserInfo.getUserInfo().isEmpty()) {
                iconsBlinkingAnim();
            } else {
                joinUsHint();
            }
        }
    }

    private void joinUsHint() {
        mBinding.profileBottomHint.setText(R.string.hint_join_us_on_social);
    }

    private void iconsBlinkingAnim() {
        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.hint_blinking_icon_anim);
        mBinding.profileEdit.startAnimation(animation);
        mBinding.profileBottomHint.setText(R.string.hint_any_add_more_personal_info);
    }

    private void logOut() {
        mUserDataViewModel.resetUserData();
        FirebaseAuth.getInstance().signOut();
        ViewModelProviders.of(requireActivity()).get(SwipeImagesViewModel.class).refreshAdapter();
        performOnBottomNavigationBarItemClick(R.id.bnm_images_gallery, null);
    }
}
