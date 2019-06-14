package com.beproffer.beproffer.data.firebase;

import android.databinding.ObservableBoolean;

import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.FirebaseDatabase;

public class SpecialistStorageSaveImage {
    private ObservableBoolean mShowProgress;
    private boolean service;
    private boolean images;

    public void saveImageData(String userId, StorageImageItem imageItem, ObservableBoolean showProgress) {
        mShowProgress = showProgress;
        saveImageDataToService(userId, imageItem);
        saveImageDataToUserImages(userId, imageItem);
    }

    private void saveImageDataToService(String userId, StorageImageItem imageItem) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.SERVICES)
                .child(imageItem.getType())
                .child(imageItem.getSubtype())
                .child(userId)
                .child(imageItem.getKey())
                .setValue(imageItem).addOnSuccessListener(aVoid -> {
            service = true;
            endSaving();
        });
    }

    private void saveImageDataToUserImages(String userId, StorageImageItem imageItem) {
        FirebaseDatabase.getInstance().getReference()
                .child(Const.USERS)
                .child(Const.SPEC)
                .child(userId)
                .child(Const.IMAGES)
                .child(imageItem.getKey())
                .setValue(imageItem).addOnSuccessListener(aVoid -> {
            images = true;
            endSaving();
        });
    }

    private void endSaving() {
        if (service && images) {
            mShowProgress.set(false);
        }
    }
}
