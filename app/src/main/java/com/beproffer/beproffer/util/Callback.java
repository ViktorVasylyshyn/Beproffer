package com.beproffer.beproffer.util;

import android.view.View;

import com.beproffer.beproffer.data.models.ContactItem;
import com.beproffer.beproffer.data.models.ContactRequestItem;
import com.beproffer.beproffer.data.models.SpecialistGalleryImageItem;

public interface Callback {

    interface  OnImageClickListener {
        void onImageClicked(SpecialistGalleryImageItem image);
    }

    interface  OnContactClickListener {
        void onContactClicked(View view, ContactItem item);
    }

    interface OnContactRequestClickListener {
        void onContactRequestClicked(View view, ContactRequestItem item);
    }
}
