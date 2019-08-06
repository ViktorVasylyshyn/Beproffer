package com.beproffer.beproffer.presentation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.beproffer.beproffer.App;
import com.beproffer.beproffer.R;
import com.google.firebase.auth.FirebaseAuth;

import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class MainActivity extends AppCompatActivity {

    private NavController mNavController;
    private BottomNavigationView bottomNavigationView;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (!hasInternetConnection(this)) {
            Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.bnm_images_gallery:
                performNavigation(R.id.action_global_swipeImageFragment, null);
                return true;
            case R.id.bnm_contacts:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    performNavigation(R.id.action_global_contactsFragment, null);
                    return true;
                } else {
                    Toast.makeText(this, R.string.toast_request_for_registered, Toast.LENGTH_LONG).show();
                    return false;
                }
            case R.id.bnm_profile:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    performNavigation(R.id.action_global_profileFragment, null);
                } else {
                    performNavigation(R.id.action_global_signInFragment, null);
                }
                return true;
            default:
                break;
        }
        return true;
    };

    private final BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener = item -> {
        if (!hasInternetConnection(this)) {
            Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        switch (item.getItemId()) {
            case R.id.bnm_images_gallery:
                break;
            case R.id.bnm_contacts:
                break;
            case R.id.bnm_profile:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    performNavigation(R.id.action_global_profileFragment, null);
                } else {
                    performNavigation(R.id.action_global_signInFragment, null);
                }
                break;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        App.getComponent().injectMainActivity(this);

        initBottomNavigationMenu();
        mNavController = Navigation.findNavController(this, R.id.navigation_fragment_container);

    }

    private void initBottomNavigationMenu() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
    }

    public void performNavigation(int layoutId, @Nullable Bundle args) {
        if (args != null) {
            mNavController.navigate(layoutId, args);
        } else {
            mNavController.navigate(layoutId);
        }
    }

    public void onBottomNavigationBarItemClicked(int menuItemId, @Nullable Integer toastRes) {
        bottomNavigationView.setSelectedItemId(menuItemId);
        if (toastRes != null) {
            Toast.makeText(this, toastRes, Toast.LENGTH_LONG).show();
        }
    }

    public void popBackStack() {
        mNavController.popBackStack();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}