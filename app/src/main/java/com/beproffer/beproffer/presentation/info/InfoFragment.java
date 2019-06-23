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
import com.beproffer.beproffer.presentation.base.BaseFragment;

public class InfoFragment extends BaseFragment {

    private InfoFragmentLayoutBinding mBinding;

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

        mBinding.infoFragmentTerms.setOnClickListener(v -> openDoc(R.string.href_terms_of_service));
        mBinding.infoFragmentPrivacyPolicy.setOnClickListener(v -> openDoc(R.string.href_privacy_policy));
    }

    private void openDoc(int resId) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
        startActivity(browserIntent);
    }
}