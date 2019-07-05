package com.beproffer.beproffer.presentation.base;


import android.app.Activity;
import android.databinding.ObservableBoolean;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.beproffer.beproffer.presentation.MainActivity;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class BaseFragment extends Fragment {

    public final ObservableBoolean mShowProgress = new ObservableBoolean(false);

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

    public void popBackStack(){
        ((MainActivity)requireActivity()).popBackStack();
    }
}
