package com.beproffer.beproffer.presentation.sign_in_up.sign_up;

import android.os.Bundle;
import android.os.Handler;
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
import com.beproffer.beproffer.databinding.FragmentSignUpBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.util.Const;

public class SignUpFragment extends BaseFragment {

    private FragmentSignUpBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private String mUserType;

    private final SignUpFragmentCallback mCallback = new SignUpFragmentCallback() {
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
            hideKeyboard(requireActivity());
        }

        @Override
        public void onShowPasswordClicked(View view) {
            EditText editText = null;
            switch (view.getId()) {
                case R.id.sign_up_fragment_show_password:
                    editText = mBinding.signUpPass;
                    break;
                case R.id.sign_up_fragment_show_password_confirm:
                    editText = mBinding.signUpPassConfirm;
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setFragmentCallback(mCallback);
        mBinding.setShowProgress(mShowProgress);

        if (this.getArguments() != null) {
            mUserType = this.getArguments().getString(Const.USERS, null);
        } else {
            showToast(R.string.toast_error_has_occurred);
            popBackStack();
        }
        if (mUserType.equals(Const.CUST) || mUserType.equals(Const.SPEC)) {
            /*никаих иных значений на фрагмент регистрации, в качестве типа пользователя, поступить не должно*/
        } else {
            showToast(R.string.toast_error_has_occurred);
            popBackStack();
        }
        if (mUserType.equals(Const.SPEC)) {
            mBinding.signUpPhone.setVisibility(View.VISIBLE);
            mBinding.signUpBottomHint.setText(R.string.hint_specialist_phone_1);
        }
    }

    private void checkDataAndSignUpNewUser() {
        hideKeyboard(requireActivity());
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (mBinding.signUpName.getText().toString().isEmpty()
                || mBinding.signUpName.getText().toString().length() < 4) {
            requestErrorFocus(mBinding.signUpName, R.string.error_message_name_is_too_short);
            return;
        }
        if (mBinding.signUpEmail.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.signUpEmail, R.string.error_message_enter_email);
            return;
        }
        if (mBinding.signUpPass.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.signUpPass, R.string.error_message_exception_sign_up_weak_password);
            return;
        }
        if (mUserType.equals(Const.SPEC)) {
            if (checkPhoneDataAccuracy()) {
                requestErrorFocus(mBinding.signUpPhone, R.string.error_message_wrong_phone_number_format);
                return;
            }
        }
        /*иметь ввиду, что Firebase разрешает использовать намного больше символьв. В тех редких случаях,
         когда пользователь будет менять пароль через отсылку на его email письма - в его пароле могут
          содержаться не толлько разрешенные этой проверкой символы, но и другие*/
        if (!mBinding.signUpPass.getText().toString().matches("[a-zA-Z0-9!@#$]+")) {
            showErrorMessage(R.string.error_message_password_allowable_symbols);
            return;
        }
        if (!mBinding.signUpPass.getText().toString().equals(mBinding.signUpPassConfirm.getText().toString())) {
            requestErrorFocus(mBinding.signUpPass, R.string.toast_error_password_confirm_failure);
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
            mSignUpViewModel.resetValues(true, false, false,
                    false, false);
            showToast(toastId);
        });
        /*popBackStack*/
        mSignUpViewModel.getPopBackStack().observe(getViewLifecycleOwner(), performPopBackStack -> {
            if (performPopBackStack == null)
                return;
            mSignUpViewModel.resetValues(false, true, false,
                    false, false);
            Handler handler = new Handler();
            handler.postDelayed(this::popBackStack, Const.POPBACKSTACK_WAITING);
        });
        /*bottom TextView*/
        mSignUpViewModel.getErrorMessageId().observe(getViewLifecycleOwner(), errorMessageId -> {
            if (errorMessageId == null)
                return;
            mSignUpViewModel.resetValues(false, false, true,
                    false, false);
            showErrorMessage(errorMessageId);
        });
        /*keyboard*/
        mSignUpViewModel.getHideKeyboard().observe(getViewLifecycleOwner(), hideKeyboard -> {
            if (hideKeyboard == null)
                return;
            mSignUpViewModel.resetValues(false, false, false,
                    true, false);
            hideKeyboard(requireActivity());
        });

        switch (mUserType) {
            case Const.CUST:
                mSignUpViewModel.signUpNewUser(mBinding.signUpEmail.getText().toString(),
                        mBinding.signUpPass.getText().toString(),
                        editInputText(mBinding.signUpName.getText().toString()),
                        mUserType,
                        null,
                        null);
                break;
            case Const.SPEC:
                mSignUpViewModel.signUpNewUser(mBinding.signUpEmail.getText().toString(),
                        mBinding.signUpPass.getText().toString(),
                        mBinding.signUpName.getText().toString(),
                        mUserType,
                        null,
                        mBinding.signUpPhone.getText().toString());
        }
    }

    private void showErrorMessage(int errorMessageId) {
        mBinding.signUpBottomHint.setText(errorMessageId);
        mBinding.signUpBottomHint.setTextColor(getResources().getColor(R.color.color_red_alpha_85));
    }

    private boolean checkPhoneDataAccuracy() {
        return 6 > mBinding.signUpPhone.length() || mBinding.signUpPhone.length() > 13;
    }
}
