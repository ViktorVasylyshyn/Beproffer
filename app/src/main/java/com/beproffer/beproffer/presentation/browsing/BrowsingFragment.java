package com.beproffer.beproffer.presentation.browsing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.databinding.BrowsingFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.browsing.info.BrowsingItemInfoFragment;
import com.beproffer.beproffer.presentation.browsing.search.SearchFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class BrowsingFragment extends BaseUserInfoFragment {

    private BrowsingFragmentBinding mBinding;
    private BrowsingViewModel mBrowsingViewModel;
    private MotionLayout mMotionLayout;
    private List<BrowsingItemRef> mBrowsingItemRefsList = new ArrayList<>();

    private final BrowsingFragmentCallback mCallback = new BrowsingFragmentCallback() {
        @Override
        public void onSearchClick() {
            performSearch();
        }

        @Override
        public void onRetryClicked() {
            mBinding.browsingFragmentNoInternetConnectionImage.setVisibility(View.GONE);
            mBinding.browsingFragmentRetryTextView.setVisibility(View.GONE);
            startNewSession();
        }

        @Override
        public void onShowServiceInfoClick() {
            showBrowsingItemInfo();
        }
    };

    private final TransitionAdapter mTransitionAdapter = new TransitionAdapter() {

        @Override
        public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
            switch (currentId) {
                case R.id.motion_scene_off_screen_dislike_position:
                    mMotionLayout.setProgress(0f);
                    mMotionLayout.setTransition(R.id.motion_scene_start_position, R.id.motion_scene_dislike_position);
                    if (!mBrowsingItemRefsList.isEmpty()) {
                        mBrowsingViewModel.deleteObservedItem(mBrowsingItemRefsList.get(0));
                        mBrowsingItemRefsList.remove(0);
                    }
                    motionLayoutUpdate();
                    break;
                case R.id.motion_scene_off_screen_like_position:
                    mMotionLayout.setProgress(0f);
                    mMotionLayout.setTransition(R.id.motion_scene_start_position, R.id.motion_scene_like_position);
                    if (!mBrowsingItemRefsList.isEmpty()) {
                        mBrowsingViewModel.deleteObservedItem(mBrowsingItemRefsList.get(0));
                        mBrowsingItemRefsList.remove(0);
                    }
                    motionLayoutUpdate();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.browsing_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setCallback(mCallback);

        mMotionLayout = mBinding.browsingFragmentMotionLayout;
        mMotionLayout.setTransitionListener(mTransitionAdapter);

        startNewSession();
    }

    private void startNewSession() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            mBinding.browsingFragmentRetryTextView.setVisibility(View.VISIBLE);
            mBinding.browsingFragmentNoInternetConnectionImage.setVisibility(View.VISIBLE);
            return;
        }

        if (getFirebaseUser() != null && mBrowsingItemRefsList.isEmpty()) {
            initUserData();
            return;
        }
        connectToRepository();
    }

    @Override
    public void applyUserData() {
        setBadgeIfNeed();
        connectToRepository();
    }

    private void connectToRepository() {
        if (!checkInternetConnection()) {
            startNewSession();
            return;
        }

        if (mBrowsingViewModel == null) {
            mBrowsingViewModel = ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class);
        }
        /*получения списка объектов для отображения*/
        mBrowsingViewModel.getBrowsingItemRefs().observe(getViewLifecycleOwner(), browsingItemRef -> {
            if (browsingItemRef == null)
                return;
            handleBrowsingItemRef(browsingItemRef);
        });
        mBrowsingViewModel.getClearRefs().observe(getViewLifecycleOwner(), clear -> {
            if (clear != null && clear) {
                mBrowsingItemRefsList.clear();
                handleBrowsingItemRef(null);
                mBrowsingViewModel.resetValues(false, false, true);
            }
        });
        /*актив\неактив прогресс бар*/
        mBrowsingViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null)
                showProgress(progress);
        });
        /*показ тостов*/
        mBrowsingViewModel.getShowToast().observe(getViewLifecycleOwner(), resId -> {
            if (resId == null)
                return;
            showToast(resId);
        });
        /*получение команд и айди для показа панели поиска*/
        mBrowsingViewModel.getShowSearchPanel().observe(getViewLifecycleOwner(), performSearch -> {
            if (performSearch != null && performSearch) {
                performSearch();
                mBrowsingViewModel.resetValues(true, false, false);
            }
        });
        /*получение команд и айди для показа сообщения*/
        mBrowsingViewModel.getShowViewMessage().observe(getViewLifecycleOwner(), textResId -> {
            if (textResId == null)
                return;
            mBinding.browsingFragmentTextMessage.setText(getResources().getText(textResId));
            mBrowsingViewModel.resetValues(false, true, false);
        });
    }

    private void handleBrowsingItemRef(@Nullable BrowsingItemRef itemRef) {
        if (itemRef == null) {
            mBinding.browsingFragmentFirstCardImage.setImageDrawable(null);
            mBinding.browsingFragmentSecondCardImage.setImageDrawable(null);
            mBinding.browsingFragmentThirdCardImage.setImageDrawable(null);
            mMotionLayout.setVisibility(View.GONE);
            return;
        }
        mBrowsingItemRefsList.add(itemRef);
        switch (mBrowsingItemRefsList.size()) {
            case 0:
                mMotionLayout.setVisibility(View.GONE);
                break;
            case 1:
                mMotionLayout.setVisibility(View.VISIBLE);
                loadImage(mBrowsingItemRefsList.get(0).getUrl(), mBinding.browsingFragmentFirstCardImage);
                break;
            case 2:
                loadImage(mBrowsingItemRefsList.get(1).getUrl(), mBinding.browsingFragmentSecondCardImage);
                break;
            case 3:
                loadImage(mBrowsingItemRefsList.get(2).getUrl(), mBinding.browsingFragmentThirdCardImage);
                break;
            default:
        }
    }

    private void motionLayoutUpdate() {
        /*TODO исследовать, насколько критично может быть, если свайп первой карты осуществился до
         *  получения изображений из базы*/
        if (mBinding.browsingFragmentSecondCardImage.getDrawable() != null) {
            mBinding.browsingFragmentFirstCardImage.setImageDrawable(
                    mBinding.browsingFragmentSecondCardImage.getDrawable());
        } else {
            if (mBrowsingItemRefsList.size() > 0) {
                loadImage(mBrowsingItemRefsList.get(0).getUrl(), mBinding.browsingFragmentFirstCardImage);
            } else {
                mBinding.browsingFragmentFirstCardImage.setImageDrawable(null);
                mMotionLayout.setVisibility(View.GONE);
                return;
            }
        }
        if (mBinding.browsingFragmentThirdCardImage.getDrawable() != null) {
            mBinding.browsingFragmentSecondCardImage.setImageDrawable(
                    mBinding.browsingFragmentThirdCardImage.getDrawable());
        } else {
            if (mBrowsingItemRefsList.size() > 1) {
                loadImage(mBrowsingItemRefsList.get(1).getUrl(), mBinding.browsingFragmentSecondCardImage);
            } else {
                mBinding.browsingFragmentSecondCardImage.setImageDrawable(null);
                return;
            }
        }
        if (mBrowsingItemRefsList.size() > 2) {
            loadImage(mBrowsingItemRefsList.get(2).getUrl(), mBinding.browsingFragmentThirdCardImage);
        } else {
            mBinding.browsingFragmentThirdCardImage.setImageDrawable(null);
        }
    }

    private void loadImage(String url, ImageView view) {
        /*TODO добавить кэш, если он не добавится по умолчанию. нужен на случай закрытия фрагмента и
            повторного в него возврата, так же и плейсхолдер. будет ли считаться, что это дровабл?*/
        Glide.with(requireContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }
    private void showBrowsingItemInfo() {
        ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).obtainBrowsingItemDetailInfo(mBrowsingItemRefsList.get(0));
        changeFragment(new BrowsingItemInfoFragment(), true, false, true, null);
    }

    private void performSearch() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        changeFragment(new SearchFragment(), true, false, true, null);
    }
}

