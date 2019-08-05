package com.beproffer.beproffer.presentation.spec_gallery;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.spec_gallery.adapter.GalleryImageItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecialistGalleryFragment extends BaseUserInfoFragment {

    private final GalleryImageItemAdapter mImageAdapter = new GalleryImageItemAdapter();

    private final ObservableBoolean mShowButton = new ObservableBoolean();

    private List<SpecialistGalleryImageItem> mImageItemsList;

    private SpecialistStorageFragmentBinding mBinding;

    private final SpecialistGalleryFragmentCallback mCallback = this::addNewImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.specialist_storage_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        initRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setShowButton(mShowButton);

        initUserData();
    }

    @Override
    public void applyUserData() {
        if(!checkInternetConnection()){
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mImageItemsList == null) {
            mUserDataViewModel.getSpecialistGalleryData().observe(this, data -> {
                mImageItemsList = new ArrayList<>();
                mShowButton.set(true);
                if (data != null) {
                    mImageItemsList = new ArrayList<>();
                    for (Map.Entry<String, SpecialistGalleryImageItem> entry : data.entrySet()) {
                        mImageItemsList.add(entry.getValue());
                    }
                    mImageAdapter.setData(mImageItemsList);
                }
            });
        }
    }

    private void initRecyclerView() {
        setupList();
        RecyclerView imageRecyclerView = mBinding.specialistStorageRecyclerView;
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imageRecyclerView.setAdapter(mImageAdapter);
    }

    private void addNewImage() {
        if(mImageItemsList != null && mImageItemsList.size() >= 5){
            showToast(R.string.toast_would_you_like_to_donate);
            cooldown(mBinding.specialistStorageAddImageButton);
            return;
        }
        mUserDataViewModel.setEditableGalleryItem(new SpecialistGalleryImageItem(
                        null,
                        "image" + (mImageItemsList.size() + 1),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        performNavigation(R.id.action_specialistStorageFragment_to_specialistStorageEditFragment);
    }

    private void setupList() {
        mImageAdapter.setOnItemClickListener(this::setEditableGalleryItem);
    }

    private void setEditableGalleryItem(@Nullable View view, SpecialistGalleryImageItem editableItem) {
        mUserDataViewModel.setEditableGalleryItem(editableItem);
        performNavigation(R.id.action_specialistStorageFragment_to_specialistStorageEditFragment);
    }
}

