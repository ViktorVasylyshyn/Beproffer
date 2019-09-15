package com.beproffer.beproffer.presentation.browsing.info;

import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.databinding.BrowsingImageInfoFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.browsing.BrowsingViewModel;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class BrowsingItemInfoFragment extends BaseUserInfoFragment {

    private BrowsingImageInfoFragmentBinding mBinding;
    private BrowsingImageItem mItem;
    private Map<String, ContactItem> mContacts;
    private Map<String, Boolean> mOutgoingContactRequests;

    private final BrowsingItemInfoFragmentCallback mCallback = new BrowsingItemInfoFragmentCallback() {
        @Override
        public void onSendContactRequestClick() {
            sendContactRequest();
        }

        @Override
        public void onPerformBackNavigationClick() {
            popBackStack();
        }

        @Override
        public void onVoteClick() {
            handleOnVoteClick();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.browsing_image_info_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);

        obtainImageItem();

        if (getFirebaseUser() != null)
            initUserData();
    }

    @Override
    public void applyUserData() {
        Log.d(Const.ERROR, "applyUserData");
        mUserDataViewModel.getContacts().observe(this, contactItems -> {
            Log.d(Const.ERROR, "applyUserData contactItems");
            if (contactItems != null) {
                Log.d(Const.ERROR, "applyUserData contactItems != null");
                mContacts = contactItems;
                syncDataWithUi();
            }
        });
        mUserDataViewModel.getOutgoingContactRequests().observe(this, contactRequests -> {
            Log.d(Const.ERROR, "applyUserData contactRequests");
            if (contactRequests != null) {
                Log.d(Const.ERROR, "applyUserData contactRequests != null");
                mOutgoingContactRequests = contactRequests;
                syncDataWithUi();
            }
        });

        showProgress(false);
    }

    private void obtainImageItem() {
        Log.d(Const.ERROR, "obtainImageItem");
        ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).getBrowsingItemInfo().observe(getViewLifecycleOwner(), item -> {
            Log.d(Const.ERROR, "obtainImageItem item");
            if (item != null) {
                Log.d(Const.ERROR, "obtainImageItem item != null");
                mItem = item;
                mBinding.setImageItem(mItem);
                syncDataWithUi();
            }
        });
    }

    private void syncDataWithUi() {
        Log.d(Const.ERROR, "syncDataWithUi");
        if (mItem != null && mContacts != null) {
            Log.d(Const.ERROR, "syncDataWithUi all != null");
            if (mItem.getId().equals(mCurrentUserInfo.getId())) {
                Log.d(Const.ERROR, "syncDataWithUi same password");
                requestButtonIsInactive(R.string.title_this_is_your_image);
                return;
            }

            if (mOutgoingContactRequests != null && mOutgoingContactRequests.containsKey(mItem.getId())) {
                requestButtonIsInactive(R.string.title_request_already_sent);
                mBinding.browsingImageInfoBottomHint.setText(R.string.hint_wait_for_confirmation);
                mBinding.browsingImageInfoBottomHint.setVisibility(View.VISIBLE);
                return;
            }

            if (mContacts != null && mContacts.containsKey(mItem.getId())) {
                requestButtonIsInactive(R.string.title_contact_already_available);
                return;
            }

            if (mContacts != null && mContacts.size() > Const.CONTACTS_NUM) {
                requestButtonIsInactive(R.string.title_max_num_of_contacts);
            }
        }
    }

    private void sendContactRequest() {
        if (getFirebaseUser() == null || mCurrentUserInfo == null) {
            /*TODO make message via view not via toast*/
            showToast(R.string.toast_available_for_registered);
            return;
        }

        mUserDataViewModel.sendContactRequest(mItem.getId());

        requestButtonIsInactive(R.string.title_request_already_sent);
    }

    private void requestButtonIsInactive(int hintRes) {
        mBinding.browsingImageInfoSendContactRequestButton.setBackgroundResource(R.drawable.background_rectangle_grey_alpha_85_grey_stroke);
        mBinding.browsingImageInfoSendContactRequestButton.setText(getResources().getText(hintRes));
        mBinding.browsingImageInfoSendContactRequestButton.setTextColor(getResources().getColor(R.color.color_base_text_70per));
        mBinding.browsingImageInfoSendContactRequestButton.setClickable(false);
    }

    private void handleOnVoteClick() {
        if (getFirebaseUser() == null || mCurrentUserInfo == null) {
            showToast(R.string.toast_available_for_registered);
            return;
        }
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }

        PopupMenu popupMenu = new PopupMenu(requireActivity(), mBinding.browsingImageInfoVote);
        popupMenu.getMenuInflater().inflate(R.menu.menu_vote, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_vote_prohibited_content:
                    sendVote(Const.PROHCONT);
                    break;
                case R.id.menu_vote_wrong_section:
                    sendVote(Const.WRONSECT);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void sendVote(String section) {
        mProcessing.set(true);
        showProgress(true);
        FirebaseDatabase.getInstance().getReference()
                .child(Const.SERVICES)
                .child(Const.VOTES)
                .child(section)
                .child(mCurrentUserInfo.getId())
                .child(mItem.getKey())
                .setValue(mItem)
                .addOnSuccessListener(aVoid -> {
                    mBinding.browsingImageInfoVoteTopHint.setText(R.string.hint_any_prohibited_content);
                    mBinding.browsingImageInfoVoteTopHint.setVisibility(View.VISIBLE);
                    showProgress(false);
                    mProcessing.set(false);
                    mBinding.browsingImageInfoVote.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
            showToast(R.string.toast_error_has_occurred);
            mBinding.browsingImageInfoVoteTopHint.setText(e.getMessage());
            showProgress(false);
            mProcessing.set(false);
        });
    }
}
