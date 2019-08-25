package com.beproffer.beproffer.presentation.browsing;

import android.arch.lifecycle.ViewModelProviders;
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
import com.beproffer.beproffer.data.models.BrowsingItemRef;
import com.beproffer.beproffer.databinding.BrowsingFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.browsing.adapter.BrowsingAdapter;
import com.beproffer.beproffer.presentation.browsing.info.BrowsingItemInfoFragment;
import com.beproffer.beproffer.presentation.browsing.search_sheet.SearchSheetDialog;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.List;

import static com.beproffer.beproffer.util.OsVersionInfo.hasLollipop;


public class BrowsingFragment extends BaseUserInfoFragment {

    private BrowsingFragmentBinding mBinding;

    private BrowsingAdapter mBrowsingAdapter;

    private List<BrowsingItemRef> mImageItemsList;

    private SearchSheetDialog mSearchSheet;

    private BrowsingViewModel mBrowsingViewModel;

    private final BrowsingFragmentCallback mCallback = new BrowsingFragmentCallback() {
        @Override
        public void onSearchClick() {
            searchSheet();
        }

        @Override
        public void onRetryClicked() {
            mBinding.browsingFragmentNoInternetConnectionImage.setVisibility(View.GONE);
            mBinding.browsingFragmentRetryTextView.setVisibility(View.GONE);
            startNewSession();
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

        if (getFirebaseUser() != null && mImageItemsList == null) {

            initUserData();
            return;
        }
        connectToRepository();
        initAdapter();
    }

    @Override
    public void applyUserData() {
        setBadgeIfNeed();
        connectToRepository();
    }

    private void connectToRepository() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if (mBrowsingViewModel == null) {
            mBrowsingViewModel = ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class);
        }
        /*получения списка объектов для отображения*/
        mBrowsingViewModel.getBrowsingItemRefsList().observe(getViewLifecycleOwner(), list -> {
            if (list == null)
                return;
            mImageItemsList = list;
            if (mBrowsingAdapter == null || mImageItemsList.isEmpty()) {
                initAdapter();
            } else {
                mBrowsingAdapter.notifyDataSetChanged();
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
            mBrowsingViewModel.resetTriggers(true, null);
        });
        /*получение команд и айди для совершения перехода*/
        mBrowsingViewModel.getPerformSearch().observe(getViewLifecycleOwner(), performSearch -> {
            if (performSearch == null)
                return;
            mBrowsingViewModel.resetTriggers(null, true);
            searchSheet();
        });

    }

    private void initAdapter() {
        mBrowsingAdapter = null;
        int layoutId;
        /*на ос меньше api 21 не работают некоторые особенности разметки лейаута. чем больше
        делаешь радиус углов тем больше изображение этим сдвигается вцентр, тоесть углы не
        хотят обрезаться поетому на апи 19 будет без округления углов*/
        if (hasLollipop()) {
            layoutId = R.layout.browsing_item21h;
        } else {
            layoutId = R.layout.browsing_item19;
        }
        mBrowsingAdapter = new BrowsingAdapter(requireActivity(), layoutId, mImageItemsList);
        SwipeFlingAdapterView flingImageContainer = mBinding.browsingFragmentImageFrame;
        flingImageContainer.setAdapter(mBrowsingAdapter);

        flingImageContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
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
        flingImageContainer.setOnItemClickListener((itemPosition, dataObject) -> showBrowsingItemInfo(dataObject));
    }

    private void onCardExit(Object imageItem) {
        mBrowsingViewModel.deleteObservedItem((BrowsingItemRef) imageItem);
    }

    private void onCardExitAnim(boolean isRightExit) {
        int animId;
        ImageView image;
        if (isRightExit) {
            animId = R.anim.swipe_out_right;
            image = mBinding.browsingFragmentLikeImage;
        } else {
            image = mBinding.browsingFragmentDislikeImage;
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

    private void showBrowsingItemInfo(Object dataObject) {
        ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).obtainBrowsingItemDetailInfo((BrowsingItemRef) dataObject);
        changeFragment(new BrowsingItemInfoFragment(), true, false, true);
    }

    private void searchSheet() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mSearchSheet != null) {
            mSearchSheet.dismiss();
            mSearchSheet = null;
        }
        mSearchSheet = new SearchSheetDialog();
        mSearchSheet.show(requireActivity().getSupportFragmentManager(), "mSearchSheet");
    }


}
