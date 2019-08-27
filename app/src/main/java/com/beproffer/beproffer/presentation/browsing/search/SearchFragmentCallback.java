package com.beproffer.beproffer.presentation.browsing.search;

import android.view.View;

public interface SearchFragmentCallback {

    void onServiceTypeIconClicked(View view);

    void onGenderClicked(View view);

    void onApplyClicked();

    void onDenyClicked();

    void onFreeAreaClicked();
}
