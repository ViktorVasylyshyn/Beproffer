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
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.databinding.ImageInfoDisplayFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserDataFragment;
import com.beproffer.beproffer.presentation.swimg.ImageItemTransfer;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

public class ImageInfoDisplayFragment extends BaseUserDataFragment {

    private ImageInfoDisplayFragmentBinding mBinding;
    private SwipeImageItem mItem;

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

    private void obtainImageItem() {
        ViewModelProviders.of(requireActivity()).get(ImageItemTransfer.class).getImageItem().observe(this, item -> {
            if (item != null) {
                mItem = item;
                mBinding.setImageItem(mItem);
            }
        });
    }

    public void sendContactRequest() {
        if (mCurrentUser == null || mCurrentUserData == null) {
            showToast(R.string.toast_request_for_registered);
            return;
        }
        showProgress(true);
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(Const.SPEC)
                    .child(mItem.getUid())
                    .child(Const.REQUEST)
                    .child(mCurrentUserData.getUserId())
                    .setValue(new ContactRequestItem(mCurrentUserData.getUserId()
                            , mCurrentUserData.getUserType()
                            , mCurrentUserData.getUserName()
                            , mCurrentUserData.getUserProfileImageUrl())).addOnSuccessListener(aVoid -> {
                showToast(R.string.toast_request_sent);
                showProgress(false);
            });
        } catch (NullPointerException e) {
            showToast(R.string.toast_error_has_occurred);
        }
        hideRequestButton();
    }

    private void hideRequestButton() {
        mBinding.imageInfoDisplaySendContactRequestImage.setVisibility(View.GONE);
        mBinding.imageInfoDisplayContactTextView.setVisibility(View.GONE);
    }

    public void performBackNavigation() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
