package com.beproffer.beproffer.presentation.spec_gallery.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.databinding.StorageImageItemBinding;
import com.beproffer.beproffer.util.Callback;

import java.util.Collections;
import java.util.List;

public class GalleryImageItemAdapter extends RecyclerView.Adapter<GalleryImageItemAdapter.GalleryImageItemViewHolder> {

    @Nullable
    private Callback.OnImageClickListener mOnImageClickListener;

    private List<BrowsingImageItem> mBrowsingImageItemList = Collections.emptyList();

    public void setData(List<BrowsingImageItem> mBrowsingImageItemList) {
        this.mBrowsingImageItemList = mBrowsingImageItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryImageItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        StorageImageItemBinding binding = StorageImageItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new GalleryImageItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryImageItemViewHolder galleryImageItemViewHolder, int i) {
        galleryImageItemViewHolder.bindData(mBrowsingImageItemList.get(i), mOnImageClickListener);

    }

    @Override
    public int getItemCount() {
        return mBrowsingImageItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnImageClickListener callback) {
        this.mOnImageClickListener = callback;
    }

    static class GalleryImageItemViewHolder extends RecyclerView.ViewHolder {

        private final StorageImageItemBinding mBinding;


        GalleryImageItemViewHolder(StorageImageItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final BrowsingImageItem browsingImageItem, Callback.OnImageClickListener callback) {
            mBinding.setImage(browsingImageItem);
            mBinding.getRoot().setOnClickListener(view -> {
                if (null != callback)
                    callback.onImageClicked(browsingImageItem);
            });
        }
    }
}
