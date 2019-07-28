package com.beproffer.beproffer.presentation.base;


import android.app.Activity;
import android.databinding.ObservableBoolean;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class BaseFragment extends Fragment {

    public final ObservableBoolean mShowProgress = new ObservableBoolean(false);

    public final ObservableBoolean mProcessing = new ObservableBoolean(false);

    public boolean checkInternetConnection() {
        return hasInternetConnection(requireContext());
    }

    /*Показ тостов при запросе в UserDataRepository обрабатывается в репозитории а не снаружи в java файле автивити*/
    public void showToast(int resId) {
        Toast.makeText(this.requireContext(), resId, Toast.LENGTH_LONG).show();
    }

    /*Показ progress bar при запросе в UserDataRepository обрабатывается в репозитории а не снаружи в java файле автивити*/
    public void showProgress(boolean show) {
        mShowProgress.set(show);
    }

    public void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void popBackStack() {
        ((MainActivity) requireActivity()).popBackStack();
    }

    public void performNavigation(int destinationId) {
        /*пока что на вход этому методу подается только айди пункта назначения. если вдруг в будущем
         * появится надобность на передачу аргументов - допишем второй параметр*/
        ((MainActivity) requireActivity()).performNavigation(destinationId, null);
    }

    /*существует специальный листенер, который слушает юзера. поначалу у меня так и было, но из-за
     * того, что я чайник, в некоторых моментах получались конфузы, и я не знал, как их решить. поэтому
     * пока что делаю мануальную проверку юзера, без листенера. а когда будут над кодом работать люди,
     * которые в этом соображают лучше - сделают прослушивание через листенер, по человечески.*/
    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /*нужно, для контроля частых нажатий на кнопки*/
    public void processing(boolean processing) {
        mProcessing.set(processing);
    }

    public void cooldown(View view){
        view.setClickable(false);
        Handler handlerWordAnim = new Handler();
        handlerWordAnim.postDelayed(() -> view.setClickable(true), Const.COOLDOWNDUR);
    }
}
