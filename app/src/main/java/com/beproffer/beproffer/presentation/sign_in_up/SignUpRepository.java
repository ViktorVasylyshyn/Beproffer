package com.beproffer.beproffer.presentation.sign_in_up;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.models.UserInfo;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class SignUpRepository {

    private Application mApplication;

    private MutableLiveData<Integer> mToastRes = new MutableLiveData<>();
    private MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> mNavigationId = new MutableLiveData<>();
    private MutableLiveData<Integer> mErrorMessageId = new MutableLiveData<>();/*устанавливает фокус
    на поле, которое требует изменения и выдает соответствующее сообщение рядом*/

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public SignUpRepository(Application application){
        mApplication = application;
    }

    public void signUpNewUser(String userEmail, String userPassword,
                              String userName, String userType, String userPhone) {
        feedBackToUi(true, null, null, null);
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(signUpTask -> {
            if (signUpTask.isSuccessful()) {
                try {
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(verifyTask -> {
                        if (verifyTask.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            mApplication.getApplicationContext()
                                    .getSharedPreferences(currentUserId, MODE_PRIVATE).edit()
                                    .putString(Const.USERTYPE, userType).apply();
                            saveUserDataToDatabase(new UserInfo(mAuth.getCurrentUser().getUid(),
                                    userType,
                                    userName,
                                    userEmail,
                                    userPhone,
                                    null,
                                    null));
                        }
                    });
                } catch (NullPointerException e) {
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            null, null);
                }
            } else {
                try {
                    throw signUpTask.getException();
                } catch (FirebaseAuthWeakPasswordException weakPassword) {
                    feedBackToUi(false, null, null,
                            R.string.error_message_exception_sign_up_weak_password);
                } catch (FirebaseAuthInvalidCredentialsException invalidCredential) {
                    feedBackToUi(false, null, null,
                            R.string.error_message_exception_sign_up_invalid_credentials);
                } catch (FirebaseAuthUserCollisionException userCollision) {
                    feedBackToUi(false, null, null,
                            R.string.error_message_exception_sign_up_collision);
                } catch (Exception e) {
                    feedBackToUi(false, R.string.toast_error_registration,
                            null, null);
                }
            }
        }).addOnFailureListener(e -> feedBackToUi(false,
                R.string.toast_error_user_data_saving_failure, null, null));
    }

    public void saveUserDataToDatabase(UserInfo userInfo) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(userInfo.getUserType())
                .child(userInfo.getUserId())
                .child(Const.INFO)
                .setValue(userInfo).addOnSuccessListener(aVoid -> feedBackToUi(false,
                R.string.toast_sign_up_email_verification, R.id.action_global_signInFragment, null))
                .addOnFailureListener(e -> feedBackToUi(false,
                        R.string.toast_error_user_data_saving_failure, null, null));
    }

    public LiveData<Integer> getToastRes() {
        return mToastRes;
    }

    public LiveData<Boolean> getShowProgress() {
        return mShowProgress;
    }

    public LiveData<Integer> getNavigationId() {
        return mNavigationId;
    }

    public LiveData<Integer> getErrorMessageId() {
        return mErrorMessageId;
    }

    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastId,
                              @Nullable Integer navigationId,
                              @Nullable Integer errorMessageId) {
        if (showProgress != null)
            mShowProgress.setValue(showProgress);
        if (toastId != null)
            mToastRes.setValue(toastId);
        if (navigationId != null)
            mNavigationId.setValue(navigationId);
        if (errorMessageId != null)
            mErrorMessageId.setValue(errorMessageId);
    }

    public void resetTriggers(@Nullable Boolean resetToastIdValue,
                              @Nullable Boolean resetNavigationIdValue,
                              @Nullable Boolean resetErrorMessageIdValue) {
        /*true = reset parameter, null = ignore parameter*/
        if (resetToastIdValue != null && resetToastIdValue) {
            mToastRes.setValue(null);
        }
        if (resetNavigationIdValue != null && resetNavigationIdValue) {
            mNavigationId.setValue(null);
        }
        if (resetErrorMessageIdValue != null)
            mErrorMessageId.setValue(null);
    }
}
