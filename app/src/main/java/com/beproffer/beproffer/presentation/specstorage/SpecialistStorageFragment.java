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
import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.specstorage.adapter.StorageImageItemAdapter;
import com.beproffer.beproffer.presentation.specstorage.edit.SpecialistStorageEditFragmentViewModel;

import java.util.List;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SpecialistStorageFragment extends BaseFragment {

    private StorageImageItemAdapter mImageAdapter = new StorageImageItemAdapter();

    private List<StorageImageItem> mImageItemsList;

    private SpecialistStorageFragmentBinding mBinding;

    private NavController mNavController;

    private SpecialistStorageFragmentCallback mCallback = this::addNewImage;

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
        showProgress(true);

        mNavController = Navigation.findNavController(requireActivity(), R.id.navigation_fragment_container);

        SpecialistStorageImageViewModel specialistStorageImageViewModel = ViewModelProviders.of(requireActivity()).get(SpecialistStorageImageViewModel.class);
        specialistStorageImageViewModel.getStorageImagesItemsList().observe(this, data -> {
            if (data != null) {
                mImageItemsList = data;
                mImageAdapter.setData(mImageItemsList);
            }
            showProgress(false);
        });

        specialistStorageImageViewModel.syncDataWithFirebase(mBinding.specialistStorageAddImageButton, mBinding.specialistStorageProgressBar);
    }

    private void initRecyclerView() {
        setupList();
        RecyclerView imageRecyclerView = mBinding.specialistStorageRecyclerView;
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imageRecyclerView.setAdapter(mImageAdapter);
    }

    public void addNewImage() {
        ViewModelProviders.of(requireActivity()).get(SpecialistStorageEditFragmentViewModel.class)
                .setStorageImageItem(new StorageImageItem(
                        null,
                        "image" + (mImageItemsList.size() + 1),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        performNavigation();
    }

    private void setupList() {
        mImageAdapter.setOnItemClickListener(this::loadImageDetails);
    }

    private void loadImageDetails(@Nullable View view, StorageImageItem storageImageItem) {
        ViewModelProviders.of(requireActivity()).get(SpecialistStorageEditFragmentViewModel.class)
                .setStorageImageItem(storageImageItem);
        performNavigation();
    }

    private void performNavigation() {
        mNavController.navigate(R.id.action_global_specialistStorageEditFragment);
    }
}

