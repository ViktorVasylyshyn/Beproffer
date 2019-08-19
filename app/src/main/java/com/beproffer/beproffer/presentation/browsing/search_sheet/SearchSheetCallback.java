package com.beproffer.beproffer.presentation.browsing.search_sheet;

import android.view.View;

public interface SearchSheetCallback {

    void onServiceTypeIconClicked(View view);

    void onGenderClicked(View view);

    void onApplyClicked();

    void onDenyClicked();
}
