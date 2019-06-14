package com.beproffer.beproffer.presentation.specstorage;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SpecialistStorageImageViewModel extends ViewModel {

    private List<StorageImageItem> mStorageImageItemsList = new ArrayList<>();
    private MutableLiveData<List<StorageImageItem>> mStorageImageItemsListLiveData = new MutableLiveData<>();

    public void syncDataWithFirebase(FloatingActionButton button, ProgressBar progressBar) {
        if (mStorageImageItemsList.isEmpty()) {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(Const.SPEC)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.IMAGES)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    mStorageImageItemsList.add(data.getValue(StorageImageItem.class));
                                }
                            }
                            mStorageImageItemsListLiveData.setValue(mStorageImageItemsList);
                            floatingButtonDisplaying(button, progressBar);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(Const.ERROR, "SpecialistStorageImageViewModel onCancelled");
                            button.hide();
                        }
                    });
        } else {
            floatingButtonDisplaying(button, progressBar);
        }
    }

    private void floatingButtonDisplaying(FloatingActionButton button, ProgressBar progressBar) {
        if (mStorageImageItemsList.size() < 5) {
            button.show();
        }
        progressBar.setVisibility(View.GONE);
    }

    public void addSingleStorageImageItem(StorageImageItem storageImageItem) {
        if(mStorageImageItemsList.size() <5){
            mStorageImageItemsList.add(storageImageItem);
            return;
        }
        for (int i = 0; i < mStorageImageItemsList.size(); i++) {
            if (mStorageImageItemsList.get(i).getKey().equals(storageImageItem.getKey())) {
                mStorageImageItemsList.set(i, storageImageItem);
                return;
            }
        }
        mStorageImageItemsListLiveData.setValue(mStorageImageItemsList);
    }

    LiveData<List<StorageImageItem>> getStorageImagesItemsList() {
        return mStorageImageItemsListLiveData;
    }
}