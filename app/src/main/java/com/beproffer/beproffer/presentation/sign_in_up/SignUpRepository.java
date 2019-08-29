package com.beproffer.beproffer.presentation.sign_in_up;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
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

class SignUpRepository {

    private final Application mApplication;

    private final MutableLiveData<Integer> mToastRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mShowProgress = new MutableLiveData<>();
    /*нужно для контроля частых нажатий на кнопки*/
    private final MutableLiveData<Boolean> mProcessing = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mPopBackStack = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mHideKeyboard = new MutableLiveData<>();
    /*устанавливает фокус на поле, которое требует изменения и выдает соответствующее сообщение рядом*/
    private final MutableLiveData<Integer> mErrorMessageId = new MutableLiveData<>();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public SignUpRepository(Application application) {
        mApplication = application;
    }

    public void signUpNewUser(String userEmail,
                              String userPassword,
                              String userName,
                              String userType,
                              @Nullable String userSpecialistType,
                              @Nullable String userPhone) {
        feedBackToUi(true, null, false, null, true, false);
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
                                    userSpecialistType,
                                    userName,
                                    userEmail,
                                    userPhone,
                                    null,
                                    null));
                        }
                    });
                } catch (NullPointerException e) {
                    feedBackToUi(false, R.string.toast_error_has_occurred,
                            false, R.string.message_error_has_occurred, false, true);
                }
            } else {
                try {
                    throw signUpTask.getException();
                } catch (FirebaseAuthWeakPasswordException weakPassword) {
                    feedBackToUi(false, null, false,
                            R.string.error_message_exception_sign_up_weak_password, false, true);
                } catch (FirebaseAuthInvalidCredentialsException invalidCredential) {
                    feedBackToUi(false, null, false,
                            R.string.error_message_exception_sign_up_invalid_credentials, false, true);
                } catch (FirebaseAuthUserCollisionException userCollision) {
                    feedBackToUi(false, null, false,
                            R.string.error_message_exception_sign_up_collision, false, true);
                } catch (Exception e) {
                    feedBackToUi(false, R.string.toast_error_registration,
                            false, R.string.message_error_has_occurred, false, true);
                }
            }
        });
    }

    private void saveUserDataToDatabase(UserInfo userInfo) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(userInfo.getType())
                .child(userInfo.getId())
                .child(Const.INFO)
                .setValue(userInfo).addOnSuccessListener(aVoid -> {
            FirebaseAuth.getInstance().signOut();
            feedBackToUi(false, R.string.toast_sign_up_email_verification, true, null, false, true);
        })
                .addOnFailureListener(e -> feedBackToUi(false,
                        R.string.toast_error_user_data_saving_failure, false, R.string.message_error_has_occurred, false, true));
    }

    public LiveData<Integer> getToastRes() {
        return mToastRes;
    }

    public LiveData<Boolean> getShowProgress() {
        return mShowProgress;
    }

    public LiveData<Boolean> getProcessing() {
        return mProcessing;
    }

    public LiveData<Boolean> getPopBackStack() {
        return mPopBackStack;
    }

    public LiveData<Integer> getErrorMessageId() {
        return mErrorMessageId;
    }

    public LiveData<Boolean> getHideKeyboard() {
        return mHideKeyboard;
    }

    private void feedBackToUi(@Nullable Boolean showProgress,
                              @Nullable Integer toastId,
                              @NonNull Boolean popBackStack,
                              @Nullable Integer errorMessageId,
                              @Nullable Boolean processing,
                              @NonNull Boolean hideKeyboard) {
        if (showProgress != null)
            mShowProgress.setValue(showProgress);
        if (toastId != null) {
            mToastRes.setValue(toastId);
            mToastRes.setValue(null);
        }
        if (popBackStack) {
            mPopBackStack.setValue(true);
            mPopBackStack.setValue(null);
        }
        if (errorMessageId != null) {
            mErrorMessageId.setValue(errorMessageId);
            mErrorMessageId.setValue(null);
        }
        /*блокирование повторных запросов если предыдущий идентичный в процессе выполнения*/
        if (processing != null) {
            mProcessing.setValue(processing);
        }
        if (hideKeyboard) {
            mHideKeyboard.setValue(true);
            mHideKeyboard.setValue(null);
        }
    }
}
