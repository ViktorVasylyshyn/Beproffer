package com.beproffer.beproffer.presentation.contacts.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.ConfirmedContactItem;
import com.beproffer.beproffer.databinding.ConfirmedContactItemBinding;
import com.beproffer.beproffer.util.Callback;

import java.util.Collections;
import java.util.List;

public class ConfirmedContactsItemAdapter extends RecyclerView.Adapter<ConfirmedContactsItemAdapter.ConfirmedContactsItemViewHolder> {

    @Nullable
    private Callback.OnContactClickListener mOnContactClickListener;

    private List<ConfirmedContactItem> mConfirmedContactsItemList = Collections.emptyList();

    public void setData(List<ConfirmedContactItem> mConfirmedContactsItemList) {
        this.mConfirmedContactsItemList = mConfirmedContactsItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConfirmedContactsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ConfirmedContactItemBinding binding = ConfirmedContactItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ConfirmedContactsItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmedContactsItemViewHolder contactRequestItemViewHolder, int i) {
        contactRequestItemViewHolder.bindData(mConfirmedContactsItemList.get(i), mOnContactClickListener, i);

    }

    @Override
    public int getItemCount() {
        return mConfirmedContactsItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnContactClickListener callback) {
        this.mOnContactClickListener = callback;
    }

    static class ConfirmedContactsItemViewHolder extends RecyclerView.ViewHolder {

        private ConfirmedContactItemBinding mBinding;


        ConfirmedContactsItemViewHolder(ConfirmedContactItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final ConfirmedContactItem confirmedContactItem, Callback.OnContactClickListener callback, int position) {
            mBinding.setItem(confirmedContactItem);
            mBinding.getRoot().setOnClickListener(view -> {
                if (null != callback)
                    callback.onContactClicked(mBinding.confirmedContactItemImage, confirmedContactItem, position);
            });
        }
    }
}