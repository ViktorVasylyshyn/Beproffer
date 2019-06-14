package com.beproffer.beproffer.data;

import android.app.Activity;
import android.databinding.ObservableBoolean;

import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;
import com.beproffer.beproffer.data.models.UserData;

public class SaveUserData {
    public void saveUserDataToDatabase(String currentUserId, UserData userData, Activity activity, ObservableBoolean showProgress, int layoutId) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(userData.getUserType())
                .child(currentUserId)
                .child(Const.INFO)
                .setValue(userData).addOnSuccessListener(aVoid -> {
            showProgress.set(false);
            if (0 != layoutId) {
                ((MainActivity) activity).performNavigation(layoutId, null);
            }
        });
    }
}
