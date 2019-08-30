package com.beproffer.beproffer.presentation.spec_gallery.edit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.databinding.SpecialistEditGalleryItemFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;
import com.beproffer.beproffer.util.ImageUtil;
import com.crashlytics.android.Crashlytics;

import java.util.Map;

public class SpecialistGalleryEditFragment extends BaseUserInfoFragment {


    private SpecialistEditGalleryItemFragmentBinding mBinding;
    private BrowsingImageItem mUpdatedImageItem;
    private String mPrimordialItemType;
    private String mPrimordialItemSubtype;
    private Uri mResultUri;

    private final SpecialistEditGalleryItemFragmentCallback mCallback = new SpecialistEditGalleryItemFragmentCallback() {
        @Override
        public void onTypeButtonClick(View view) {
            setServiceType(view);
        }

        @Override
        public void onGenderButtonClick(View view) {
            setServiceGender(view);
        }

        @Override
        public void onDurationButtonClick(View view) {
            setServiceDuration(view);
        }

        @Override
        public void setImage() {
            setServiceImage();
        }

        @Override
        public void confirmImageData() {
            checkServiceImageData();
        }

        @Override
        public void denyChanges() {
            popBackStack();
        }

        @Override
        public void onTermsClick() {
            if (checkInternetConnection()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.href_terms_of_service)));
                startActivity(browserIntent);
            } else {
                showToast(R.string.toast_no_internet_connection);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.specialist_edit_gallery_item_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setCallback(mCallback);

        initUserData();

        obtainStorageImageItem();
    }

    private void obtainStorageImageItem() {
        mUserDataViewModel.getEditableGalleryItem().observe(this, item -> {
            /*получаем изначальные type и subtype, чтобы потом проверить -
             * изменились ли они после изменения обьекта. если да - это значит что актуальные данные
             * о обьекте записалсь в новую ветку в бд. соответственно из старой их нужно удалить.*/
            try {
                if (item.getType() != null && item.getSubtype() != null) {
                    mPrimordialItemType = item.getType();
                    mPrimordialItemSubtype = item.getSubtype();
                }
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
            }
            mUpdatedImageItem = item;
            mBinding.setItem(mUpdatedImageItem);
            setTypeScrollFocus();
            setDurationScrollFocus();
            setGenderLayoutFocus();
        });
    }

    private void checkServiceImageData() {
        if (mUpdatedImageItem.getUrl() == null && mResultUri == null) {
            showToast(R.string.toast_select_image);
            return;
        }
        if (mUpdatedImageItem.getSubtype() == null) {
            showToast(R.string.toast_select_type);
            return;
        }
        if (mUpdatedImageItem.getGender() == null) {
            showToast(R.string.toast_determine_gender);
            return;
        }
        if (mUpdatedImageItem.getDuration() == null) {
            showToast(R.string.toast_select_duration);
            return;
        }
        if (mBinding.specialistEditGalleryItemPrice.getText().toString().isEmpty() && mUpdatedImageItem.getPrice() == null) {
            showToast(R.string.toast_enter_price);
            return;
        }
        if (mBinding.specialistEditGalleryItemDescription.getText().toString().isEmpty() && mUpdatedImageItem.getDescription() == null) {
            showToast(R.string.toast_enter_description);
            return;
        }
        saveNewImageData();
    }

    private void setServiceType(View view) {
        int requiredMenuRes;
        switch (view.getId()) {
            case R.id.specialist_edit_gallery_item_haircut_icon:
                requiredMenuRes = R.menu.menu_hair_services;
                break;
            case R.id.specialist_edit_gallery_item_nails_icon:
                requiredMenuRes = R.menu.menu_nails_services;
                break;
            case R.id.specialist_edit_gallery_item_makeup_icon:
                requiredMenuRes = R.menu.menu_makeup_services;
                break;
            case R.id.specialist_edit_gallery_item_barber_icon:
                requiredMenuRes = R.menu.menu_barber_services;
                break;
            case R.id.specialist_edit_gallery_item_tattoo_icon:
                requiredMenuRes = R.menu.menu_tattoo_services;
                break;
            case R.id.specialist_edit_gallery_item_fitness_icon:
                requiredMenuRes = R.menu.menu_fitness_services;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(requiredMenuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            Map<String, String> serviceType = new DefineServiceType(view.getContext()).setRequest(menuItem);
            mUpdatedImageItem.setType(serviceType.get(Const.SERVTYPE));
            mUpdatedImageItem.setSubtype(serviceType.get(Const.SERVSBTP));
            mBinding.setItem(mUpdatedImageItem);
            setTypeScrollFocus();
            return true;
        });
        popupMenu.show();
    }

    private void saveNewImageData() {
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        hideErrorMessage();
        mUserDataViewModel.updateSpecialistGallery(mUpdatedImageItem, mResultUri);
        /*сравнением тип услуги начального обьекта и измененного.проверяем, изменился ли тип услуги.
         * если изменился - удаляем данные по старому адресу*/
        if (mPrimordialItemSubtype != null && !mPrimordialItemSubtype.equals(mUpdatedImageItem.getSubtype()))
            mUserDataViewModel.deleteNotRelevantImageData(mUpdatedImageItem, mPrimordialItemType, mPrimordialItemSubtype);
    }

    private void setServiceGender(View view) {
        String gender;
        switch (view.getId()) {
            case R.id.specialist_gallery_edit_gender_male:
                gender = Const.MALE;
                break;
            case R.id.specialist_gallery_edit_gender_female:
                gender = Const.FEMALE;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mUpdatedImageItem.setGender(gender);
        mBinding.setItem(mUpdatedImageItem);
        setGenderLayoutFocus();
    }

    private void setServiceDuration(View view) {
        String duration;
        switch (view.getId()) {
            case R.id.specialist_edit_gallery_item_duration_30_min_icon:
                duration = Const.MIN30;
                break;
            case R.id.specialist_edit_gallery_item_duration_45_min_icon:
                duration = Const.MIN45;
                break;
            case R.id.specialist_edit_gallery_item_duration_60_min_icon:
                duration = Const.MIN60;
                break;
            case R.id.specialist_edit_gallery_item_duration_90_min_icon:
                duration = Const.MIN90;
                break;
            case R.id.specialist_edit_gallery_item_duration_120_min_icon:
                duration = Const.MIN120;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mUpdatedImageItem.setDuration(duration);
        mBinding.setItem(mUpdatedImageItem);
        setDurationScrollFocus();
    }

    /*пытался сделать через байндинг адаптер, чето ничего не получилось. так что делаю как умею.*/
    private void setTypeScrollFocus() {
        if (mUpdatedImageItem == null || mUpdatedImageItem.getType() == null)
            return;
        ImageView targetView;
        switch (mUpdatedImageItem.getType()) {
            case Const.HAI:
                targetView = mBinding.specialistEditGalleryItemHaircutIcon;
                break;
            case Const.NAI:
                targetView = mBinding.specialistEditGalleryItemNailsIcon;
                break;
            case Const.MAK:
                targetView = mBinding.specialistEditGalleryItemMakeupIcon;
                break;
            case Const.TAT:
                targetView = mBinding.specialistEditGalleryItemTattooIcon;
                break;
            case Const.BAR:
                targetView = mBinding.specialistEditGalleryItemBarberIcon;
                break;
            case Const.FIT:
                targetView = mBinding.specialistEditGalleryItemFitnessIcon;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mBinding.specialistEditGalleryItemHaircutIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemNailsIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemMakeupIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemTattooIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemBarberIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemFitnessIcon.setBackgroundResource(R.drawable.background_transparent);

        targetView.getParent().requestChildFocus(targetView, targetView);
        targetView.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
    }

    private void setDurationScrollFocus() {
        if (mUpdatedImageItem == null || mUpdatedImageItem.getDuration() == null)
            return;
        ImageView targetView;
        switch (mUpdatedImageItem.getDuration()) {
            case Const.MIN30:
                targetView = mBinding.specialistEditGalleryItemDuration30MinIcon;
                break;
            case Const.MIN45:
                targetView = mBinding.specialistEditGalleryItemDuration45MinIcon;
                break;
            case Const.MIN60:
                targetView = mBinding.specialistEditGalleryItemDuration60MinIcon;
                break;
            case Const.MIN90:
                targetView = mBinding.specialistEditGalleryItemDuration90MinIcon;
                break;
            case Const.MIN120:
                targetView = mBinding.specialistEditGalleryItemDuration120MinIcon;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        mBinding.specialistEditGalleryItemDuration30MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemDuration45MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemDuration60MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemDuration90MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistEditGalleryItemDuration120MinIcon.setBackgroundResource(R.drawable.background_transparent);

        targetView.getParent().requestChildFocus(targetView, targetView);
        targetView.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
    }


    private void setGenderLayoutFocus() {
        if (mUpdatedImageItem == null || mUpdatedImageItem.getGender() == null)
            return;
        TextView targetTextView;
        switch (mUpdatedImageItem.getGender()) {
            case Const.MALE:
                targetTextView = mBinding.specialistGalleryEditGenderMale;
                break;
            case Const.FEMALE:
                targetTextView = mBinding.specialistGalleryEditGenderFemale;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        mBinding.specialistGalleryEditGenderMale.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistGalleryEditGenderFemale.setBackgroundResource(R.drawable.background_transparent);

        targetTextView.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
    }

    private void setServiceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Const.REQUEST_CODE_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_1) {
            try {
                mResultUri = data.getData();

                // максимальный размер изображения 1500000 = 1.5 mb
                if (ImageUtil.isImageSizeCorrect(getContext(), mResultUri, 1500000)) {
                    mUpdatedImageItem.setUrl(mResultUri.toString());
                } else {
                    String toastMessage = getResources().getString(R.string.toast_image_size_error, 1.5);
                    Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }

            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            mBinding.setItem(mUpdatedImageItem);
        }
    }

    @Override
    protected void showErrorMessage(int messageResId) {
        mBinding.specialistEditGalleryItemScroll.setVisibility(View.GONE);
        mBinding.specialistEditGalleryItemTextMessage.setVisibility(View.VISIBLE);
        mBinding.specialistEditGalleryItemTextMessage.setText(messageResId);
    }

    private void hideErrorMessage() {
        mBinding.specialistEditGalleryItemTextMessage.setVisibility(View.GONE);
        mBinding.specialistEditGalleryItemScroll.setVisibility(View.VISIBLE);
    }
}
