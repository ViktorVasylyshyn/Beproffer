package com.beproffer.beproffer.presentation.spec_gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.util.Const;

/**
 * Показ пользователю галереи с изображениями сервисов специалиста, который находится у пользователя в контактах.
 */

public class CustomerGalleryFragment extends BaseGalleryFragment {

    private String mTargetSpecialistId = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setShowButton(mShowButton);
        mShowButton.set(false);

        if (this.getArguments() != null) {
            mTargetSpecialistId = this.getArguments().getString(Const.SPEC, null);
        }
        initUserData();
    }

    @Override
    public void applyUserData() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mTargetSpecialistId == null) {
            showToast(R.string.message_error_has_occurred);
            return;
        }

        mUserDataViewModel.getServiceItemsList(mTargetSpecialistId).observe(this, list -> {
            if (list != null) {
                mImageItemsList = list;
                mImageAdapter.setData(mImageItemsList);
            }
        });
    }

    @Override
    protected void showServiceInfo(BrowsingImageItem editableItem) {
        /*возможно, однажды разработать детальный показ сервиса пока же, почти вся информация о нем
         * помещается на объекте recycler view. так что клик по объекту не дает результатов*/
    }
}
