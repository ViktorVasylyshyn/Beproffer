package com.beproffer.beproffer.presentation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

import com.beproffer.beproffer.App;
import com.beproffer.beproffer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class MainActivity extends AppCompatActivity {

    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        App.getComponent().injectMainActivity(this);

        initBottomNavigationMenu();
        mNavController = Navigation.findNavController(this, R.id.navigation_fragment_container);
    }

    private void initBottomNavigationMenu() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bnm_images_gallery:
                    performNavigation(R.id.action_global_swipeImageFragment, null);
                    return true;
                case R.id.bnm_search:
                    performNavigation(R.id.action_global_searchFragment, null);
                    return true;
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
            return false;
        });
    }


    public void performNavigation(int layoutId, @Nullable Bundle args) {
        if (args != null) {
            mNavController.navigate(layoutId, args);
        } else {
            mNavController.navigate(layoutId);
        }
    }

    public void popBackStack() {
        mNavController.popBackStack();
    }
}