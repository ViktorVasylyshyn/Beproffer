package com.beproffer.beproffer.presentation.base;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.presentation.activities.MainActivity;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class BaseFragment extends Fragment {

    protected final ObservableBoolean mShowProgress = new ObservableBoolean(false);

    protected final ObservableBoolean mProcessing = new ObservableBoolean(false);

    protected boolean checkInternetConnection() {
        return hasInternetConnection(requireContext());
    }

    /*Показ тостов при запросе в UserDataRepository обрабатывается в репозитории а не снаружи в java файле автивити*/
    protected void showToast(int resId) {
        Toast.makeText(this.requireContext(), resId, Toast.LENGTH_LONG).show();
    }

    /*Показ progress bar при запросе в UserDataRepository обрабатывается в репозитории а не снаружи в java файле автивити*/
    protected void showProgress(boolean show) {
        mShowProgress.set(show);
    }

    protected void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected void popBackStack() {
        (requireActivity()).onBackPressed();
    }

    @SuppressWarnings("SameParameterValue")
    protected void changeFragment(Fragment fragment, boolean addToBackStack, boolean clearTask, boolean addToCont, Bundle arg) {
        ((MainActivity) requireActivity()).performNavigation(fragment, addToBackStack, clearTask, addToCont, arg);
    }

    @SuppressWarnings("SameParameterValue")
    protected void performOnBottomNavigationBarItemClick(int itemId, @Nullable Integer toastRes) {
        ((MainActivity) requireActivity()).onBottomNavigationBarItemClicked(itemId, toastRes);
    }

    /*существует специальный листенер, который слушает юзера. поначалу у меня так и было, но из-за
     * того, что я чайник, в некоторых моментах получались конфузы, и я не знал, как их решить. поэтому
     * пока что делаю мануальную проверку юзера, без листенера. а когда будут над кодом работать люди,
     * которые в этом соображают лучше - сделают прослушивание через листенер, по человечески.*/
    protected FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /*нужно, для контроля частых нажатий на кнопки*/
    protected void processing(boolean processing) {
        mProcessing.set(processing);
    }

    protected void cooldown(View view) {
        view.setClickable(false);
        Handler handlerWordAnim = new Handler();
        handlerWordAnim.postDelayed(() -> view.setClickable(true), Const.COOLDOWNDUR_LONG);
    }

    /*TODO метод валиден только для фрагментов регистрации и авторизациию. Создать базовый фрагмент,
       для фрагментов авторизации и регистрации.*/
    private ImageView mImageView;

    protected void passwordVisibility(EditText editText, ImageView imageView) {
        /*в случае, если раскроются сразу два поля(на фрагментах регистрации), то первое поле отреагирует
         только после двух кликов. баг мелкий, так что, пускай пока живет.*/
        if (mImageView != null && mImageView.getId() == imageView.getId()) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_pass_hide);
            mImageView = null;
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_pass_show);
            mImageView = imageView;
        }
    }

    protected void openDoc(int resId) {
        if (checkInternetConnection()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(resId)));
            startActivity(browserIntent);
        } else {
            showToast(R.string.toast_no_internet_connection);
        }
    }

    protected void requestErrorFocus(TextView textView, int messageId) {
        textView.requestFocus();
        textView.setError(getResources().getText(messageId));
    }

    protected String editInputText(String inputRes) {
        /*заменяем повторяющиеся отступы в средине на один отступ + обрезаем отступы вначале и конце*/
        String editedRes = inputRes.replaceAll("\\s+", " ");
        return editedRes.trim();
    }
}
