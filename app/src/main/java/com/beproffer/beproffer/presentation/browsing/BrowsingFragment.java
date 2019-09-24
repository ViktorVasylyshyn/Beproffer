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
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.databinding.BrowsingFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.browsing.info.BrowsingItemInfoFragment;
import com.beproffer.beproffer.presentation.browsing.search.SearchFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class BrowsingFragment extends BaseUserInfoFragment {

    final ObservableBoolean mShowSearchSign = new ObservableBoolean(false);


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
                    }
                    updateCardsSwipe();
                    break;
                case R.id.motion_scene_off_screen_like_position:
                    mMotionLayout.setProgress(0f);
                    mMotionLayout.setTransition(R.id.motion_scene_start_position, R.id.motion_scene_like_position);
                    if (!mBrowsingItemRefsList.isEmpty()) {
                        mBrowsingViewModel.deleteObservedItem(mBrowsingItemRefsList.get(0));
                    }
                    updateCardsSwipe();
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
        mBinding.setShowSearchSign(mShowSearchSign);

        mMotionLayout = mBinding.browsingFragmentMotionLayout;
        mMotionLayout.setTransitionListener(mTransitionAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        mBrowsingViewModel.getBrowsingItemRefs().observe(getViewLifecycleOwner(), browsingItemsList -> {
            if (browsingItemsList == null)
                return;
            mBrowsingItemRefsList = browsingItemsList;
            updateCardsRepo();
        });
        /*актив\неактив прогресс бар*/
        mBrowsingViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress == null)
                return;
            showProgress(progress);
            if (progress)
                mShowSearchSign.set(false);
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
                mBrowsingViewModel.resetValues(true, false);
            }
        });
        /*получение команд и айди для показа сообщения*/
        mBrowsingViewModel.getShowViewMessage().observe(getViewLifecycleOwner(), textResId -> {
            if (textResId == null)
                return;
            mBinding.browsingFragmentTextMessage.setText(getResources().getText(textResId));
            mBrowsingViewModel.resetValues(false, true);
        });
    }

    private void updateCardsRepo() {
        if (mBrowsingItemRefsList.isEmpty()) {
            mShowSearchSign.set(true);
            resetSession();
            return;
        }
        if (mBinding.browsingFragmentFirstCardImage.getDrawable() == null) {
            mMotionLayout.setVisibility(View.VISIBLE);
            mBinding.browsingFragmentImageDetailInfo.setVisibility(View.VISIBLE);
            loadImage(mBrowsingItemRefsList.get(0).getUrl(), mBinding.browsingFragmentFirstCardImage);
        }
        if (mBinding.browsingFragmentSecondCardImage.getDrawable() == null
                && mBrowsingItemRefsList.size() > 1) {
            loadImage(mBrowsingItemRefsList.get(1).getUrl(), mBinding.browsingFragmentSecondCardImage);
        }
        if (mBinding.browsingFragmentThirdCardImage.getDrawable() == null
                && mBrowsingItemRefsList.size() > 2) {
            loadImage(mBrowsingItemRefsList.get(2).getUrl(), mBinding.browsingFragmentThirdCardImage);
        }
    }

    private void resetSession() {
        mBinding.browsingFragmentFirstCardImage.setImageDrawable(null);
        mBinding.browsingFragmentSecondCardImage.setImageDrawable(null);
        mBinding.browsingFragmentThirdCardImage.setImageDrawable(null);
        mMotionLayout.setVisibility(View.GONE);
        mBinding.browsingFragmentImageDetailInfo.setVisibility(View.GONE);
    }

    private void updateCardsSwipe() {
        /*TODO исследовать, насколько критично может быть, если свайп первой карты осуществился до
         *  получения изображений из базы*/
        if (mBinding.browsingFragmentSecondCardImage.getDrawable() != null) {
            mBinding.browsingFragmentFirstCardImage.setImageDrawable(
                    mBinding.browsingFragmentSecondCardImage.getDrawable());
        } else {
            if (mBrowsingItemRefsList.size() > 0) {
                loadImage(mBrowsingItemRefsList.get(0).getUrl(), mBinding.browsingFragmentFirstCardImage);
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
        Glide.with(requireContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(view);
    }

    private void showBrowsingItemInfo() {
        if (!mBrowsingItemRefsList.isEmpty())
            ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).obtainBrowsingItemDetailInfo(mBrowsingItemRefsList.get(0));
        changeFragment(new BrowsingItemInfoFragment(), true, false, true, null);
    }

    private void performSearch() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        mShowSearchSign.set(false);
        changeFragment(new SearchFragment(), true, false, true, null);
    }
}

