package com.beproffer.beproffer.presentation.specstorage;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
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
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.UserDataViewModel;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.specstorage.adapter.GalleryImageItemAdapter;
import com.beproffer.beproffer.presentation.specstorage.edit.SpecialistGalleryEditFragmentViewModel;

import java.util.List;

public class SpecialistGalleryFragment extends BaseFragment {

    private GalleryImageItemAdapter mImageAdapter = new GalleryImageItemAdapter();

    private List<SpecialistGalleryImageItem> mImageItemsList;

    private SpecialistStorageFragmentBinding mBinding;

    private SpecialistGalleryFragmentCallback mCallback = this::addNewImage;

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

        if (mImageItemsList == null) {
            UserDataViewModel userDataViewModel = ViewModelProviders.of(requireActivity()).get(UserDataViewModel.class);
            userDataViewModel.getSpecialistGalleryData().observe(this, data -> {
                if (data != null) {
                    mImageItemsList = data;
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

    public void addNewImage() {
        if(mImageItemsList != null && mImageItemsList.size() >= 5){
            showToast(R.string.toast_would_you_like_to_donate);
            return;
        }
        ViewModelProviders.of(requireActivity()).get(SpecialistGalleryEditFragmentViewModel.class)
                .setStorageImageItem(new SpecialistGalleryImageItem(
                        null,
                        "image" + (mImageItemsList.size() + 1),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        performNavigationForEdit();
    }

    private void setupList() {
        mImageAdapter.setOnItemClickListener(this::loadImageDetails);
    }

    private void loadImageDetails(@Nullable View view, SpecialistGalleryImageItem specialistGalleryImageItem) {
        ViewModelProviders.of(requireActivity()).get(SpecialistGalleryEditFragmentViewModel.class)
                .setStorageImageItem(specialistGalleryImageItem);
        performNavigationForEdit();
    }

    private void performNavigationForEdit() {
        ((MainActivity)requireActivity()).performNavigation(R.id.action_specialistStorageFragment_to_specialistStorageEditFragment, null);
    }
}

