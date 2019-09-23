package com.beproffer.beproffer.util;

import android.view.View;

import com.beproffer.beproffer.data.models.BrowsingImageItem;
import com.beproffer.beproffer.data.models.ContactItem;

public interface Callback {

    interface  OnImageClickListener {
        void onImageClicked(BrowsingImageItem image);
    }

    interface  OnContactClickListener {
        void onContactClicked(View view, ContactItem item);
    }
}
