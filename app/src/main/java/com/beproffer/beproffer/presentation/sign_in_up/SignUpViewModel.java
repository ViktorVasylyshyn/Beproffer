package com.beproffer.beproffer.presentation.sign_in_up;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class SignUpViewModel extends AndroidViewModel {
    private final SignUpRepository mRepository;

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SignUpRepository(application);
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Boolean> getProcessing() {
        return mRepository.getProcessing();
    }

    public LiveData<Integer> getToastId() {
        return mRepository.getToastRes();
    }

    public LiveData<Integer> getErrorMessageId() {
        return mRepository.getErrorMessageId();
    }

    public LiveData<Boolean> getPopBackStack() {
        return mRepository.getPopBackStack();
    }

    public LiveData<Boolean> getHideKeyboard() {
        return mRepository.getHideKeyboard();
    }

    public void resetValues(@NonNull Boolean resetToast,
                            @NonNull Boolean resetPopBackStack,
                            @NonNull Boolean resetErrorMessage,
                            @NonNull Boolean resetHideKeyboard) {
        mRepository.resetValues(resetToast,
                resetPopBackStack,
                resetErrorMessage,
                resetHideKeyboard);
    }

    public void signUpNewUser(String userEmail,
                              String userPassword,
                              String userName,
                              String userType,
                              @Nullable String userSpecialistType,
                              @Nullable String userPhone) {
        mRepository.signUpNewUser(userEmail, userPassword, userName, userType, userSpecialistType, userPhone);
    }
}
