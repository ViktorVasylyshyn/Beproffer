package com.beproffer.beproffer.presentation.browsing.info;

import android.view.View;

/*must be public for binding*/
public interface BrowsingItemInfoFragmentCallback {

    void onAddContactClick();

    void onPerformBackNavigationClick();

    void onVoteClick();

    void onCallClick(View view);
}
