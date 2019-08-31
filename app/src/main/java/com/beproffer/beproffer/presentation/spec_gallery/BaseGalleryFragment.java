package com.beproffer.beproffer.presentation.spec_gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.databinding.SpecialistGalleryFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.spec_gallery.adapter.GalleryImageItemAdapter;

import java.util.List;

public abstract class BaseGalleryFragment extends BaseUserInfoFragment {

    final GalleryImageItemAdapter mImageAdapter = new GalleryImageItemAdapter();

    final ObservableBoolean mShowButton = new ObservableBoolean();

    List<BrowsingImageItem> mImageItemsList;

    protected SpecialistGalleryFragmentBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.specialist_gallery_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        initRecyclerView();
        return mBinding.getRoot();
    }

    private void initRecyclerView() {
        onServiceItemClicked();
        RecyclerView imageRecyclerView = mBinding.specialistGalleryRecyclerView;
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imageRecyclerView.setAdapter(mImageAdapter);
    }

    private void onServiceItemClicked() {
        mImageAdapter.setOnItemClickListener(this::showServiceInfo);
    }

    abstract void showServiceInfo(BrowsingImageItem editableItem);
}
