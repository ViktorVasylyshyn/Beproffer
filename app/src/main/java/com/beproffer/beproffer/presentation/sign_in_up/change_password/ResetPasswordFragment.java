package com.beproffer.beproffer.presentation.sign_in_up.change_password;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.ResetPasswordFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ResetPasswordFragment extends BaseFragment {

    private ResetPasswordFragmentBinding mBinding;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ResetPasswordFragmentCallback mCallback = this::changePassword;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.reset_password_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        mBinding.setFragmentCallback(mCallback);
    }

    public void changePassword() {
        if (mBinding.resetPasswordFragmentEmail.getText().toString().isEmpty()) {
            mBinding.resetPasswordFragmentEmail.setError(getResources().getText(R.string.error_message_enter_email));
            mBinding.resetPasswordFragmentEmail.requestFocus();
            return;
        }
        showProgress(true);
        mAuth.sendPasswordResetEmail(mBinding.resetPasswordFragmentEmail.getText().toString()).
                addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast(R.string.toast_reset_password_check_your_email);
                showProgress(false);

            } else {
                try {
                   throw task.getException();
                } catch (FirebaseAuthInvalidUserException invalidUser) {
                    mBinding.resetPasswordFragmentEmail.setError(getResources().
                            getText(R.string.error_message_exception_sign_in_invalid_user));
                    mBinding.resetPasswordFragmentEmail.requestFocus();
                    showProgress(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(R.string.toast_error_has_occurred);
                    showProgress(false);
                }
                showProgress(false);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        showProgress(false);
    }
}
