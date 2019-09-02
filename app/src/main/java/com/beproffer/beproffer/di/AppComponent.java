package com.beproffer.beproffer.di;

import com.beproffer.beproffer.presentation.activities.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component()
public interface AppComponent {
    void injectMainActivity(MainActivity mainActivity);
}

