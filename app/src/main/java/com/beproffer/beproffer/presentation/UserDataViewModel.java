package com.beproffer.beproffer.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.beproffer.beproffer.data.models.UserData;
import com.beproffer.beproffer.data.firebase.auth.FirebaseAuthLiveData;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class UserDataViewModel extends ViewModel {

    private FirebaseAuthLiveData mFirebaseAuthLiveData = new FirebaseAuthLiveData();

    private MutableLiveData<String> mCurrentUserTypeLiveData = new MutableLiveData<>();

    private MutableLiveData<UserData> mCurrentUserDataLiveData = new MutableLiveData<>();


    public LiveData<FirebaseUser> getFirebaseAuthLiveData() {
        return mFirebaseAuthLiveData;
    }

    public LiveData<String> getCurrentUserType() {
        return mCurrentUserTypeLiveData;
    }

    public LiveData<UserData> getUserData() {
        return mCurrentUserDataLiveData;
    }

    public void setUserData(UserData currentUserData){
        mCurrentUserDataLiveData.setValue(currentUserData);
    }

    public void defineUserType(Context context, String currentUserId) {

        if (context.getSharedPreferences(currentUserId, MODE_PRIVATE).getString(Const.USERTYPE, null) != null) {
            String currentUserType = context.getSharedPreferences(currentUserId, MODE_PRIVATE)
                    .getString(Const.USERTYPE, null);
            mCurrentUserTypeLiveData.setValue(currentUserType);
            loadUserDataFromFirebase(currentUserType);
        } else {
            FirebaseDatabase.getInstance().getReference().child(Const.USERS).child(Const.SPEC)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(currentUserId)) {
                                    mCurrentUserTypeLiveData.setValue(Const.SPEC);
                                    saveUserType(context, currentUserId, Const.SPEC);
                                } else {
                                    mCurrentUserTypeLiveData.setValue(Const.CUST);
                                    saveUserType(context, currentUserId, Const.CUST);
                                }
                                loadUserDataFromFirebase(mCurrentUserTypeLiveData.getValue());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(Const.ERROR, "Cant define currentUserType via firebase");
                        }
                    });
        }
    }

    private void saveUserType(Context context, String currentUserId, String currentUserType) {
        context.getSharedPreferences(currentUserId, MODE_PRIVATE).edit().putString(Const.USERTYPE,
                currentUserType).apply();
    }


    private void loadUserDataFromFirebase(String currentUserType) {
        if (mCurrentUserDataLiveData.getValue() != null){
            return;
        }
        FirebaseDatabase.getInstance().getReference().child(Const.USERS).child(currentUserType)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Const.INFO)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            mCurrentUserDataLiveData.postValue(dataSnapshot.getValue(UserData.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    public void resetUserData(){
        mCurrentUserDataLiveData.setValue(null);
    }

}
