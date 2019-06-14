package com.beproffer.beproffer.data.firebase.auth;

import android.arch.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthLiveData extends LiveData<FirebaseUser> {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    private FirebaseAuth.AuthStateListener authStateListener =
            firebaseAuth -> {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                setValue(firebaseUser);
            };

    @Override
    protected void onActive() {
        super.onActive();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mFirebaseAuth.removeAuthStateListener(authStateListener);
    }
}