package com.beproffer.beproffer.presentation.swimg;

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
import com.beproffer.beproffer.data.models.SwipeImageItem;
import com.beproffer.beproffer.databinding.SwipeImageFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.presentation.swimg.adapter.SwipeImageAdapter;
import com.beproffer.beproffer.presentation.swimg.search_sheet.SearchSheetDialog;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.List;

public class SwipeImageFragment extends BaseUserInfoFragment {

    private SwipeImageFragmentBinding mBinding;

    private SwipeImageAdapter mSwipeImageAdapter;

    private List<SwipeImageItem> mImageItemsList;

    private SwipeImagesViewModel mSwipeImagesViewModel;

    private SwipeImageFragmentCallback mCallback = this::searchSheet;

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
        mBinding.setSearch(mCallback);

        if (getFirebaseUser() != null) {
            if (mImageItemsList == null) {
                initUserData();
                return;
            }
            if (!mImageItemsList.isEmpty())
                initAdapter();
        } else {
            if (mImageItemsList == null) {
                connectToRepository();
                return;
            }
            if (!mImageItemsList.isEmpty()) {
                initAdapter();
            }
        }
    }

    @Override
    public void applyUserData() {
        connectToRepository();
    }

    private void connectToRepository() {
        if (!checkInternetConnection()) {
            /*сделать здесь переход, на какой нить фрагмент*/
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if (mSwipeImagesViewModel == null) {
            mSwipeImagesViewModel = ViewModelProviders.of(requireActivity()).get(SwipeImagesViewModel.class);
        }
        /*получения списка объектов для отображения*/
        mSwipeImagesViewModel.getSwipeImageItemsList().observe(getViewLifecycleOwner(), list -> {
            if (list == null)
                return;
            mImageItemsList = list;
            if (mSwipeImageAdapter == null) {
                initAdapter();
            } else {
                mSwipeImageAdapter.notifyDataSetChanged();
            }
        });
        /*актив\неактив прогресс бар*/
        mSwipeImagesViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null)
                showProgress(progress);
        });
        /*показ тостов*/
        mSwipeImagesViewModel.getShowToast().observe(getViewLifecycleOwner(), resId -> {
            if (resId == null)
                return;
            showToast(resId);
            mSwipeImagesViewModel.resetTriggers(true, null);
        });
        /*получение команд и айди для совершения перехода*/
        mSwipeImagesViewModel.getPerformNavigation().observe(getViewLifecycleOwner(), performSearch -> {
            if (performSearch == null)
                return;
            mSwipeImagesViewModel.resetTriggers(null, true);
            searchSheet();
        });

    }

    private void initAdapter() {
        mSwipeImageAdapter = new SwipeImageAdapter(requireActivity(), R.layout.swipe_image_item, mImageItemsList);
        SwipeFlingAdapterView flingImageContainer = mBinding.imageFrame;
        flingImageContainer.setAdapter(mSwipeImageAdapter);

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
        flingImageContainer.setOnItemClickListener((itemPosition, dataObject) -> displayImageInfo(dataObject));
        /*попробовать здесь прописать анимацию для изображения. чтобы пользователь, войдя, понял
         * что это изображение нужно сбросить*/
    }

    private void onCardExit(Object imageItem) {
        mSwipeImagesViewModel.deleteObservedImageItem((SwipeImageItem) imageItem);
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

    private void displayImageInfo(Object dataObject) {
        ViewModelProviders.of(requireActivity()).get(ImageItemTransferViewModel.class).setImageItem((SwipeImageItem) dataObject);
        performNavigation(R.id.action_swipeImageFragment_to_imageInfoDisplayFragment);
    }

    private void searchSheet() {
        SearchSheetDialog searchSheet = new SearchSheetDialog();
        searchSheet.show(requireActivity().getSupportFragmentManager(), "searchSheet");
    }


    @Override
    public void onPause() {
        super.onPause();
    }
}
