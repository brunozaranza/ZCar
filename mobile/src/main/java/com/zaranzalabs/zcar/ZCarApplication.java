package com.zaranzalabs.zcar;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class ZCarApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        FirebaseApp.initializeApp(this);
    }

}
