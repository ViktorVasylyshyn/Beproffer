package com.beproffer.beproffer.presentation.contacts.request;

import android.arch.lifecycle.ViewModelProviders;
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
import com.beproffer.beproffer.data.models.ConfirmedContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.databinding.ContactRequestsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserDataFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.ContactRequestItemAdapter;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ContactRequestsFragment extends BaseUserDataFragment {

    private ContactRequestItemAdapter mContactRequestAdapter = new ContactRequestItemAdapter();

    private List<ContactRequestItem> mContactRequestList;

    private ContactRequestsFragmentBinding mBinding;

    private RecyclerView mContactRequestRecyclerView;

    private ContactRequestFragmentCallback mCallback = this::confirmAllContacts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.contact_requests_fragment, container, false);
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
        ViewModelProviders.of(this).get(ContactRequestsViewModel.class)
                .getDataSnapshotLiveData().observe(this, dataSnapshot -> {
            if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                mContactRequestList = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    mContactRequestList.add(data.getValue(ContactRequestItem.class));
                }
                mContactRequestAdapter.setData(mContactRequestList);
            }
            showProgress(false);
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
                        denyContact(viewHolder.getAdapterPosition());
                        break;
                    case ItemTouchHelper.RIGHT:
                        confirmContact(viewHolder.getAdapterPosition());
                        break;
                    default:
                        showToast(R.string.toast_error_has_occurred);
                }
            }
        }).attachToRecyclerView(mContactRequestRecyclerView);
    }

    private void confirmContact(int position) {
        confirmContactViaFirebase(mContactRequestList.get(position).getRequestUid(),
                mContactRequestList.get(position).getRequestType());
        mContactRequestList.remove(mContactRequestAdapter.getContactRequestAt(position));
        mContactRequestAdapter.setData(mContactRequestList);
        showToast(R.string.toast_contact_confirmed);
    }

    private void denyContact(int position) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(Const.SPEC)
                .child(mCurrentUser.getUid())
                .child(Const.REQUEST)
                .child(mContactRequestList.get(position).getRequestUid()).removeValue();
        mContactRequestList.remove(mContactRequestAdapter.getContactRequestAt(position));
        mContactRequestAdapter.setData(mContactRequestList);
        showToast(R.string.toast_contact_denied);
    }

    public void confirmAllContacts() {
        showProgress(true);
        for (int i = 0; i < mContactRequestList.size(); i++) {
            confirmContactViaFirebase(mContactRequestList.get(i).getRequestUid(), mContactRequestList.get(i).getRequestType());
            mContactRequestList.remove(mContactRequestList.get(i));
        }
        mContactRequestAdapter.setData(mContactRequestList);
        showProgress(false);
    }

    private void confirmContactViaFirebase(String uid, String userType) {
        FirebaseDatabase.getInstance().getReference().child(Const.USERS).
                child(userType).
                child(uid).
                child(Const.CONTACT).
                child(mCurrentUser.getUid()).
                setValue(new ConfirmedContactItem(mCurrentUser.getUid(),
                        mCurrentUserData.getUserName(),
                        mCurrentUserData.getUserPhone(),
                        mCurrentUserData.getUserInfo(),
                        mCurrentUserData.getUserProfileImageUrl()));
        FirebaseDatabase.getInstance().getReference().child(Const.USERS).
                child(Const.SPEC).child(mCurrentUser.getUid()).
                child(Const.REQUEST).child(uid).removeValue();
    }

    private void initRecyclerView() {
        mContactRequestRecyclerView = mBinding.contactRequestsRecyclerView;
        mContactRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mContactRequestRecyclerView.setAdapter(mContactRequestAdapter);
    }
}
