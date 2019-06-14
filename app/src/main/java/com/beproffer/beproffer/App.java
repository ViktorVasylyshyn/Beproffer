package com.beproffer.beproffer;

import android.app.Application;

import com.beproffer.beproffer.di.AppComponent;
import com.beproffer.beproffer.di.DaggerAppComponent;

public class App extends Application {

    private static AppComponent component;

    @Override
    public void onCreate(){
        super.onCreate();
        component = DaggerAppComponent.create();
    }
    public static AppComponent getComponent(){
        return component;
    }
}
