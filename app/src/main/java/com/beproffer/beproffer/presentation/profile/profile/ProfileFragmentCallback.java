package com.beproffer.beproffer.presentation.profile.profile;

import android.view.View;

/*must be public for binding*/
public interface ProfileFragmentCallback {

    void onPerformNavigationClick(View view);

    void onLogOutClick();

    void onPopularityClick();
}
