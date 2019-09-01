package com.beproffer.beproffer.presentation.sign_in_up.sign_up;

import android.view.View;

/*must be public for binding*/
public interface SignUpFragmentCallback {

    void onSignUpClick();

    void onPrivacyPolicyClick();

    void onTermsClick();

    void denySigningUp();

    void onShowPasswordClicked(View view);
}
