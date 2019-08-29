package com.beproffer.beproffer.presentation.sign_in_up.sign_in;

import android.view.View;

/*must be public for binding*/
public interface SingInFragmentCallback {
    void onSignInClick();

    void onCustomerSignUpClick();

    void onSpecialistSignUpClick();

    void onResetPasswordClick();

    void onTermsClick();

    void onPrivacyPolicyClick();

    void onShowPasswordClicked(View view);
}
