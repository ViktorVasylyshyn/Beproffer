package com.beproffer.beproffer.presentation.swimg.info;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.databinding.ImageInfoDisplayFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.swimg.ImageItemTransferViewModel;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class ImageInfoDisplayFragment extends BaseUserInfoFragment {

    private ImageInfoDisplayFragmentBinding mBinding;
    private SwipeImageItem mItem;
    private Map<String, ContactItem> mContacts;
    private Map<String, Boolean> mOutgoingContactRequests;

    private final ImageInfoDisplayFragmentCallback mCallback = new ImageInfoDisplayFragmentCallback() {
        @Override
        public void onSendContactRequestClick() {
            sendContactRequest();
        }

        @Override
        public void onPerformBackNavigationClick() {
            performBackNavigation();
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
                R.layout.image_info_display_fragment, container, false);
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
        mUserDataViewModel.getContacts().observe(this, contactItems -> {
            if (contactItems != null) {
                mContacts = contactItems;
                syncDataWithUi();
            }
        });
        mUserDataViewModel.getOutgoingContactRequests().observe(this, contactRequests -> {
            if (contactRequests != null)
                mOutgoingContactRequests = contactRequests;
            syncDataWithUi();
        });

        showProgress(false);
    }

    private void obtainImageItem() {
        ViewModelProviders.of(requireActivity()).get(ImageItemTransferViewModel.class).getImageItem().observe(this, item -> {
            if (item != null) {
                mItem = item;
                mBinding.setImageItem(mItem);
            }
        });
    }

    private void syncDataWithUi() {
        if (mItem.getUid().equals(mCurrentUserInfo.getUserId())) {
            requestButtonIsInactive(R.string.title_this_is_your_image);
            return;
        }

        if (mOutgoingContactRequests != null && mOutgoingContactRequests.containsKey(mItem.getUid())) {
            requestButtonIsInactive(R.string.title_request_already_sent);
            mBinding.imageInfoDisplayBottomHint.setText(R.string.hint_wait_for_confirmation);
            return;
        }

        if (mContacts != null && mContacts.containsKey(mItem.getUid())) {
            requestButtonIsInactive(R.string.title_contact_already_available);
            return;
        }

        if (mContacts != null && mContacts.size() > Const.CONTACTS_NUM) {
            requestButtonIsInactive(R.string.title_max_num_of_contacts);
        }

    }

    private void sendContactRequest() {
        if (getFirebaseUser() == null || mCurrentUserInfo == null) {
            showToast(R.string.toast_request_for_registered);
            return;
        }

        mUserDataViewModel.sendContactRequest(new ContactRequestItem(mCurrentUserInfo.getUserId()
                , mCurrentUserInfo.getUserType()
                , mCurrentUserInfo.getUserName()
                , mCurrentUserInfo.getUserProfileImageUrl()), mItem.getUid());

        requestButtonIsInactive(R.string.title_request_already_sent);
    }

    private void requestButtonIsInactive(int hintRes) {
        mBinding.imageInfoDisplaySendContactRequestButton.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.imageInfoDisplaySendContactRequestButton.setText(getResources().getText(hintRes));
        mBinding.imageInfoDisplaySendContactRequestButton.setTextColor(getResources().getColor(R.color.color_base_text_70per));
        mBinding.imageInfoDisplaySendContactRequestButton.setClickable(false);
    }

    private void handleOnVoteClick() {
        if (getFirebaseUser() == null || mCurrentUserInfo == null) {
            showToast(R.string.toast_request_for_registered);
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

        PopupMenu popupMenu = new PopupMenu(requireActivity(), mBinding.imageInfoDisplayVote);
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
                .child(mCurrentUserInfo.getUserId())
                .setValue(mItem)
                .addOnSuccessListener(aVoid -> {
                    mBinding.imageInfoDisplayVoteTopHint.setText(R.string.hint_any_prohibited_content);
                    showProgress(false);
                    mProcessing.set(false);
                    mBinding.imageInfoDisplayVote.setImageResource(R.drawable.ic_vote_inactive);
                    mBinding.imageInfoDisplayVote.setClickable(false);
                }).addOnFailureListener(e -> {
            showToast(R.string.toast_error_has_occurred);
            mBinding.imageInfoDisplayVoteTopHint.setText(e.getMessage());
            showProgress(false);
            mProcessing.set(false);
        });
    }

    private void performBackNavigation() {
        performNavigation(R.id.action_global_swipeImageFragment);
    }
}
