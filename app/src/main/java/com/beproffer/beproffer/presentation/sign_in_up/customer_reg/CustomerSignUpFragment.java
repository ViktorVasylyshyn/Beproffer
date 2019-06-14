package com.beproffer.beproffer.presentation.sign_in_up.customer_reg;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.SaveUserData;
import com.beproffer.beproffer.data.models.UserData;
import com.beproffer.beproffer.databinding.CustomerSignUpFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import static android.content.Context.MODE_PRIVATE;

public class CustomerSignUpFragment extends BaseFragment {

    private CustomerSignUpFragmentBinding mBinding;

    private CustomerSignUpFragmentCallback mCallback = this::createUser;

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
        FirebaseApp.initializeApp(requireActivity());
        mBinding.customerSignUpTerms.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.customerSignUpPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void createUser() {
        showProgress(true);
        if (!mBinding.customerSignUpPass.getText().toString().equals(mBinding.customerSignUpPassConfirm.getText().toString())) {
            showToast(R.string.toast_error_check_password);
            showProgress(false);
            return;
        }
        if (mBinding.customerSignUpName.getText().toString().isEmpty()
                || mBinding.customerSignUpEmail.getText().toString().isEmpty()
                || mBinding.customerSignUpPass.getText().toString().isEmpty()) {
            showToast(R.string.toast_error_check_fields_data);
            showProgress(false);
            return;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(mBinding.customerSignUpEmail.getText().toString()
                , mBinding.customerSignUpPass.getText().toString()).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                try {
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            requireContext().getSharedPreferences(currentUserId, MODE_PRIVATE).edit().putString(Const.USERTYPE,
                                    Const.CUST).apply();
                            new SaveUserData().saveUserDataToDatabase(currentUserId,
                                    new UserData(currentUserId,
                                            Const.CUST,
                                            mBinding.customerSignUpName.getText().toString(),
                                            mBinding.customerSignUpEmail.getText().toString(),
                                            null,
                                            null,
                                            null,
                                            null),
                                    requireActivity(),
                                    mShowProgress,
                                    R.id.action_global_loginFragment);
                            showToast(R.string.toast_sign_up_email_verification);
                        }
                    });
                } catch (NullPointerException e) {
                    showToast(R.string.toast_error_has_occurred);
                    showProgress(false);
                }
            } else {
                showProgress(false);
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException weakPassword) {
                    mBinding.customerSignUpPass.setError(getResources().getText(R.string.error_message_exception_sign_up_weak_password));
                    mBinding.customerSignUpPass.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException invalidCredential) {
                    mBinding.customerSignUpEmail.setError(getResources().getText(R.string.error_message_exception_sign_up_invalid_credentials));
                    mBinding.customerSignUpEmail.requestFocus();
                } catch (FirebaseAuthUserCollisionException userCollision) {
                    mBinding.customerSignUpEmail.setError(getResources().getText(R.string.error_message_exception_sign_up_collision));
                    mBinding.customerSignUpEmail.requestFocus();
                } catch (Exception e) {
                    showToast(R.string.toast_error_registration);
                }
            }
        });
    }
}
