package com.beproffer.beproffer.presentation.contacts.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
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
//        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ContactItemBinding binding = ContactItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ContactsItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsItemViewHolder contactRequestItemViewHolder, int i) {
        contactRequestItemViewHolder.bindData(mConfirmedContactsItemList.get(i), mOnContactClickListener);

    }

    @Override
    public int getItemCount() {
        return mConfirmedContactsItemList.size();
    }

    public void setOnItemClickListener(@NonNull Callback.OnContactClickListener callback) {
        this.mOnContactClickListener = callback;
    }

    static class ContactsItemViewHolder extends RecyclerView.ViewHolder {

        private final ContactItemBinding mBinding;

        ContactsItemViewHolder(ContactItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bindData(final ContactItem contactItem, Callback.OnContactClickListener callback) {
            mBinding.setItem(contactItem);
            mBinding.getRoot().setOnClickListener(view -> {
                if (null != callback)
                    callback.onContactClicked(mBinding.contactItemImage, contactItem);
            });
        }
    }
}