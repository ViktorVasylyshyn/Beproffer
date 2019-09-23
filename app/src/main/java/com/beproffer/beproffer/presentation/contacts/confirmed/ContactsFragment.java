package com.beproffer.beproffer.presentation.contacts.confirmed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.databinding.ContactsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.ContactsItemAdapter;
import com.beproffer.beproffer.presentation.spec_gallery.CustomerGalleryFragment;
import com.beproffer.beproffer.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends BaseUserInfoFragment {

    private final ObservableBoolean mShowNoContacts = new ObservableBoolean(false);

    private ContactsItemAdapter mContactsAdapter;

    private RecyclerView mItemsRecyclerView;

    private List<ContactItem> mContactsList = new ArrayList<>();

    private ContactsFragmentBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.contacts_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*хоть на этот фрагмент можно войти исключительно после проверки, но на всякий случай, поставлю еще одну*/
        if (getFirebaseUser() == null)
            performOnBottomNavigationBarItemClick(R.id.bnm_images_gallery, R.string.toast_available_for_registered);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setShowNoContacts(mShowNoContacts);
        mItemsRecyclerView = mBinding.contactsRecyclerView;
        initContactsRecyclerView();
        initUserData();
    }

    @Override
    public void applyUserData() {
        obtainContacts();
    }

    private void obtainContacts() {
        hideErrorMessageView();
        mUserDataViewModel.getContacts().observe(this, contacts -> {
            if (contacts != null) {
                mContactsList.clear();
                for (Map.Entry<String, ContactItem> entry : contacts.entrySet()) {
                    mContactsList.add(entry.getValue());
                }
                mContactsAdapter.notifyDataSetChanged();
                mShowNoContacts.set(contacts.isEmpty());
            }
        });
    }

    private void initContactsRecyclerView() {
        if (mContactsAdapter == null) {
            mContactsAdapter = new ContactsItemAdapter();
        }
        mItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mItemsRecyclerView.setAdapter(mContactsAdapter);
        mContactsAdapter.setOnItemClickListener(this::onContactItemClick);
        mContactsAdapter.setData(mContactsList);
    }

    private void onContactItemClick(View view, ContactItem item) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.confirmed_contact_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.confirmed_contact_menu_call:
                    /*на случай, если приложение установлено на устройство, с которого нельзя позвонить*/
                    PackageManager pm = requireActivity().getPackageManager();
                    if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getPhone())));
                    } else {
                        showToast(R.string.toast_telephony_not_available);
                    }
                    break;
                case R.id.confirmed_contact_menu_gallery:
                    Bundle specialistId = new Bundle();
                    specialistId.putString(Const.SPEC, item.getId());
                    changeFragment(new CustomerGalleryFragment(),
                            true,
                            false,
                            false,
                            specialistId);
                    break;
                case R.id.confirmed_contact_menu_delete:
                    mUserDataViewModel.deleteContact(item);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    protected void showErrorMessage(int messageResId) {
        mBinding.contactsTextMessage.setVisibility(View.VISIBLE);
        mBinding.contactsTextMessage.setText(messageResId);
    }

    private void hideErrorMessageView() {
        mBinding.contactsTextMessage.setVisibility(View.GONE);
    }
}
