package com.beproffer.beproffer.presentation.contacts.confirmed;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.databinding.ContactsFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.contacts.adapter.ContactRequestItemAdapter;
import com.beproffer.beproffer.presentation.contacts.adapter.ContactsItemAdapter;
import com.beproffer.beproffer.util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends BaseUserInfoFragment {

    private final ObservableBoolean mShowNoContacts = new ObservableBoolean(false);

    private ContactsItemAdapter mContactsAdapter;

    private ContactRequestItemAdapter mCustomersRequestsAdapter;

    private RecyclerView mItemsRecyclerView;

    private List<ContactItem> mContactsList;

    private List<ContactRequestItem> mCustomerRequestsList;

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

        initUserData();
    }

    @Override
    public void applyUserData() {
        removeBadge(Const.CONTBNBINDEX);
        /*задумка такова, что специалистам будет больше интересна вкладка входящих запросов от потен-
         * циалльных клиентов, поэтому есть смысл открывать им этот фрагмент, именно на вкладке запросов.
         * а клиентам, и вовсе, доступна только вкладка контактов. так что со старта проверяем тип узера,
         * и настраиваем интерфейс согласно нему.*/
        switch (mCurrentUserInfo.getType()) {
            case Const.SPEC:
                adaptUiForSpecialist();
                obtainCustomersRequests();
                break;
            case Const.CUST:
                adaptUiForCustomer();
                obtainContacts();
                break;
        }
    }

    private void adaptUiForCustomer() {
        mBinding.contactsContacts.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
        mBinding.contactsNoContacts.setText(R.string.toast_no_any_contacts);
    }

    private void adaptUiForSpecialist() {
        mBinding.contactsContactRequests.setVisibility(View.VISIBLE);
        mBinding.contactsNoContacts.setText(R.string.toast_no_any_contact_requests);
        mBinding.contactsContacts.setOnClickListener(v -> obtainContacts());
        mBinding.contactsContactRequests.setOnClickListener(v -> obtainCustomersRequests());
    }

    private void obtainCustomersRequests() {
        mBinding.contactsContacts.setBackgroundResource(R.drawable.background_transparent);
        mBinding.contactsContactRequests.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
        mBinding.contactsContacts.setClickable(true);
        mBinding.contactsContactRequests.setClickable(false);
        hideErrorMessageView();
        mUserDataViewModel.getIncomingContactRequests().observe(this, list -> {
            if (list != null) {
                initCustomersRequestsRecyclerView();
                mCustomerRequestsList = new ArrayList<>();
                for (Map.Entry<String, ContactRequestItem> entry : list.entrySet()) {
                    mCustomerRequestsList.add(entry.getValue());
                }
                mShowNoContacts.set(list.isEmpty());
            }
            mCustomersRequestsAdapter.setData(mCustomerRequestsList);
            mCustomersRequestsAdapter.notifyDataSetChanged();
        });
    }

    private void obtainContacts() {
        if (mCurrentUserInfo.getType().equals(Const.SPEC)) {
            mBinding.contactsContacts.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
            mBinding.contactsContactRequests.setBackgroundResource(R.drawable.background_transparent);
            mBinding.contactsContacts.setClickable(false);
            mBinding.contactsContactRequests.setClickable(true);
        }
        hideErrorMessageView();
        mUserDataViewModel.getContacts().observe(this, contacts -> {
            if (contacts != null) {
                initContactsRecyclerView();
                mContactsList = new ArrayList<>();
                for (Map.Entry<String, ContactItem> entry : contacts.entrySet()) {
                    mContactsList.add(entry.getValue());
                }
                mShowNoContacts.set(contacts.isEmpty());
            }
            mContactsAdapter.setData(mContactsList);
        });
    }

    private void initContactsRecyclerView() {
        if (mContactsAdapter == null) {
            mContactsAdapter = new ContactsItemAdapter();
        }
        initContactClickListener();
        mItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mItemsRecyclerView.setAdapter(mContactsAdapter);
    }

    private void initCustomersRequestsRecyclerView() {
        if (mCustomersRequestsAdapter == null) {
            mCustomersRequestsAdapter = new ContactRequestItemAdapter();
        }
        initCustomerRequestClickListener();
        mItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mItemsRecyclerView.setAdapter(mCustomersRequestsAdapter);
    }

    private void initContactClickListener() {
        mContactsAdapter.setOnItemClickListener(this::onContactItemClick);
    }

    private void initCustomerRequestClickListener() {
        mCustomersRequestsAdapter.setOnItemClickListener(this::onCustomerRequestItemClick);
    }

    private void onContactItemClick(View view, ContactItem item) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.confirmed_contact_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.confirmed_contact_menu_call:
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getPhone())));
                    break;
                case R.id.confirmed_contact_menu_delete:
                    mUserDataViewModel.deleteContact(item);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void onCustomerRequestItemClick(View view, ContactRequestItem item) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.contact_request_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.contact_request_menu_apply:
                    handleContactRequest(item, true);
                    break;
                case R.id.contact_request_menu_deny:
                    handleContactRequest(item, false);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void handleContactRequest(ContactRequestItem handledItem, boolean confirm) {
        if(mCurrentUserInfo.getSpecialization() == null || mCurrentUserInfo.getDescription() == null){
            showToast(R.string.toast_add_personal_info_first);
            return;
        }
        mUserDataViewModel.handleIncomingContactRequest(handledItem, confirm);
    }

    @Override
    protected void showErrorMessage(int messageResId) {
        mBinding.contactsTextMessage.setVisibility(View.VISIBLE);
        mBinding.contactsTextMessage.setText(messageResId);
    }

    private void hideErrorMessageView(){
        mBinding.contactsTextMessage.setVisibility(View.GONE);
    }
}
