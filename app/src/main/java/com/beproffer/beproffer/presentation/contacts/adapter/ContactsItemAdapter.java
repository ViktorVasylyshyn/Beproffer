package com.beproffer.beproffer.presentation.contacts.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.databinding.ContactItemBinding;
import com.beproffer.beproffer.util.Callback;

import java.util.Collections;
import java.util.List;

public class ContactsItemAdapter extends RecyclerView.Adapter<ContactsItemAdapter.ContactsItemViewHolder> {

    @Nullable
    private Callback.OnContactClickListener mOnContactClickListener;

    private List<ContactItem> mConfirmedContactsItemList = Collections.emptyList();

    public void setData(List<ContactItem> mConfirmedContactsItemList) {
        this.mConfirmedContactsItemList = mConfirmedContactsItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ContactItemBinding binding = ContactItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ContactsItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsItemViewHolder contactRequestItemViewHolder, int i) {
        contactRequestItemViewHolder.bindData(mConfirmedContactsItemList.get(i), mOnContactClickListener, i);

    }

    @Override
    public int getItemCount() {
        return mConfirmedContactsItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnContactClickListener callback) {
        this.mOnContactClickListener = callback;
    }

    static class ContactsItemViewHolder extends RecyclerView.ViewHolder {

        private ContactItemBinding mBinding;


        ContactsItemViewHolder(ContactItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final ContactItem contactItem, Callback.OnContactClickListener callback, int position) {
            mBinding.setItem(contactItem);
            mBinding.getRoot().setOnClickListener(view -> {
                if (null != callback)
                    callback.onContactClicked(mBinding.contactItemImage, contactItem);
            });
        }
    }
}