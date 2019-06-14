package com.beproffer.beproffer.presentation.contacts.request;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.beproffer.beproffer.data.firebase.FirebaseQueryLiveData;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactRequestsViewModel extends ViewModel {
    private static final DatabaseReference HOT_STOCK_REF =
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(Const.SPEC)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.REQUEST);

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(HOT_STOCK_REF);

    @NonNull
    LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}