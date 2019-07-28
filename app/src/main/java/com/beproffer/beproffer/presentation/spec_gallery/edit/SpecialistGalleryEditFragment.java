package com.beproffer.beproffer.presentation.spec_gallery.edit;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageEditFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;
import com.crashlytics.android.Crashlytics;

import java.util.Map;

public class SpecialistGalleryEditFragment extends BaseUserInfoFragment {


    private SpecialistStorageEditFragmentBinding mBinding;
    private SpecialistGalleryImageItem mUpdatedImageItem;
    private String mPrimordialItemType;
    private String mPrimordialItemSubtype;
    private Uri mResultUri;

    private SpecialistStorageEditFragmentCallback mCallback = new SpecialistStorageEditFragmentCallback() {
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
            ((MainActivity)requireActivity()).popBackStack();
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
                inflater, R.layout.specialist_storage_edit_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setFragmentCallback(mCallback);

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
        if (mBinding.specialistStorageEditPrice.getText().toString().isEmpty() && mUpdatedImageItem.getPrice() == null) {
            showToast(R.string.toast_enter_price);
            return;
        }
        if (mBinding.specialistStorageEditDescription.getText().toString().isEmpty() && mUpdatedImageItem.getDescription() == null) {
            showToast(R.string.toast_enter_description);
            return;
        }
        saveNewImageData();
    }

    public void setServiceType(View view) {
        int requiredMenuRes;
        switch (view.getId()) {
            case R.id.specialist_storage_edit_haircut_icon:
                requiredMenuRes = R.menu.menu_hair_services;
                break;
            case R.id.specialist_storage_edit_nails_icon:
                requiredMenuRes = R.menu.menu_nails_services;
                break;
            case R.id.specialist_storage_edit_makeup_icon:
                requiredMenuRes = R.menu.menu_makeup_services;
                break;
            case R.id.specialist_storage_edit_barber_icon:
                requiredMenuRes = R.menu.menu_barber_services;
                break;
            case R.id.specialist_storage_edit_tattoo_piercing_icon:
                requiredMenuRes = R.menu.menu_tattoo_services;
                break;
            case R.id.specialist_storage_edit_fitness_icon:
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
        if(mProcessing.get()){
            showToast(R.string.toast_processing);
            return;
        }
        mUserDataViewModel.updateSpecialistGallery(mUpdatedImageItem, mResultUri);
        /*сравнением тип услуги начального обьекта и измененного.проверяем, изменился ли тип услуги.
         * если изменился - удаляем данные по старому адресу*/
        if (mPrimordialItemSubtype!=null && !mPrimordialItemSubtype.equals(mUpdatedImageItem.getSubtype()))
            mUserDataViewModel.deleteNotRelevantImageData(mUpdatedImageItem, mPrimordialItemType, mPrimordialItemSubtype);
    }

    public void setServiceGender(View view) {
        String gender;
        switch (view.getId()) {
            case R.id.specialist_gallery_edit_gender_male:
                gender = Const.MALE;
                break;
            case R.id.specialist_gallery_edit_gender_female:
                gender = Const.FEMALE;
                break;
            case R.id.specialist_gallery_edit_gender_both:
                gender = Const.BOTHGEND;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mUpdatedImageItem.setGender(gender);
        mBinding.setItem(mUpdatedImageItem);
        setGenderLayoutFocus();
    }

    public void setServiceDuration(View view) {
        String duration;
        switch (view.getId()) {
            case R.id.specialist_storage_edit_duration_30_min_icon:
                duration = Const.MIN30;
                break;
            case R.id.specialist_storage_edit_duration_45_min_icon:
                duration = Const.MIN45;
                break;
            case R.id.specialist_storage_edit_duration_60_min_icon:
                duration = Const.MIN60;
                break;
            case R.id.specialist_storage_edit_duration_90_min_icon:
                duration = Const.MIN90;
                break;
            case R.id.specialist_storage_edit_duration_120_min_icon:
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
                targetView = mBinding.specialistStorageEditHaircutIcon;
                break;
            case Const.NAI:
                targetView = mBinding.specialistStorageEditNailsIcon;
                break;
            case Const.MAK:
                targetView = mBinding.specialistStorageEditMakeupIcon;
                break;
            case Const.TAT:
                targetView = mBinding.specialistStorageEditTattooPiercingIcon;
                break;
            case Const.BAR:
                targetView = mBinding.specialistStorageEditBarberIcon;
                break;
            case Const.FIT:
                targetView = mBinding.specialistStorageEditFitnessIcon;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mBinding.specialistStorageEditHaircutIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditNailsIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditMakeupIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditTattooPiercingIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditBarberIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditFitnessIcon.setBackgroundResource(R.drawable.background_transparent);

        targetView.getParent().requestChildFocus(targetView, targetView);
        targetView.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
    }

    private void setDurationScrollFocus() {
        if (mUpdatedImageItem == null || mUpdatedImageItem.getDuration() == null)
            return;
        ImageView targetView;
        switch (mUpdatedImageItem.getDuration()) {
            case Const.MIN30:
                targetView = mBinding.specialistStorageEditDuration30MinIcon;
                break;
            case Const.MIN45:
                targetView = mBinding.specialistStorageEditDuration45MinIcon;
                break;
            case Const.MIN60:
                targetView = mBinding.specialistStorageEditDuration60MinIcon;
                break;
            case Const.MIN90:
                targetView = mBinding.specialistStorageEditDuration90MinIcon;
                break;
            case Const.MIN120:
                targetView = mBinding.specialistStorageEditDuration120MinIcon;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        mBinding.specialistStorageEditDuration30MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditDuration45MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditDuration60MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditDuration90MinIcon.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistStorageEditDuration120MinIcon.setBackgroundResource(R.drawable.background_transparent);

        targetView.getParent().requestChildFocus(targetView, targetView);
        targetView.setBackgroundResource(R.drawable.button_background_green_stroke_rectangle);
    }



    public void setGenderLayoutFocus() {
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
            case Const.BOTHGEND:
                targetTextView = mBinding.specialistGalleryEditGenderBoth;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        mBinding.specialistGalleryEditGenderMale.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistGalleryEditGenderFemale.setBackgroundResource(R.drawable.background_transparent);
        mBinding.specialistGalleryEditGenderBoth.setBackgroundResource(R.drawable.background_transparent);

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
                if (mResultUri != null) {
                    mUpdatedImageItem.setUrl(mResultUri.toString());
                }
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            mBinding.setItem(mUpdatedImageItem);
        }
    }
}
