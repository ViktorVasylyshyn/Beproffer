package com.beproffer.beproffer.presentation.base;


import android.databinding.ObservableBoolean;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class BaseFragment extends Fragment {

    public final ObservableBoolean mShowProgress = new ObservableBoolean(false);

    public boolean checkInternetConnection(){
        return hasInternetConnection(requireContext());
    }

    public void showToast(int resId){
        Toast.makeText(this.requireContext(), resId, Toast.LENGTH_LONG).show();
    }

    public void showProgress(boolean show){
        mShowProgress.set(show);
    }
}
