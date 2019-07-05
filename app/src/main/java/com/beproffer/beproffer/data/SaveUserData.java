package com.beproffer.beproffer.data;

import android.app.Activity;
import android.databinding.ObservableBoolean;

import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;
import com.beproffer.beproffer.data.models.UserInfo;

public class SaveUserData {
    public void saveUserDataToDatabase(String currentUserId, UserInfo userInfo, Activity activity, ObservableBoolean showProgress, int layoutId) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(userInfo.getUserType())
                .child(currentUserId)
                .child(Const.INFO)
                .setValue(userInfo).addOnSuccessListener(aVoid -> {
            showProgress.set(false);
            if (0 != layoutId) {
                ((MainActivity) activity).performNavigation(layoutId, null);
            }
        });
    }
}
