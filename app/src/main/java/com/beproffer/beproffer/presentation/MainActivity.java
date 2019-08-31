package com.beproffer.beproffer.presentation;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.beproffer.beproffer.App;
import com.beproffer.beproffer.R;
import com.beproffer.beproffer.presentation.browsing.BrowsingFragment;
import com.beproffer.beproffer.presentation.contacts.confirmed.ContactsFragment;
import com.beproffer.beproffer.presentation.profile.profile.ProfileFragment;
import com.beproffer.beproffer.presentation.sign_in_up.sign_in.SignInFragment;
import com.google.firebase.auth.FirebaseAuth;

import static com.beproffer.beproffer.util.NetworkUtil.hasInternetConnection;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (!hasInternetConnection(this)) {
            Toast.makeText(this, R.string.toast_no_internet_connection, Toast.LENGTH_SHORT).show();
            return false;
        }
        Fragment targetFragment;

        switch (item.getItemId()) {
            case R.id.bnm_contacts:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    targetFragment = new ContactsFragment();
                } else {
                    Toast.makeText(this, R.string.toast_available_for_registered, Toast.LENGTH_LONG).show();
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
        performNavigation(targetFragment, true, true, false);
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
        performNavigation(new BrowsingFragment(), true, false, false);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, R.string.toast_guest_mode, Toast.LENGTH_LONG).show();
        }
    }

    private void initBottomNavigationMenu() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
    }

    public void setBadgeMain(int index) {
        /*создание красной точки, как подсказка, что в этом разделе, что то нужно сделать или доделать*/
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(index);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        if (itemView.getChildCount() == 2) {
            LayoutInflater.from(this).inflate(R.layout.bnb_badge, itemView, true);
        }
    }

    public void removeBadge(int index) {
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(index);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        if (itemView.getChildCount() > 2) {
            itemView.removeViewAt(itemView.getChildCount() - 1);
        }
    }


    public void performNavigation(@NonNull Fragment fragment, boolean addToBackStack, boolean clearBackStack, boolean addToCont) {
        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
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
            Toast.makeText(this, toastRes, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        fragmentManager.popBackStackImmediate();

        if (fragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }
}