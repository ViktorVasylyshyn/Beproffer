package com.beproffer.beproffer.presentation.profile.profile_edit;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.ProfileEditFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseUserInfoFragment;
import com.beproffer.beproffer.util.Const;

public class ProfileEditFragment extends BaseUserInfoFragment {

    private ProfileEditFragmentBinding mBinding;

    private ObservableBoolean mIsUserSpecialist = new ObservableBoolean();

    private Uri mResultUri;

    private final ProfileEditFragmentCallback mCallback = new ProfileEditFragmentCallback() {
        @Override
        public void onSetProfileImageClick() {
            setProfileImage();
        }

        @Override
        public void onSpecialistTypeIconClick(View view) {
            defineSpecialistType(view);
        }

        @Override
        public void onCheckUserDataClick() {
            checkUserData();
        }

        @Override
        public void denyChanges() {
            popBackStack();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.profile_edit_fragment, container
                , false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setIsUserSpecialis(mIsUserSpecialist);

        initUserData();
    }

    @Override
    public void applyUserData() {
        mBinding.setUserInfo(mCurrentUserInfo);
        /*если специалист - то добавить вью, которые касаются специалиста*/
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            mIsUserSpecialist.set(true);
            showSpecialistUserType(mCurrentUserInfo.getUserSpecialistType());
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
        }
    }

    private void checkUserData() {
        if (null == mResultUri && mCurrentUserInfo.getUserProfileImageUrl() == null) {
            showToast(R.string.toast_select_image);
            mBinding.editFragmentBottomHint.setText(R.string.hint_any_image_size);
            return;
        }
        if (mBinding.editFragmentName.getText().toString().isEmpty() && mCurrentUserInfo.getUserName() == null) {
            showToast(R.string.toast_enter_name);
            return;
        }
        if (mCurrentUserInfo.getUserType().equals(Const.SPEC)) {
            if (mCurrentUserInfo.getUserPhone() != null || !mBinding.editFragmentPhone.getText().toString().isEmpty()) {
                saveUserData();
            } else {
                showToast(R.string.toast_enter_phone);
            }
        } else {
            saveUserData();
        }
    }

    private void saveUserData() {
        /*ввести указание типа специалиста, и сделать проверку на указанность. равносильно как и отображение текущего типа специалиста*/
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (mCurrentUserInfo.getUserPhone() != null && (mCurrentUserInfo.getUserPhone().length() < 5 || mCurrentUserInfo.getUserPhone().length() > 13)) {
            mBinding.editFragmentPhone.requestFocus();
            mBinding.editFragmentPhone.setError(getResources().getText(R.string.error_message_wrong_phone_number_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_specialist_phone_1);
            return;
        }
        if (mCurrentUserInfo.getUserName().length() < 4) {
            mBinding.editFragmentName.requestFocus();
            mBinding.editFragmentName.setError(getResources().getText(R.string.error_message_wrong_name_format));
            mBinding.editFragmentBottomHint.setText(R.string.hint_outfield_use_correct_name_format);
            return;
        }

        if (mCurrentUserInfo.getUserType().equals(Const.SPEC) && mCurrentUserInfo.getUserSpecialistType() == null) {
            mBinding.editFragmentBottomHint.setText(R.string.hint_choose_main_specialization);
            return;
        }

        mUserDataViewModel.updateUserInfo(mCurrentUserInfo, mResultUri);
    }

    private void showSpecialistUserType(String specialistType) {
        if (specialistType != null) {
            switch (specialistType) {
                case Const.HAI:
                    markActualSpecialistType(mBinding.editFragmentHaircutIcon);
                    break;
                case Const.NAI:
                    markActualSpecialistType(mBinding.editFragmentNailsIcon);
                    break;
                case Const.MAK:
                    markActualSpecialistType(mBinding.editFragmentMakeupIcon);
                    break;
                case Const.TAT:
                    markActualSpecialistType(mBinding.editFragmentTattooIcon);
                    break;
                case Const.BAR:
                    markActualSpecialistType(mBinding.editFragmentBarberIcon);
                    break;
                case Const.FIT:
                    markActualSpecialistType(mBinding.editFragmentFitnessIcon);
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
    }

    private void defineSpecialistType(View view) {
        String mainSpecialization;
        switch (view.getId()) {
            case R.id.edit_fragment_haircut_icon:
                mainSpecialization = Const.HAI;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_hai));
                break;
            case R.id.edit_fragment_nails_icon:
                mainSpecialization = Const.NAI;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_nai));
                break;
            case R.id.edit_fragment_makeup_icon:
                mainSpecialization = Const.MAK;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_mak));
                break;
            case R.id.edit_fragment_tattoo_icon:
                mainSpecialization = Const.TAT;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_tat));
                break;
            case R.id.edit_fragment_barber_icon:
                mainSpecialization = Const.BAR;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_bar));
                break;
            case R.id.edit_fragment_fitness_icon:
                mainSpecialization = Const.FIT;
                mBinding.editFragmentMainSpecialization.setText(getResources().getText(R.string.title_specialization_fit));
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mCurrentUserInfo.setUserSpecialistType(mainSpecialization);
        markActualSpecialistType(view);
    }

    private void markActualSpecialistType(View targetView) {
        mBinding.editFragmentHaircutIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.editFragmentNailsIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.editFragmentMakeupIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.editFragmentTattooIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.editFragmentBarberIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
        mBinding.editFragmentFitnessIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);

        targetView.setBackground(getResources().getDrawable(R.drawable.button_background_green_stroke_rectangle));
    }

    private void setProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Const.REQUEST_CODE_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_1) {
            try {
                if (data == null) {
                    return;
                }
                mResultUri = data.getData();
                mCurrentUserInfo.setUserProfileImageUrl(mResultUri.toString());
                mBinding.setUserInfo(mCurrentUserInfo);
            } catch (NullPointerException e) {
                showToast(R.string.toast_error_has_occurred);
            }
        }
    }
}