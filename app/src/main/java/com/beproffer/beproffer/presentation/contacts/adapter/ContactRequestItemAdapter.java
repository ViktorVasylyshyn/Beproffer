package com.beproffer.beproffer.presentation.contacts.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.databinding.ContactRequestItemBinding;
import com.beproffer.beproffer.util.Callback;

import java.util.Collections;
import java.util.List;

public class ContactRequestItemAdapter extends RecyclerView.Adapter<ContactRequestItemAdapter.ContactRequestItemViewHolder> {

    @Nullable
    private Callback.OnContactRequestClickListener mOnContactRequestClickListener;

    private List<ContactRequestItem> mContactRequestItemList = Collections.emptyList();

    public void setData(List<ContactRequestItem> mContactRequestItemList) {
        this.mContactRequestItemList = mContactRequestItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactRequestItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ContactRequestItemBinding binding = ContactRequestItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ContactRequestItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactRequestItemViewHolder contactRequestItemViewHolder, int i) {
        contactRequestItemViewHolder.bindData(mContactRequestItemList.get(i), mOnContactRequestClickListener);

    }

    @Override
    public int getItemCount() {
        return mContactRequestItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnContactRequestClickListener callback) {
        this.mOnContactRequestClickListener = callback;
    }

    static class ContactRequestItemViewHolder extends RecyclerView.ViewHolder {

        private final ContactRequestItemBinding mBinding;

        ContactRequestItemViewHolder(ContactRequestItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bindData(final ContactRequestItem contactRequestItem, Callback.OnContactRequestClickListener callback) {
            mBinding.setItem(contactRequestItem);
            mBinding.getRoot().setOnClickListener(v -> {
                if (null != callback)
                    callback.onContactRequestClicked(mBinding.contactRequestItemImage, contactRequestItem);
            });
        }
    }
}