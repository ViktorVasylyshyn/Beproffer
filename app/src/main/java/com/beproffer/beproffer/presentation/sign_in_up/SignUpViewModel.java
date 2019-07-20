package com.beproffer.beproffer.presentation.sign_in_up;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SignUpViewModel extends AndroidViewModel {
    private SignUpRepository mRepository;

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SignUpRepository(application);
    }

    public LiveData<Boolean> getShowProgress() {
        return mRepository.getShowProgress();
    }

    public LiveData<Boolean> getProcessing(){
        return mRepository.getProcessing();
    }

    public LiveData<Integer> getToastId() {
        return mRepository.getToastRes();
    }

    public LiveData<Integer> getErrorMessageId() {
        return mRepository.getErrorMessageId();
    }

    public LiveData<Integer> getNavigationId() {
        return mRepository.getNavigationId();
    }

    public void signUpNewUser(String userEmail,
                              String userPassword,
                              String userName,
                              String userType,
                              @Nullable String userPhone) {
        mRepository.signUpNewUser(userEmail, userPassword, userName, userType, userPhone);
    }

    public void resetTriggers(@Nullable Boolean resetToastIdValue,
                              @Nullable Boolean resetNavigationIdValue,
                              @Nullable Boolean resetErrorMessageIdValue) {
        mRepository.resetTriggers(resetToastIdValue, resetNavigationIdValue, resetErrorMessageIdValue);
    }
}
