package com.beproffer.beproffer.presentation.sign_in_up.specialist_sign_up;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SpecialistSignUpFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.util.Const;

public class SpecialistSignUpFragment extends BaseFragment {

    private SpecialistSignUpFragmentBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private SpecialistSignUpFragmentCallback mCallback = new SpecialistSignUpFragmentCallback() {
        @Override
        public void onSignUpClick() {
            checkDataAndSignUpNewUser();
        }

        @Override
        public void onPrivacyPolicyClick() {
            openDoc(R.string.href_privacy_policy);
        }

        @Override
        public void onTermsClick() {
            openDoc(R.string.href_terms_of_service);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.specialist_sign_up_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.specialistSignUpTerms.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.specialistSignUpPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void checkDataAndSignUpNewUser() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if(mProcessing.get()){
            showToast(R.string.toast_processing);
            return;
        }
        if (mBinding.specialistSignUpEmail.getText().toString().isEmpty()
                || mBinding.specialistSignUpPass.getText().toString().isEmpty()) {
            showErrorMessage(R.string.toast_error_check_fields_data);
            return;
        }
        if (mBinding.specialistSignUpName.getText().toString().length() < 4) {
            showErrorMessage(R.string.error_message_wrong_name_format);
            return;
        }

        if (6 > mBinding.specialistSignUpPhone.length() && mBinding.specialistSignUpPhone.length() > 13) {
            showErrorMessage(R.string.error_message_wrong_phone_number_format);
            return;
        }
        if (!mBinding.specialistSignUpPass.getText().toString().equals(mBinding.specialistSignUpPassConfirm.getText().toString())) {
            showErrorMessage(R.string.toast_error_check_password);
            return;
        }

        if (mSignUpViewModel == null)
            mSignUpViewModel = ViewModelProviders.of(requireActivity()).get(SignUpViewModel.class);

        /*актив\неактив прогресс бар*/
        mSignUpViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null)
                showProgress(progress);
        });
        /*блокировать повторное нажатие на кнопку, если предыдущий запрос в процессе выполнения*/
        mSignUpViewModel.getProcessing().observe(getViewLifecycleOwner(), processing ->{
            if(processing != null)
                processing(processing);
        });
        /*показ тостов*/
        mSignUpViewModel.getToastId().observe(getViewLifecycleOwner(), toastId -> {
            if (toastId == null)
                return;
            showToast(toastId);
            mSignUpViewModel.resetTriggers(true, null, null);
        });
        /*получение команд и айди для совершения перехода*/
        mSignUpViewModel.getNavigationId().observe(getViewLifecycleOwner(), destinationId -> {
            if (destinationId == null)
                return;
            performNavigation(destinationId);
            mSignUpViewModel.resetTriggers(null, true, null);
        });
        mSignUpViewModel.getErrorMessageId().observe(getViewLifecycleOwner(), errorMessageId -> {
            if (errorMessageId == null)
                return;
            showErrorMessage(errorMessageId);
            mSignUpViewModel.resetTriggers(null, null, true);
        });

        mSignUpViewModel.signUpNewUser(mBinding.specialistSignUpEmail.getText().toString(),
                mBinding.specialistSignUpPass.getText().toString(),
                mBinding.specialistSignUpName.getText().toString(),
                Const.SPEC,
                mBinding.specialistSignUpPhone.getText().toString());
    }

    private void showErrorMessage(int errorMessageId) {
        TextView targetTextView;
        int bottomHintIdRes = 0;
        switch (errorMessageId) {
            case R.string.error_message_exception_sign_up_weak_password:
                targetTextView = mBinding.specialistSignUpPass;
                break;
            case R.string.error_message_exception_sign_up_invalid_credentials:
                targetTextView = mBinding.specialistSignUpEmail;
                break;
            case R.string.error_message_exception_sign_up_collision:
                targetTextView = mBinding.specialistSignUpEmail;
                break;
            case R.string.error_message_wrong_phone_number_format:
                targetTextView = mBinding.specialistSignUpPhone;
                bottomHintIdRes = R.string.hint_specialist_phone_1;
                break;
            case R.string.error_message_wrong_name_format:
                targetTextView = mBinding.specialistSignUpName;
                bottomHintIdRes = R.string.hint_outfield_use_correct_name_format;
                break;
            case R.string.toast_error_check_fields_data:
                targetTextView = mBinding.specialistSignUpEmail;
                break;
            case R.string.toast_error_check_password:
                targetTextView = mBinding.specialistSignUpPassConfirm;
                break;
            default:
                targetTextView = mBinding.specialistSignUpName;
                bottomHintIdRes = R.string.toast_error_has_occurred;
        }
        if (targetTextView != null) {
            targetTextView.setError(getResources().getText(errorMessageId));
            targetTextView.requestFocus();
        }
        if (bottomHintIdRes != 0) {
            mBinding.specialistSignUpBottomHint.setText(bottomHintIdRes);
        }
    }

    private void openDoc(int resId) {
        if (checkInternetConnection()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
            startActivity(browserIntent);
        } else {
            showToast(R.string.toast_no_internet_connection);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard(requireActivity());
    }
}
