package com.example.myapplication;

import android.app.Application;

public class MyApp extends Application {
    private String globalString = "1";

    public String getGlobalString() {
        return globalString;
    }

    public void setGlobalString(String globalString) {
        this.globalString = globalString;
    }
}
