package com.beproffer.beproffer.presentation.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Можно использовать Handler, если нужен переход на другое активити
    // за определенное время

    /*
        private Handler handler;
        private int time = 1500;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    /*
        handler = new Handler();
        handler.postDelayed(this::goToMainScreen, time);
    */
        goToMainScreen();
    }

    private void goToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
