package com.beproffer.beproffer.presentation.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.beproffer.beproffer.App;
import com.beproffer.beproffer.R;
import com.beproffer.beproffer.presentation.browsing.BrowsingFragment;
import com.beproffer.beproffer.presentation.contacts.confirmed.ContactsFragment;
import com.beproffer.beproffer.presentation.profile.profile.ProfileFragment;
import com.beproffer.beproffer.presentation.sign_in_up.sign_in.SignInFragment;
import com.beproffer.beproffer.util.Const;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (!hasInternetConnection(this)) {
            showToast(R.string.toast_no_internet_connection);
            return false;
        }
        Fragment targetFragment;

        switch (item.getItemId()) {
            case R.id.bnm_contacts:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    targetFragment = new ContactsFragment();
                } else {
                    showToast(R.string.toast_available_for_registered);
                    return false;
                }
                break;
            case R.id.bnm_profile:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    targetFragment = new ProfileFragment();
                } else {
                    targetFragment = new SignInFragment();
                }
                break;
            default:
                targetFragment = new BrowsingFragment();/* в том числе, если поступило R.id.bnm_images_gallery*/
                break;
        }
        performNavigation(targetFragment, true, true, false, null);
        return true;
    };

    private final BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener = item -> {
        /*Повторные нажатия не должны ничего делать*/
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        App.getComponent().injectMainActivity(this);

        initBottomNavigationMenu();
        performNavigation(new BrowsingFragment(), true, false, false, null);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showToast(R.string.toast_guest_mode);
        }
    }

    private void initBottomNavigationMenu() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
    }

    public void performNavigation(@NonNull Fragment fragment, boolean addToBackStack,
                                  boolean clearBackStack, boolean addToCont, @Nullable Bundle arg) {
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right);
        if (arg != null) {
            fragment.setArguments(arg);
        }
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        if (clearBackStack) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        /*addToCont - используется добавлении в контейнер двух фрагментов: BrowsingItemInfoFragment
         * & SearchFragment. Сделано для эфекта появления этих фрагментов на фоне BrowsingFragment. Как бы поверх
         * свайп изображений. Конфликтов пока не замечено.*/
        if (!addToCont) {
            transaction.replace(R.id.fragment_container, fragment).commit();
        } else {
            transaction.add(R.id.fragment_container, fragment).commit();
        }
    }

    public void onBottomNavigationBarItemClicked(int menuItemId, @Nullable Integer toastRes) {
        bottomNavigationView.setSelectedItemId(menuItemId);
        if (toastRes != null) {
            showToast(toastRes);
        }
    }

    private boolean mCloseAppPermit;

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() == 1) {
            if (!mCloseAppPermit) {
                showToast(R.string.toast_close_the_app);
                mCloseAppPermit = true;
                Handler handler = new Handler();
                handler.postDelayed(() -> mCloseAppPermit = false, Const.COOLDOWNDUR_LONG);
                return;
            }
            fragmentManager.popBackStackImmediate();
            if (fragmentManager.getBackStackEntryCount() == 0) {
                super.onBackPressed();
            }
            return;
        }
        fragmentManager.popBackStackImmediate();
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }
}
