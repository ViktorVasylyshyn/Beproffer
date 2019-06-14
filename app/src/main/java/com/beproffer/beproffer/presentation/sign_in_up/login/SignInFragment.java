package com.beproffer.beproffer.presentation.sign_in_up.login;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SignInFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInFragment extends BaseFragment {

    private SignInFragmentBinding mBinding;

    private SingInFragmentCallback mCallback = new SingInFragmentCallback() {

        @Override
        public void onSignInClick() {
            signIn();
        }

        @Override
        public void onCustomerSignUpClick() {
            customerSignUp();
        }

        @Override
        public void onSpecialistSignUpClick() {
            specialistSignUp();
        }

        @Override
        public void onChangePasswordClick() {
            changePassword();
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
    }

    private void customerSignUp() {
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_customerRegistrationFragment, null);
    }

    private void specialistSignUp() {
        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_specialistRegistrationFragment, null);
    }

    private void changePassword(){
        ((MainActivity)requireActivity()).performNavigation(R.id.action_global_resetPasswordFragment, null);
    }

    public void signIn() {
        showProgress(true);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(mBinding.signInFragmentEmail.getText().toString(), mBinding.signInFragmentPassword.getText()
                        .toString()).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser() != null) {
                    showProgress(false);
                    if(auth.getCurrentUser().isEmailVerified()){
                        ((MainActivity) requireActivity()).performNavigation(R.id.action_global_profileFragment, null);
                    }else {
                        auth.signOut();
                        showToast(R.string.toast_sign_in_verify_email_first);
                    }
                }
            } else {
                showProgress(false);
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

    @Override
    public void onStop() {
        super.onStop();
        showProgress(false);

    }
}
