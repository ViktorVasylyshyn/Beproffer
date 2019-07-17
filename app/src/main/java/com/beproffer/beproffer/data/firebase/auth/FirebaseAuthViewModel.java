package com.beproffer.beproffer.data.firebase.auth;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthViewModel extends ViewModel {

    private FirebaseAuthLiveData mFirebaseAuthLiveData = new FirebaseAuthLiveData();

    public LiveData<FirebaseUser> getFirebaseAuthLiveData() {
        return mFirebaseAuthLiveData;
    }
}
