package com.beproffer.beproffer.presentation.swimg;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryViewModel;
import com.beproffer.beproffer.data.browsing_history.BrowsingHistoryViewModelFactory;
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserDataFragment;
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

public class SwipeImageFragment extends BaseUserDataFragment {
    private BrowsingHistoryViewModel mBrowsingHistoryViewModel;
    private View view;

    private SwipeImageAdapter swipeImageAdapter;
    private List<SwipeImageItem> mImageItems;

    private List<String> mBrowsingHistoryUrlList;

    private HashMap<String, String> mRequestParams;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.swipe_image_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            initUserData();
        } else {
            mCurrentUserData = null;
            mBrowsingHistoryUrlList = null;
            obtainRequestParams();
        }
        /*get request params for search*/
        mImageItems = new ArrayList<>();
        swipeImageAdapter = new SwipeImageAdapter(requireActivity(), R.layout.swipe_image_item, mImageItems);
        SwipeFlingAdapterView flingImageContainer = view.findViewById(R.id.image_frame);
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
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                onCardExit(dataObject);
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
            changeSearchParams();
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
        if (mCurrentUser != null && mCurrentUserData != null) {
            searchRequestData = requireActivity().getSharedPreferences(mCurrentUserData.getUserId(), MODE_PRIVATE);
        } else {
            searchRequestData = requireActivity().getSharedPreferences("unknownUserRequest", MODE_PRIVATE);
        }
        if (searchRequestData == null) {
            showToast(R.string.toast_define_search_request);
            navigateToSearchRequestFragment();
            return;
        }
        try {
            mRequestParams = new HashMap<>();
            mRequestParams.put(Const.SERVSBTP, searchRequestData.getString(Const.SERVSBTP, null));
            mRequestParams.put(Const.SERVTYPE, searchRequestData.getString(Const.SERVTYPE, null));
            mRequestParams.put(Const.GENDER, searchRequestData.getString(Const.GENDER, null));
        } catch (NullPointerException e) {
            navigateToSearchRequestFragment();
            showToast(R.string.toast_error_search_request);
        }
        /*if search request params are not correct or equals null - we go to the search fragment to define params*/
        if (mRequestParams == null || mRequestParams.containsValue(null) || mRequestParams.size() != 3) {
            showProgress(false);
            showToast(R.string.toast_define_search_request);
            navigateToSearchRequestFragment();
        } else {
            if (mCurrentUser != null && mCurrentUserData != null) {
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

    private void navigateToSearchRequestFragment() {
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_searchFragment, null);

    }

    private void displayImageInfo(Object dataObject) {
        /*переделать через вьюмодел!!!!!*/
        ViewModelProviders.of(requireActivity()).get(ImageItemTransfer.class).setImageItem((SwipeImageItem) dataObject);
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_imageInfoDisplayFragment, null);
    }

    private void changeSearchParams() {
        showToast(R.string.toast_no_available_images);
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_searchFragment, null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
