package com.beproffer.beproffer;

import android.app.Application;

import com.beproffer.beproffer.di.AppComponent;
import com.beproffer.beproffer.di.DaggerAppComponent;
import com.squareup.leakcanary.LeakCanary;

public class App extends Application {

    private static AppComponent component;

    @Override
    public void onCreate(){
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        component = DaggerAppComponent.create();
    }
    public static AppComponent getComponent(){
        return component;
    }
}
