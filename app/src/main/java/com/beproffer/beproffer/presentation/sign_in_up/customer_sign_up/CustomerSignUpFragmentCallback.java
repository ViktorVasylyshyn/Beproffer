package com.beproffer.beproffer.presentation.sign_in_up.customer_sign_up;

/*must be public for binding*/
public interface CustomerSignUpFragmentCallback {

    void onSignUpClick();

    void onPrivacyPolicyClick();

    void onTermsClick();

    void denySigningUp();
}
