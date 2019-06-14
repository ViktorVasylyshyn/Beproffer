package com.beproffer.beproffer.presentation.specstorage.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.databinding.StorageImageItemBinding;
import com.beproffer.beproffer.util.Callback;

import java.util.Collections;
import java.util.List;

public class StorageImageItemAdapter extends RecyclerView.Adapter<StorageImageItemAdapter.StorageImageItemViewHolder> {

    @Nullable
    private Callback.OnImageClickListener mOnImageClickListener;

    private List<StorageImageItem> mStorageImageItemList = Collections.emptyList();

    public void setData(List<StorageImageItem> mStorageImageItemList) {
        this.mStorageImageItemList = mStorageImageItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StorageImageItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        StorageImageItemBinding binding = StorageImageItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new StorageImageItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StorageImageItemViewHolder storageImageItemViewHolder, int i) {
        storageImageItemViewHolder.bindData(mStorageImageItemList.get(i), mOnImageClickListener);

    }

    @Override
    public int getItemCount() {
        return mStorageImageItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnImageClickListener callback) {
        this.mOnImageClickListener = callback;
    }

    static class StorageImageItemViewHolder extends RecyclerView.ViewHolder {

        private StorageImageItemBinding mBinding;


        StorageImageItemViewHolder(StorageImageItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final StorageImageItem storageImageItem, Callback.OnImageClickListener callback) {
            mBinding.setImage(storageImageItem);
            mBinding.getRoot().setOnClickListener(view -> {
                if (null != callback)
                    callback.onImageClicked(mBinding.storageImageItemImage, storageImageItem);
            });
        }
    }
}
