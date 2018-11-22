package com.zaranzalabs.zcar;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;

public class ZCarApplication extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        FirebaseApp.initializeApp(this);
    }

}
