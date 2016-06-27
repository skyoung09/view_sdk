package com.skyoung09.legoimpl;

import com.skyoung09.legolib.AppInit;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = LayoutInflater.from(this).inflate(R.layout.lego_main, null);
        setContentView(root);
    }

    @Override
    public Resources getResources() {
        if (AppInit.getInstance() != null) {
            Resources resources = AppInit.getInstance().getCurrentResources();
            if (resources != null) {
                return resources;
            }
        }
        return super.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (AppInit.getInstance() != null) {
            ClassLoader classLoader = AppInit.getInstance().getDexClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }
        return super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }
}