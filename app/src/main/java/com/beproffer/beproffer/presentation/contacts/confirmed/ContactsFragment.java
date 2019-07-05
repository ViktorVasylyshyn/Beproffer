package com.beproffer.beproffer.presentation.contacts.confirmed;

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
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.databinding.ContactsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.ContactsItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends BaseUserInfoFragment {

    private ContactsItemAdapter mContactsAdapter = new ContactsItemAdapter();

    private List<ContactItem> mContactsList;

    private ContactsFragmentBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.contacts_fragment, container, false);
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
        mUserDataViewModel.getContacts().observe(this, contacts -> {
            if (contacts != null && contacts.size() > 0) {
                for (Map.Entry<String, ContactItem> entry : contacts.entrySet()) {
                    mContactsList = new ArrayList<>();
                    mContactsList.add(entry.getValue());
                }
                mContactsAdapter.setData(mContactsList);
            }
        });
    }

    private void initClickListener() {
        mContactsAdapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(View view, ContactItem item) {

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.confirmed_contact_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.confirmed_contact_menu_call:
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getContactPhone())));
                    break;
                case R.id.confirmed_contact_menu_delete:
                    mUserDataViewModel.deleteContact(item);
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
        contactRequestRecyclerView.setAdapter(mContactsAdapter);
    }
}
