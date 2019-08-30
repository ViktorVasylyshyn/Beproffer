package com.beproffer.beproffer.presentation.sign_in_up.customer_sign_up;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.CustomerSignUpFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.util.Const;

public class CustomerSignUpFragment extends BaseFragment {

    private CustomerSignUpFragmentBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private final CustomerSignUpFragmentCallback mCallback = new CustomerSignUpFragmentCallback() {
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

        @Override
        public void denySigningUp() {
            popBackStack();
        }

        @Override
        public void onShowPasswordClicked(View view) {
            EditText editText = null;
            switch (view.getId()) {
                case R.id.customer_sign_up_fragment_show_password:
                    editText = mBinding.customerSignUpPass;
                    break;
                case R.id.customer_sign_up_fragment_show_password_confirm:
                    editText = mBinding.customerSignUpPassConfirm;
                    break;
                default:
            }
            if (editText != null)
                passwordVisibility(editText, (ImageView) view);
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

    private void checkDataAndSignUpNewUser() {
        if (!checkInternetConnection()) {
            showErrorMessage(R.string.toast_no_internet_connection);
            return;
        }
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (mBinding.customerSignUpName.getText().toString().isEmpty()
                || mBinding.customerSignUpName.getText().toString().length() < 4) {
            requestErrorFocus(mBinding.customerSignUpName, R.string.error_message_name_is_too_short);
            return;
        }
        if (mBinding.customerSignUpEmail.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.customerSignUpEmail, R.string.error_message_enter_email);
            return;
        }
        if (mBinding.customerSignUpPass.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.customerSignUpPass, R.string.error_message_exception_sign_up_weak_password);
            return;
        }
        if (!mBinding.customerSignUpPass.getText().toString()
                .equals(mBinding.customerSignUpPassConfirm.getText().toString())) {
            requestErrorFocus(mBinding.customerSignUpPass, R.string.toast_error_password_confirm_failure);
            return;
        }
        if (mSignUpViewModel == null)
            mSignUpViewModel = ViewModelProviders.of(requireActivity()).get(SignUpViewModel.class);

        /*progressBar*/
        mSignUpViewModel.getShowProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null)
                showProgress(progress);
        });
        /*блокировать повторное нажатие на кнопку, если предыдущий запрос в процессе выполнения*/
        mSignUpViewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            if (processing != null)
                processing(processing);
        });
        /*Toast*/
        mSignUpViewModel.getToastId().observe(getViewLifecycleOwner(), toastId -> {
            if (toastId == null)
                return;
            mSignUpViewModel.resetValues(true, false, false, false);
            showToast(toastId);
        });
        /*popBackStack*/
        mSignUpViewModel.getPopBackStack().observe(getViewLifecycleOwner(), performPopBackStack -> {
            if (performPopBackStack == null)
                return;
            mSignUpViewModel.resetValues(false, true, false, false);
            Handler handler = new Handler();
            handler.postDelayed(this::popBackStack, Const.POPBACKSTACK_WAITING);
        });
        /*bottom TextView*/
        mSignUpViewModel.getErrorMessageId().observe(getViewLifecycleOwner(), errorMessageId -> {
            if (errorMessageId == null)
                return;
            mSignUpViewModel.resetValues(false, false, true, false);
            showErrorMessage(errorMessageId);
        });
        /*keyboard*/
        mSignUpViewModel.getHideKeyboard().observe(getViewLifecycleOwner(), hideKeyboard -> {
            if (hideKeyboard == null)
                return;
            mSignUpViewModel.resetValues(false, false, false, true);
            hideKeyboard(requireActivity());
        });

        mSignUpViewModel.signUpNewUser(mBinding.customerSignUpEmail.getText().toString(),
                mBinding.customerSignUpPass.getText().toString(),
                mBinding.customerSignUpName.getText().toString(),
                Const.CUST,
                null,
                null);
    }

    private void showErrorMessage(int errorMessageId) {
        mBinding.customerSignUpBottomHint.setText(errorMessageId);
        mBinding.customerSignUpBottomHint.setTextColor(getResources().getColor(R.color.color_red_alpha_85));
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