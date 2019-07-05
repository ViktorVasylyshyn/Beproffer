package com.beproffer.beproffer.presentation.swimg;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryViewModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryViewModelFactory;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.databinding.SwipeImageFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.swimg.adapter.SwipeImageAdapter;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SwipeImageFragment extends BaseUserInfoFragment {

    private SwipeImageFragmentBinding mBinding;

    private BrowsingHistoryViewModel mBrowsingHistoryViewModel;

    private SwipeImageAdapter swipeImageAdapter;
    private List<SwipeImageItem> mImageItems;

    private List<String> mBrowsingHistoryUrlList;

    private HashMap<String, String> mRequestParams;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.swipe_image_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            initUserData();
        } else {
            mCurrentUserInfo = null;
            mBrowsingHistoryUrlList = null;
            obtainRequestParams();
        }
        /*get request params for search*/
        if (mImageItems == null)
            mImageItems = new ArrayList<>();
        swipeImageAdapter = new SwipeImageAdapter(requireActivity(), R.layout.swipe_image_item, mImageItems);
        SwipeFlingAdapterView flingImageContainer = mBinding.imageFrame;
        flingImageContainer.setAdapter(swipeImageAdapter);

        flingImageContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                mImageItems.remove(0);
                swipeImageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                onCardExit(dataObject);
                onCardExitAnim(false);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                onCardExit(dataObject);
                onCardExitAnim(true);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }

        });
        flingImageContainer.setOnItemClickListener((itemPosition, dataObject) -> displayImageInfo(dataObject));
    }

    private void onCardExit(Object dataObject) {
        SwipeImageItem imageItem = (SwipeImageItem) dataObject;
        if (mCurrentUser != null && mBrowsingHistoryViewModel != null) {
            mBrowsingHistoryViewModel.insert(new BrowsingHistoryModel(imageItem.getUrl(), imageItem.getType()));
            mBrowsingHistoryUrlList.add(imageItem.getUrl());
        }
        if (mImageItems.size() == 3) {
            getImagesToAdapter();
        }
        if (mImageItems.isEmpty()) {
            showToast(R.string.toast_no_available_images);
            navigateTo(R.id.action_global_searchFragment);
        }
    }

    @Override
    public void applyUserData() {
        obtainRequestParams();
    }

    public void getImagesToAdapter() {
        try {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.SERVICES)
                    .child(mRequestParams.get(Const.SERVTYPE))
                    .child(mRequestParams.get(Const.SERVSBTP))
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (mImageItems.size() > 10) {
                                return;
                            }
                            applyImageData(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            showProgress(false);
        } catch (NullPointerException exception) {
            showToast(R.string.toast_error_has_occurred);
        }

    }

    private void applyImageData(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() < 0) {
            return;
        }
        for (DataSnapshot snapshotItem : dataSnapshot.getChildren()) {
            if (mCurrentUser != null) {
                segregateImageForRegistered(snapshotItem);
            } else {
                segregateImageDataForNotRegistered(snapshotItem);
            }
        }
    }

    private void segregateImageForRegistered(DataSnapshot snapshotItem) throws NullPointerException {

        if (mRequestParams.get(Const.GENDER).equals(Const.ALLGEND)) {
            if (!mBrowsingHistoryUrlList.contains(snapshotItem.child(Const.SERIMGURL).getValue().toString())) {
                addImageToAdapter(snapshotItem);
            }
        } else {
            if (!mBrowsingHistoryUrlList.contains(snapshotItem.child(Const.SERIMGURL).getValue().toString()) &&
                    snapshotItem.child(Const.GENDER).getValue().toString().equals(mRequestParams.get(Const.GENDER))) {

                addImageToAdapter(snapshotItem);
            }
        }
    }

    private void segregateImageDataForNotRegistered(DataSnapshot snapshotItem) throws NullPointerException {
        if (mRequestParams.get(Const.GENDER).equals(Const.ALLGEND)) {
            addImageToAdapter(snapshotItem);
        } else {
            if (snapshotItem.child(Const.GENDER).getValue().toString().equals(mRequestParams.get(Const.GENDER))) {
                addImageToAdapter(snapshotItem);
            }
        }
    }

    private void addImageToAdapter(DataSnapshot snapshotItem) {
        SwipeImageItem imageItem = snapshotItem.getValue(SwipeImageItem.class);
        if (!mImageItems.contains(imageItem)) {
            mImageItems.add(imageItem);
            swipeImageAdapter.notifyDataSetChanged();
        }
    }

    private void obtainRequestParams() {
        SharedPreferences searchRequestData;
        if (mCurrentUser != null && mCurrentUserInfo != null) {
            searchRequestData = requireActivity().getSharedPreferences(mCurrentUserInfo.getUserId(), MODE_PRIVATE);
        } else {
            searchRequestData = requireActivity().getSharedPreferences("unknownUserRequest", MODE_PRIVATE);
        }
        if (searchRequestData == null) {
            showToast(R.string.toast_define_search_request);
            navigateTo(R.id.action_global_searchFragment);
            return;
        }
        try {
            mRequestParams = new HashMap<>();
            mRequestParams.put(Const.SERVSBTP, searchRequestData.getString(Const.SERVSBTP, null));
            mRequestParams.put(Const.SERVTYPE, searchRequestData.getString(Const.SERVTYPE, null));
            mRequestParams.put(Const.GENDER, searchRequestData.getString(Const.GENDER, null));
        } catch (NullPointerException e) {
            navigateTo(R.id.action_global_searchFragment);
            showToast(R.string.toast_error_search_request);
        }
        /*if search request params are not correct or equals null - we go to the search fragment to define params*/
        if (mRequestParams == null || mRequestParams.containsValue(null) || mRequestParams.size() != 3) {
            showProgress(false);
            showToast(R.string.toast_define_search_request);
            navigateTo(R.id.action_global_searchFragment);
        } else {
            if (mCurrentUser != null && mCurrentUserInfo != null) {
                /*obtaining browsing history via viewmodel from room local db if user is authed*/
                showProgress(true);
                mBrowsingHistoryViewModel = ViewModelProviders.of(requireActivity(),
                        new BrowsingHistoryViewModelFactory(requireActivity().getApplication(),
                                searchRequestData.getString(Const.SERVTYPE, null)))
                        .get(BrowsingHistoryViewModel.class);
                mBrowsingHistoryViewModel.getTargetBrowsingHistory().observe(this, browsingHistoryList -> {
                    if (null != browsingHistoryList && mBrowsingHistoryUrlList == null) {
                        mBrowsingHistoryUrlList = browsingHistoryList;
                        getImagesToAdapter();
                    }
                });
            } else {
                getImagesToAdapter();
            }
        }
    }

    private void displayImageInfo(Object dataObject) {
        ViewModelProviders.of(requireActivity()).get(ImageItemTransfer.class).setImageItem((SwipeImageItem) dataObject);
        navigateTo(R.id.action_swipeImageFragment_to_imageInfoDisplayFragment);
    }

    private void navigateTo(int destinationId){
        ((MainActivity) requireActivity()).performNavigation(destinationId, null);
    }

    private void onCardExitAnim(boolean isRightExit) {
        int animId;
        ImageView image;
        if (isRightExit) {
            animId = R.anim.swipe_out_right;
            image = mBinding.swipeImageLikeImage;
        } else {
            image = mBinding.swipeImageDislikeImage;
            animId = R.anim.swipe_out_left;
        }
        try {
            Animation animation;
            animation = AnimationUtils.loadAnimation(requireContext(), animId);
            image.startAnimation(animation);
        } catch (NullPointerException e) {
            showToast(R.string.toast_error_has_occurred);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
