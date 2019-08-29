package com.beproffer.beproffer.presentation.sign_in_up.specialist_sign_up;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SpecialistSignUpFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.util.Const;

public class SpecialistSignUpFragment extends BaseFragment {

    private SpecialistSignUpFragmentBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private final SpecialistSignUpFragmentCallback mCallback = new SpecialistSignUpFragmentCallback() {
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
                case R.id.specialist_sign_up_fragment_show_password:
                    editText = mBinding.specialistSignUpPass;
                    break;
                case R.id.specialist_sign_up_fragment_show_password_confirm:
                    editText = mBinding.specialistSignUpPassConfirm;
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

    private void checkDataAndSignUpNewUser() {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (mBinding.specialistSignUpName.getText().toString().isEmpty()
                || mBinding.specialistSignUpName.getText().toString().length() < 4) {
            showErrorMessage(R.string.error_message_name_is_too_short);
            return;
        }
        if (mBinding.specialistSignUpEmail.getText().toString().isEmpty()) {
            showErrorMessage(R.string.error_message_enter_email);
            return;
        }
        if (mBinding.specialistSignUpPass.getText().toString().isEmpty()) {
            showErrorMessage(R.string.error_message_exception_sign_up_weak_password);
            return;
        }
        if (6 > mBinding.specialistSignUpPhone.length() && mBinding.specialistSignUpPhone.length() > 13) {
            showErrorMessage(R.string.error_message_wrong_phone_number_format);
            return;
        }
        if (!mBinding.specialistSignUpPass.getText().toString().equals(mBinding.specialistSignUpPassConfirm.getText().toString())) {
            showErrorMessage(R.string.toast_error_password_confirm_failure);
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
        mSignUpViewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            if (processing != null)
                processing(processing);
        });
        /*показ тостов*/
        mSignUpViewModel.getToastId().observe(getViewLifecycleOwner(), toastId -> {
            if (toastId == null)
                return;
            showToast(toastId);
        });
        /*получение команд и айди для совершения перехода*/
        mSignUpViewModel.getPopBackStack().observe(getViewLifecycleOwner(), performPopBackStack -> {
            if (performPopBackStack == null)
                return;
            popBackStack();
        });
        mSignUpViewModel.getErrorMessageId().observe(getViewLifecycleOwner(), errorMessageId -> {
            if (errorMessageId == null)
                return;
            showErrorMessage(errorMessageId);
        });

        mSignUpViewModel.signUpNewUser(mBinding.specialistSignUpEmail.getText().toString(),
                mBinding.specialistSignUpPass.getText().toString(),
                mBinding.specialistSignUpName.getText().toString(),
                Const.SPEC,
                null,
                mBinding.specialistSignUpPhone.getText().toString());
    }

    private void showErrorMessage(int errorMessageId) {
        mBinding.specialistSignUpBottomHint.setText(errorMessageId);
        mBinding.specialistSignUpBottomHint.setTextColor(getResources().getColor(R.color.color_red_alpha_85));
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
