package com.beproffer.beproffer.presentation.specstorage.edit;

import android.view.View;

public interface SpecialistStorageEditFragmentCallback {

    void onTypeButtonClick(View view);

    void onGenderButtonClick(View view);

    void onDurationButtonClick(View view);

    void setImage();

    void confirmImageData();
}
