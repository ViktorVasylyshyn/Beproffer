package com.beproffer.beproffer.presentation.contacts.confirmed;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ConfirmedContactItem;
import com.beproffer.beproffer.databinding.ConfirmedContactsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserDataFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.ConfirmedContactsItemAdapter;
import com.beproffer.beproffer.util.DefineServiceType;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConfirmedContactsFragment extends BaseUserDataFragment {

    private ConfirmedContactsItemAdapter mConfirmedContactsAdapter = new ConfirmedContactsItemAdapter();

    private List<ConfirmedContactItem> mConfirmedContactsList;

    private ConfirmedContactsFragmentBinding mBinding;

    private ConfirmedContactsViewModel mConfirmedContactsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.confirmed_contacts_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        initRecyclerView();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);

        initUserData();
    }

    @Override
    public void applyUserData() {
        if (mConfirmedContactsList != null && !mConfirmedContactsList.isEmpty()) {
            return;
        }
        mConfirmedContactsViewModel = ViewModelProviders.of(this)
                .get(ConfirmedContactsViewModel.class);
        mConfirmedContactsViewModel.obtainConfirmedContactsId(mCurrentUserData);
        mConfirmedContactsViewModel.getDataSnapshotLiveData().observe(this, dataSnapshot -> {
            if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                mConfirmedContactsList = new ArrayList<>();
                for (DataSnapshot dataId : dataSnapshot.getChildren()) {
                    mConfirmedContactsList.add(dataId.getValue(ConfirmedContactItem.class));
                }
                mConfirmedContactsAdapter.setData(mConfirmedContactsList);
            }
            showProgress(false);
        });
    }

    private void initClickListener() {
        mConfirmedContactsAdapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(View view, ConfirmedContactItem item, int position) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.confirmed_contact_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.confirmed_contact_menu_call:
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getContactPhone())));
                    break;
                case R.id.confirmed_contact_menu_delete:
                    mConfirmedContactsViewModel.deleteContact(mCurrentUserData, item.getContactUid());
                    mConfirmedContactsList.remove(position);
                    mConfirmedContactsAdapter.setData(mConfirmedContactsList);
                    showToast(R.string.toast_contact_delete_access);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void initRecyclerView() {
        initClickListener();
        RecyclerView contactRequestRecyclerView = mBinding.confirmedContactsRecyclerView;
        contactRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactRequestRecyclerView.setAdapter(mConfirmedContactsAdapter);
    }
}
