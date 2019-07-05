package com.beproffer.beproffer.presentation.contacts.request;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.IncomingContactRequestItem;
import com.beproffer.beproffer.databinding.IncomingContactRequestsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.IncomingContactRequestsItemAdapter;

import java.util.List;

public class IncomingContactRequestsFragment extends BaseUserInfoFragment {

    private IncomingContactRequestsItemAdapter mIncomingContactRequestsAdapter = new IncomingContactRequestsItemAdapter();

    private List<IncomingContactRequestItem> mIncomingContactRequestsList;

    private IncomingContactRequestsFragmentBinding mBinding;

    private RecyclerView mIncomingContactRequestsRecyclerView;

    private IncomingContactRequestsFragmentCallback mCallback = this::confirmAllContacts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.incoming_contact_requests_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        initRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);

        initUserData();

        itemTouch();
    }

    @Override
    public void applyUserData() {
        obtainContactRequests();
    }

    private void obtainContactRequests() {
        mUserDataViewModel.getIncomingContactRequests().observe(this, list -> {
            if (list != null && !list.isEmpty()) {
                mIncomingContactRequestsList = list;
            }
            mIncomingContactRequestsAdapter.setData(mIncomingContactRequestsList);
        });
    }

    private void itemTouch() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        handleContactRequest(false, mIncomingContactRequestsList.get(viewHolder.getAdapterPosition()));
                        break;
                    case ItemTouchHelper.RIGHT:
                        handleContactRequest(true, mIncomingContactRequestsList.get(viewHolder.getAdapterPosition()));
                        break;
                    default:
                        showToast(R.string.toast_error_has_occurred);
                }
            }
        }).attachToRecyclerView(mIncomingContactRequestsRecyclerView);
    }

    private void handleContactRequest(boolean confirm, IncomingContactRequestItem handledItem){
        mUserDataViewModel.handleIncomingContactRequest(mCurrentUserInfo, handledItem, confirm);
    }

    public void confirmAllContacts() {
        for (IncomingContactRequestItem item : mIncomingContactRequestsList) {
            handleContactRequest(true, item);
        }
    }

    private void initRecyclerView() {
        mIncomingContactRequestsRecyclerView = mBinding.contactRequestsRecyclerView;
        mIncomingContactRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mIncomingContactRequestsRecyclerView.setAdapter(mIncomingContactRequestsAdapter);
    }
}
