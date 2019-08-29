package com.beproffer.beproffer.presentation.spec_gallery;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.databinding.SpecialistGalleryFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.spec_gallery.adapter.GalleryImageItemAdapter;
import com.beproffer.beproffer.presentation.spec_gallery.edit.SpecialistGalleryEditFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecialistGalleryFragment extends BaseUserInfoFragment {

    private final GalleryImageItemAdapter mImageAdapter = new GalleryImageItemAdapter();

    private final ObservableBoolean mShowButton = new ObservableBoolean();

    private List<BrowsingImageItem> mImageItemsList;

    private SpecialistGalleryFragmentBinding mBinding;

    private final SpecialistGalleryFragmentCallback mCallback = this::addNewImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.specialist_gallery_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        initRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setShowButton(mShowButton);

        initUserData();
    }

    @Override
    public void applyUserData() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mImageItemsList == null) {
            mUserDataViewModel.getSpecialistGalleryData().observe(this, data -> {
                mImageItemsList = new ArrayList<>();
                mShowButton.set(true);
                if (data != null) {
                    mImageItemsList = new ArrayList<>();
                    for (Map.Entry<String, BrowsingImageItem> entry : data.entrySet()) {
                        mImageItemsList.add(entry.getValue());
                    }
                    mImageAdapter.setData(mImageItemsList);
                }
            });
        }
    }

    private void initRecyclerView() {
        setupList();
        RecyclerView imageRecyclerView = mBinding.specialistGalleryRecyclerView;
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imageRecyclerView.setAdapter(mImageAdapter);
    }

    private void addNewImage() {
        if (mImageItemsList != null && mImageItemsList.size() >= 5) {
            showToast(R.string.toast_would_you_like_to_donate);
            cooldown(mBinding.specialistGalleryAddImageButton);
            return;
        }

        mUserDataViewModel.setEditableGalleryItem(new BrowsingImageItem(
                null,
                FirebaseDatabase.getInstance().getReference().child(Const.USERS).child(Const.SPEC)
                        .child(mCurrentUserInfo.getId()).child(Const.SERVICES).push().getKey(),
                null,
                null,
                null,
                null,
                null,
                null,
                null));
        changeFragment(new SpecialistGalleryEditFragment(), true, false, false);
    }

    private void setupList() {
        mImageAdapter.setOnItemClickListener(this::setEditableGalleryItem);
    }

    private void setEditableGalleryItem(BrowsingImageItem editableItem) {
        mUserDataViewModel.setEditableGalleryItem(editableItem);
        changeFragment(new SpecialistGalleryEditFragment(), true, false, false);
    }
}

