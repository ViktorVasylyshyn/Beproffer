package com.beproffer.beproffer.presentation.info;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            if(mCurrentUserInfo != null && mCurrentUserInfo.getUserType().equals(Const.SPEC)){
                openDoc(R.string.href_telegram_chat_specialists);
            }else {
                openDoc(R.string.href_telegram_chat_customers);
            }
        }

        @Override
        public void onViberClicked() {
            if(mCurrentUserInfo != null && mCurrentUserInfo.getUserType().equals(Const.SPEC)){
                openDoc(R.string.href_viber_chat_specialists);
            }else {
                openDoc(R.string.href_viber_chat_customers);
            }
        }

        @Override
        public void onFacebookClicked() {
            /*вызов группы на девайсе не предлагает открыть группу через фейсбук приложение, а только
            * через браузеры. почему так понять не могу. оставляю пока что так.*/
            if(mCurrentUserInfo != null && mCurrentUserInfo.getUserType().equals(Const.SPEC)){
                openDoc(R.string.href_facebook_page);
            }
        }
    };

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
        if(getFirebaseUser()!=null){
            initUserData();
        }
    }

    private void openDoc(int resId) {
        if(checkInternetConnection()){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
            startActivity(browserIntent);
        }else {
            showToast(R.string.toast_no_internet_connection);
        }
    }
}