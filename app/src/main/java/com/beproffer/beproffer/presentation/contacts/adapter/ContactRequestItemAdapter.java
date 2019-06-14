package com.beproffer.beproffer.presentation.contacts.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.databinding.ContactRequestItemBinding;

import java.util.Collections;
import java.util.List;

public class ContactRequestItemAdapter extends RecyclerView.Adapter<ContactRequestItemAdapter.ContactRequestItemViewHolder> {

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
        contactRequestItemViewHolder.bindData(mContactRequestItemList.get(i));

    }

    @Override
    public int getItemCount() {
        return mContactRequestItemList.size();
    }

    public ContactRequestItem getContactRequestAt(int position){
        return mContactRequestItemList.get(position);
    }

    static class ContactRequestItemViewHolder extends RecyclerView.ViewHolder {

        private ContactRequestItemBinding mBinding;


        ContactRequestItemViewHolder(ContactRequestItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final ContactRequestItem contactRequestItem) {
            mBinding.setItem(contactRequestItem);
        }
    }
}