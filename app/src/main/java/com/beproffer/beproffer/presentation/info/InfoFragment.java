package com.beproffer.beproffer.presentation.info;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.InfoFragmentLayoutBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;

public class InfoFragment extends BaseUserInfoFragment {

    private InfoFragmentLayoutBinding mBinding;

    private final InfoFragmentCallback mCallback = new InfoFragmentCallback() {
        @Override
        public void onOfficialSiteClick() {
            openDoc(R.string.href_official_site);
        }

        @Override
        public void onUseTermsClick() {
            openDoc(R.string.href_terms_of_service);
        }

        @Override
        public void onPrivacyClick() {
            openDoc(R.string.href_privacy_policy);
        }

        @Override
        public void onTelegramClicked() {
            if (!isAppInstalled(R.string.package_id_telegram)) {
                showToast(R.string.toast_app_not_found_on_device);
                return;
            }
            if (mCurrentUserInfo != null && mCurrentUserInfo.getType().equals(Const.SPEC)) {
                openDoc(R.string.href_telegram_chat_specialists);
            } else {
                openDoc(R.string.href_telegram_chat_customers);
            }
        }

        @Override
        public void onViberClicked() {
            if (!isAppInstalled(R.string.package_id_viber)) {
                showToast(R.string.toast_app_not_found_on_device);
                return;
            }
            if (mCurrentUserInfo != null && mCurrentUserInfo.getType().equals(Const.SPEC)) {
                openDoc(R.string.href_viber_chat_specialists);
            } else {
                openDoc(R.string.href_viber_chat_customers);
            }
        }

        @Override
        public void onFacebookClicked() {
            /*вызов группы на девайсе не предлагает открыть группу через фейсбук приложение, а только
             * через браузеры. почему так понять не могу. оставляю пока что так.*/
            if (mCurrentUserInfo != null) {
                openDoc(R.string.href_facebook_page);
            }
        }

        @Override
        public void onInstagramClicked() {
            if (mCurrentUserInfo != null) {
                openDoc(R.string.href_instagram_page);
            }
        }
    };

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAppInstalled(int uriId) {
        PackageManager pm = requireActivity().getPackageManager();
        try {
            pm.getPackageInfo(getResources().getString(uriId), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.info_fragment_layout, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        if (getFirebaseUser() != null) {
            initUserData();
        }
    }
}