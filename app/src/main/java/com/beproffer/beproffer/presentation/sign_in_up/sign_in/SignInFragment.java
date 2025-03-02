package com.beproffer.beproffer.presentation.sign_in_up.sign_in;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SignInFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.browsing.BrowsingViewModel;
import com.beproffer.beproffer.presentation.profile.profile.ProfileFragment;
import com.beproffer.beproffer.presentation.sign_in_up.SignUpViewModel;
import com.beproffer.beproffer.presentation.sign_in_up.change_password.ResetPasswordFragment;
import com.beproffer.beproffer.presentation.sign_in_up.sign_up.SignUpFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInFragment extends BaseFragment {

    private SignInFragmentBinding mBinding;

    private SignUpViewModel mSignUpViewModel;

    private final SingInFragmentCallback mCallback = new SingInFragmentCallback() {

        @Override
        public void onSignInClick() {
            signIn();
        }

        @Override
        public void onCustomerSignUpClick() {
            Bundle userType = new Bundle();
            userType.putString(Const.USERS, Const.CUST);
            changeFragment(new SignUpFragment(), true, false, false, userType);
        }

        @Override
        public void onSpecialistSignUpClick() {
            Bundle userType = new Bundle();
            userType.putString(Const.USERS, Const.SPEC);
            changeFragment(new SignUpFragment(), true, false, false, userType);
        }

        @Override
        public void onResetPasswordClick() {
            changeFragment(new ResetPasswordFragment(), true, false, false, null);
        }

        @Override
        public void onTermsClick() {
            openDoc(R.string.href_terms_of_service);
        }

        @Override
        public void onPrivacyPolicyClick() {
            openDoc(R.string.href_privacy_policy);
        }

        @Override
        public void onShowPasswordClicked(View view) {
            passwordVisibility(mBinding.signInFragmentPassword, (ImageView) view);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.sign_in_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setFragmentCallback(mCallback);
        if (getFirebaseUser() != null) {
            performOnBottomNavigationBarItemClick(R.id.bnm_images_gallery, null);
        }
        signUpRepoSync();
    }

    private void signIn() {
        hideErrorMessage();// на случай, если высвечивалось сообщение об ошибке
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if (mBinding.signInFragmentEmail.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.signInFragmentEmail, R.string.error_message_enter_email);
            return;
        }
        if (mBinding.signInFragmentPassword.getText().toString().isEmpty()) {
            requestErrorFocus(mBinding.signInFragmentPassword, R.string.error_message_exception_sign_in_invalid_credentials);
            return;
        }
        showProgress(true);
        processing(true);
        mSignUpViewModel.resetValues(false, false,false,
                false, true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(mBinding.signInFragmentEmail.getText().toString(), mBinding.signInFragmentPassword.getText()
                .toString()).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser() != null) {
                    showProgress(false);
                    processing(false);
                    mSignUpViewModel.setSignUpEmailLiveData(null);
                    hideKeyboard(requireActivity());
                    if (auth.getCurrentUser().isEmailVerified()) {
                        changeFragment(new ProfileFragment(), true, true, false, null);
                        ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).refreshAdapter();
                    } else {
                        auth.signOut();
                        showErrorMessage(R.string.toast_sign_in_verify_email_first);
                    }
                }
            } else {
                showProgress(false);
                processing(false);
                if (task.getException() == null)
                    return;
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException weakPassword) {
                    mBinding.signInFragmentEmail.setError(getResources().getText(R.string.error_message_exception_sign_in_invalid_user));
                    mBinding.signInFragmentEmail.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException invalidCredential) {
                    mBinding.signInFragmentPassword.setError(getResources().getText(R.string.error_message_exception_sign_in_invalid_credentials));
                    mBinding.signInFragmentPassword.requestFocus();
                } catch (Exception e) {
                    showToast(R.string.toast_error_login);
                }
            }
        });
    }

    /*TODO после успешной регистрации нового пользователя делается возврат с фрагмента регистрации(в
       котором приходит сообщение о том, что нужно почту подтвердить), на фрагмент аутентификации(на
        котором, по факту, после удачного регистрирования, сообщение о подтверждении пароля нужно
         отобразить). как это сделать без кучи кода?*/
    private void showErrorMessage(int textResId) {
        mBinding.signInBottomHint1.setText(textResId);
        mBinding.signInBottomHint1.setTextColor(getResources().getColor(R.color.color_red_alpha_85));
    }

    private void hideErrorMessage() {
        mBinding.signInBottomHint1.setText(getResources().getText(R.string.string_res_without_text));
        mBinding.signInBottomHint1.setTextColor(getResources().getColor(R.color.color_base_text));
    }

    private void signUpRepoSync() {
        mSignUpViewModel = ViewModelProviders.of(requireActivity()).get((SignUpViewModel.class));
        mSignUpViewModel.getVerifyEmail().observe(getViewLifecycleOwner(), verify -> {
            if (verify == null)
                return;
            if (verify) {
                showErrorMessage(R.string.toast_sign_up_email_verification);
            } else {
                hideErrorMessage();
            }
        });
        mSignUpViewModel.getSignUpEmail().observe(getViewLifecycleOwner(), email ->{
            if(email != null){
                mBinding.signInFragmentEmail.setText(email);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        showProgress(false);
        processing(false);
    }
}
