package com.beproffer.beproffer.util;

import android.view.View;

import com.beproffer.beproffer.data.models.ConfirmedContactItem;
import com.beproffer.beproffer.data.models.StorageImageItem;

public interface Callback {

    interface  OnImageClickListener {
        void onImageClicked(View view, StorageImageItem image);
    }

    interface  OnContactClickListener {
        void onContactClicked(View view, ConfirmedContactItem item, int position);
    }
}
