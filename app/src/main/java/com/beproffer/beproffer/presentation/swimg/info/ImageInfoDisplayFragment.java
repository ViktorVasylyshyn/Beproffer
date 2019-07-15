package com.beproffer.beproffer.presentation.swimg.info;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.IncomingContactRequestItem;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.databinding.ImageInfoDisplayFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.swimg.ImageItemTransfer;
import com.beproffer.beproffer.util.Const;

import java.util.Map;

public class ImageInfoDisplayFragment extends BaseUserInfoFragment {

    private ImageInfoDisplayFragmentBinding mBinding;
    private SwipeImageItem mItem;
    private Map<String, ContactItem> mContacts;
    private Map<String, Boolean> mOutgoingContactRequests;

    private ImageInfoDisplayFragmentCallback mCallback = new ImageInfoDisplayFragmentCallback() {
        @Override
        public void onSendContactRequestClick() {
            sendContactRequest();
        }

        @Override
        public void onPerformBackNavigationClick() {
            performBackNavigation();
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

        initUserData();
    }

    @Override
    public void applyUserData() {
        mUserDataViewModel.getContacts().observe(this, contactItems -> {
            if(contactItems != null){
                mContacts = contactItems;
                syncDataWithUi();
            }
        });
        mUserDataViewModel.getOutgoingContactRequests().observe(this, contactRequests -> {
            if(contactRequests != null)
                mOutgoingContactRequests = contactRequests;
            syncDataWithUi();
        });

        showProgress(false);
    }

    private void obtainImageItem() {
        ViewModelProviders.of(requireActivity()).get(ImageItemTransfer.class).getImageItem().observe(this, item -> {
            if (item != null) {
                mItem = item;
                mBinding.setImageItem(mItem);
            }
        });
    }

    private void syncDataWithUi(){
        if(mOutgoingContactRequests != null && mOutgoingContactRequests.containsKey(mItem.getUid())){
            requestButtonIsInactive(R.string.title_request_already_sent);
            return;
        }
        if(mContacts != null && mContacts.containsKey(mItem.getUid())){
            requestButtonIsInactive(R.string.title_contact_already_available);
            return;
        }
        if(mContacts != null && mContacts.size() > Const.CONTACTS_NUM){
            requestButtonIsInactive(R.string.title_max_num_of_contacts);
        }

    }

    public void sendContactRequest() {
        if (mCurrentUser == null || mCurrentUserInfo == null) {
            showToast(R.string.toast_request_for_registered);
            return;
        }

        mUserDataViewModel.sendContactRequest(new IncomingContactRequestItem(mCurrentUserInfo.getUserId()
                , mCurrentUserInfo.getUserType()
                , mCurrentUserInfo.getUserName()
                , mCurrentUserInfo.getUserProfileImageUrl()), mItem.getUid());

        requestButtonIsInactive(R.string.title_request_already_sent);
    }

    private void requestButtonIsInactive(int hintRes) {
        mBinding.imageInfoDisplaySendContactRequestImage.setImageResource(R.drawable.ic_request_contact_inact);
        mBinding.imageInfoDisplaySendContactRequestImage.setClickable(false);
        mBinding.imageInfoDisplayContactTextView.setText(hintRes);
    }

    public void performBackNavigation() {
        ((MainActivity)requireActivity()).performNavigation(R.id.action_global_swipeImageFragment, null);
    }
}
