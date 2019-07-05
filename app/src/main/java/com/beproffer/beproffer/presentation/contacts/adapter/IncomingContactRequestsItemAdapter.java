package com.beproffer.beproffer.presentation.contacts.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.beproffer.beproffer.data.models.IncomingContactRequestItem;
import com.beproffer.beproffer.databinding.ContactRequestItemBinding;

import java.util.Collections;
import java.util.List;

public class IncomingContactRequestsItemAdapter extends RecyclerView.Adapter<IncomingContactRequestsItemAdapter.IncomingContactRequestItemViewHolder> {

    private List<IncomingContactRequestItem> mIncomingContactRequestItemList = Collections.emptyList();

    public void setData(List<IncomingContactRequestItem> mIncomingContactRequestItemList) {
        this.mIncomingContactRequestItemList = mIncomingContactRequestItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncomingContactRequestItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ContactRequestItemBinding binding = ContactRequestItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new IncomingContactRequestItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomingContactRequestItemViewHolder incomingContactRequestItemViewHolder, int i) {
        incomingContactRequestItemViewHolder.bindData(mIncomingContactRequestItemList.get(i));

    }

    @Override
    public int getItemCount() {
        return mIncomingContactRequestItemList.size();
    }

    public IncomingContactRequestItem getContactRequestAt(int position){
        return mIncomingContactRequestItemList.get(position);
    }

    static class IncomingContactRequestItemViewHolder extends RecyclerView.ViewHolder {

        private ContactRequestItemBinding mBinding;


        IncomingContactRequestItemViewHolder(ContactRequestItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }


        void bindData(final IncomingContactRequestItem incomingContactRequestItem) {
            mBinding.setItem(incomingContactRequestItem);
        }
    }
}