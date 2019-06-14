package com.beproffer.beproffer.presentation.contacts.confirmed;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.beproffer.beproffer.data.firebase.FirebaseQueryLiveData;
import com.beproffer.beproffer.data.models.UserData;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmedContactsViewModel extends ViewModel {

    private FirebaseQueryLiveData liveData;

    void obtainConfirmedContactsId(UserData currentUserData) {

        liveData = new FirebaseQueryLiveData(FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(currentUserData.getUserType())
                .child(currentUserData.getUserId())
                .child(Const.CONTACT));
    }

    @NonNull
    LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }

    void deleteContact(UserData currentUserData, String contactUserId) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(currentUserData.getUserType())
                .child(currentUserData.getUserId())
                .child(Const.CONTACT)
                .child(contactUserId).removeValue();

    }
}
