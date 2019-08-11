package com.beproffer.beproffer.presentation.spec_gallery.edit;

import android.view.View;

/*must be public for binding*/
public interface SpecialistEditGalleryItemFragmentCallback {

    void onTypeButtonClick(View view);

    void onGenderButtonClick(View view);

    void onDurationButtonClick(View view);

    void setImage();

    void confirmImageData();

    void denyChanges();

    void onTermsClick();
}
