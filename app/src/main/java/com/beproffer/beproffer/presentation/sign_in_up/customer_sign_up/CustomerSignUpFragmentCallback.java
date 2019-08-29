package com.beproffer.beproffer.presentation.sign_in_up.customer_sign_up;

import android.view.View;

/*must be public for binding*/
public interface CustomerSignUpFragmentCallback {

    void onSignUpClick();

    void onPrivacyPolicyClick();

    void onTermsClick();

    void denySigningUp();

    void onShowPasswordClicked(View view);
}
