package com.skyoung09.legolib;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.LayoutInflater;

/**
 * Created by zhangxiaobo02 on 16/6/14.
 */

public class LibContextWrapper extends ContextWrapper {

    private LayoutInflater mInflater;
    private Resources mResources;
    private AssetManager mAssets;

    public LibContextWrapper(Context base, Resources resources, AssetManager assets) {
        super(base);
        this.mResources = resources;
        this.mAssets = assets;
        mInflater = LayoutInflater.from(base).cloneInContext(this);
    }

    @Override
    public Object getSystemService(String name) {
        if (name.equals(LAYOUT_INFLATER_SERVICE)) {
            return mInflater;
        }
        return super.getSystemService(name);
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    @Override
    public AssetManager getAssets() {
        return mAssets;
    }
}
