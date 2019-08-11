package com.beproffer.beproffer.presentation.profile.profile_edit;

import android.view.View;
/*must be public for binding*/
public interface ProfileEditFragmentCallback {

    void onSetProfileImageClick();

    void onSpecialistTypeIconClick(View view);

    void onCheckUserDataClick();

    void denyChanges();
}
