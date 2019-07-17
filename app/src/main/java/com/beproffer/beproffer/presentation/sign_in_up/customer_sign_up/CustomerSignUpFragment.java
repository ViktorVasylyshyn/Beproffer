package com.beproffer.beproffer.presentation.sign_in_up.customer_sign_up;

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
import com.beproffer.beproffer.data.SaveUserData;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.databinding.CustomerSignUpFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import static android.content.Context.MODE_PRIVATE;

public class CustomerSignUpFragment extends BaseFragment {

    private CustomerSignUpFragmentBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private CustomerSignUpFragmentCallback mCallback = new CustomerSignUpFragmentCallback() {
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.customer_sign_up_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);
        mBinding.customerSignUpTerms.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.customerSignUpPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void checkDataAndSignUpNewUser() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mBinding.customerSignUpEmail.getText().toString().isEmpty()
                || mBinding.customerSignUpPass.getText().toString().isEmpty()) {
            showErrorMessage(R.string.toast_error_check_fields_data);
            return;
        }
        if (mBinding.customerSignUpName.getText().toString().length() < 4) {
            showErrorMessage(R.string.error_message_wrong_name_format);
            return;
        }

        if (!mBinding.customerSignUpPass.getText().toString().equals(mBinding.customerSignUpPassConfirm.getText().toString())) {
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
            mSignUpViewModel.resetTriggers(null, true, null);
            ((MainActivity) requireActivity()).performNavigation(destinationId, null);
        });
        mSignUpViewModel.getErrorMessageId().observe(getViewLifecycleOwner(), errorMessageId -> {
            if (errorMessageId == null)
                return;
            showErrorMessage(errorMessageId);
            mSignUpViewModel.resetTriggers(null, null, true);
        });

        mSignUpViewModel.signUpNewUser(mBinding.customerSignUpEmail.getText().toString(),
                mBinding.customerSignUpPass.getText().toString(),
                mBinding.customerSignUpName.getText().toString(),
                Const.CUST,
                null);
    }

    private void showErrorMessage(int errorMessageId) {
        TextView targetTextView;
        int bottomHintIdRes = 0;
        switch (errorMessageId) {
            case R.string.error_message_exception_sign_up_weak_password:
                targetTextView = mBinding.customerSignUpPass;
                break;
            case R.string.error_message_exception_sign_up_invalid_credentials:
                targetTextView = mBinding.customerSignUpEmail;
                break;
            case R.string.error_message_exception_sign_up_collision:
                targetTextView = mBinding.customerSignUpEmail;
                break;
            case R.string.error_message_wrong_name_format:
                targetTextView = mBinding.customerSignUpName;
                bottomHintIdRes = R.string.hint_outfield_use_correct_name_format;
                break;
            case R.string.toast_error_check_fields_data:
                targetTextView = mBinding.customerSignUpEmail;
                break;
            case R.string.toast_error_check_password:
                targetTextView = mBinding.customerSignUpPassConfirm;
                break;
            default:
                targetTextView = mBinding.customerSignUpName;
                bottomHintIdRes = R.string.toast_error_has_occurred;
        }
        if (targetTextView != null) {
            targetTextView.setError(getResources().getText(errorMessageId));
            targetTextView.requestFocus();
        }
        if (bottomHintIdRes != 0) {
            mBinding.customerSignUpBottomHint.setText(bottomHintIdRes);
        }
    }

    private void openDoc(int resId){
        if(checkInternetConnection()){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
            startActivity(browserIntent);
        }else {
            showToast(R.string.toast_no_internet_connection);
        }
    }
}