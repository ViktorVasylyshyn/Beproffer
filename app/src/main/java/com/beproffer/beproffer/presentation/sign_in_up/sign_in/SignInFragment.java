package com.beproffer.beproffer.presentation.sign_in_up.sign_in;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
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
import com.beproffer.beproffer.presentation.swimg.SwipeImagesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInFragment extends BaseFragment {

    private SignInFragmentBinding mBinding;

    private final SingInFragmentCallback mCallback = new SingInFragmentCallback() {

        @Override
        public void onSignInClick() {
            signIn();
        }

        @Override
        public void onCustomerSignUpClick() {
            performNavigation(R.id.action_signInFragment_to_customerRegistrationFragment);
        }

        @Override
        public void onSpecialistSignUpClick() {
            performNavigation(R.id.action_signInFragment_to_specialistRegistrationFragment);
        }

        @Override
        public void onResetPasswordClick() {
            performNavigation(R.id.action_signInFragment_to_resetPasswordFragment);
        }

        @Override
        public void onTermsClick() {
            openDoc(R.string.href_terms_of_service);
        }

        @Override
        public void onPrivacyPolicyClick() {
            openDoc(R.string.href_privacy_policy);
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
            ((MainActivity) requireActivity()).popBackStack();
        }
    }

    private void signIn() {
        if (mProcessing.get()) {
            showToast(R.string.toast_processing);
            return;
        }
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }

        if (mBinding.signInFragmentEmail.getText().toString().isEmpty() ||
                mBinding.signInFragmentPassword.getText().toString().isEmpty()) {
            showToast(R.string.toast_error_login);
            return;
        }
        showProgress(true);
        processing(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(mBinding.signInFragmentEmail.getText().toString(), mBinding.signInFragmentPassword.getText()
                .toString()).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser() != null) {
                    showProgress(false);
                    processing(false);
                    hideKeyboard(requireActivity());
                    if (auth.getCurrentUser().isEmailVerified()) {
                        performNavigation(R.id.action_global_profileFragment);
                        ViewModelProviders.of(requireActivity()).get(SwipeImagesViewModel.class).refreshAdapter();
                    } else {
                        auth.signOut();
                        showToast(R.string.toast_sign_in_verify_email_first);
                    }
                }
            } else {
                showProgress(false);
                processing(false);
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

    private void openDoc(int resId) {
        if (!checkInternetConnection()) {
            showToast(R.string.toast_no_internet_connection);
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
        startActivity(browserIntent);

    }

    @Override
    public void onStop() {
        super.onStop();
        showProgress(false);
        processing(false);
    }
}
