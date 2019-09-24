package com.beproffer.beproffer.presentation.browsing.info;

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
import androidx.lifecycle.ViewModelProviders;

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

    private final BrowsingItemInfoFragmentCallback mCallback = new BrowsingItemInfoFragmentCallback() {
        @Override
        public void onAddContactClick() {
            addContact();
        }

        @Override
        public void onPerformBackNavigationClick() {
            popBackStack();
        }

        @Override
        public void onVoteClick() {
            handleOnVoteClick();
        }

        @Override
        public void onCallClick() {
            /*звонить могут олько зарегистрированные пользователи*/
            if (!checkUser())
                return;
            /*на случай, если приложение установлено на устройство, с которого нельзя позвонить*/
            if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                showToast(R.string.toast_telephony_not_available);
                return;
            }
            mUserDataViewModel.getSpecialistPhone(mItem.getId()).observe(getViewLifecycleOwner(), phone -> {
                if (phone == null)
                    return;
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            });
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
        mBinding.setShowProgress(mShowProgress);

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

        showProgress(false);
    }

    private void obtainImageItem() {
        ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).getBrowsingItemInfo().observe(getViewLifecycleOwner(), item -> {
            if (item != null) {
                mItem = item;
                mBinding.setImageItem(mItem);
                syncDataWithUi();
            }
        });
    }

    private void syncDataWithUi() {
        if (mItem != null && mContacts != null) {
            if (mItem.getId().equals(mCurrentUserInfo.getId())) {
                contactButtonsInactive(R.string.title_this_is_your_image);
                return;
            }

            if (mContacts != null && mContacts.containsKey(mItem.getId())) {
                contactButtonsInactive(R.string.title_contact_already_available);
                return;
            }

            if (mContacts != null && mContacts.size() > Const.CONTACTS_NUM) {
                contactButtonsInactive(R.string.title_max_num_of_contacts);
            }
        }
    }

    private void addContact() {
        if (!checkUser())
            return;

        mUserDataViewModel.addContact(mItem.getId());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkUser() {
        if (getFirebaseUser() != null || mCurrentUserInfo != null) {
            return true;
        } else {
            showBottomHint(R.string.toast_available_for_registered);
            return false;
        }
    }

    private void showBottomHint(@SuppressWarnings("SameParameterValue") int messageResId) {
        showToast(messageResId);
        mBinding.browsingImageInfoBottomHint.setText(messageResId);
        mBinding.browsingImageInfoBottomHint.setVisibility(View.VISIBLE);
    }

    private void contactButtonsInactive(int hintRes) {
        mBinding.browsingImageInfoAddToContactsButton.setBackgroundResource(R.drawable.background_rectangle_grey_alpha_85_grey_stroke);
        mBinding.browsingImageInfoCallButton.setBackgroundResource(R.drawable.background_rectangle_grey_alpha_85_grey_stroke);
        mBinding.browsingImageInfoAddToContactsButton.setText(getResources().getText(hintRes));
        mBinding.browsingImageInfoAddToContactsButton.setTextColor(getResources().getColor(R.color.color_base_text_70per));
        mBinding.browsingImageInfoCallButton.setImageResource(R.drawable.ic_contacts_inactive);
        mBinding.browsingImageInfoAddToContactsButton.setClickable(false);
        mBinding.browsingImageInfoCallButton.setClickable(false);
    }

    private void handleOnVoteClick() {
        if (!checkUser()) {
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
