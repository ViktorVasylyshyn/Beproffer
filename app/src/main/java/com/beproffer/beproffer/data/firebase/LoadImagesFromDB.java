package com.beproffer.beproffer.data.firebase;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadImagesFromDB {

    public void loadOneImage(Activity activity, ImageView imageView, String currentUserId, String currentUserType) {
        String imageUrl;
        imageUrl = activity.getSharedPreferences(currentUserId, activity.MODE_PRIVATE).getString(Const.PROFIMGURL, null);
        if(imageUrl == null) {
            FirebaseDatabase.getInstance().getReference()
                    .child(Const.USERS)
                    .child(currentUserType)
                    .child(currentUserId)
                    .child(Const.INFO).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        if (dataSnapshot.child(Const.PROFIMGURL).getValue() != null) {
                            Glide.with(activity.getApplication()).load(dataSnapshot.child(Const.PROFIMGURL).getValue().toString()).into(imageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            try{
                Glide.with(activity.getApplication()).load(imageUrl).into(imageView);
            }catch (Exception e){
                Toast.makeText(activity, "wrong url from local memory", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
