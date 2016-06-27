package com.skyoung09.legolib;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import dalvik.system.DexClassLoader;

/**
 * Created by zhangxiaobo02 on 16/6/13.
 */

public class AppInit {
    public static AppInit instance = null;
    // 宿主context
    private Context mContext;
    // 插件context
    private LibContextWrapper mLibContext;
    // 插件classloader
    private DexClassLoader mDexClassLoader;
    // 插件resources
    private Resources mCurrentResources;

    public static AppInit getInstance() {
        if (null == instance) {
            synchronized (AppInit.class) {
                if (null == instance) {
                    instance = new AppInit();
                }
            }
        }
        return instance;
    }

    private AppInit() {
    }

    public Context getAppContext() {
        return mContext;
    }

    public void init(Context context) {
        mContext = context;
        loadLego();
    }

    public LibContextWrapper getLibContext() {
        return mLibContext;
    }

    public Resources getCurrentResources() {
        return mCurrentResources;
    }

    public DexClassLoader getDexClassLoader() {
        return mDexClassLoader;
    }

    private void loadLego() {
        PluginInstaller.getInstance().installBuildIn("assets://plugins/lego3.apk");
//        PluginInstaller.getInstance().installApkFile(PluginInstaller.SCHEME_FILE +
//                                                             Environment.getExternalStorageDirectory().toString()
//                                                             + File.separator + "lego3.apk");
        String apkPath = Util.getInstalledApkPath();
        loadRes(apkPath);
        loadDex(apkPath);
        mLibContext = new LibContextWrapper(mContext, mCurrentResources, mCurrentResources.getAssets());
    }

    private void loadDex(String apkPath) {
        try {
            File optFile = mContext.getDir("lego_dex", Context.MODE_PRIVATE);
            File libFile = mContext.getDir("lego_lib", Context.MODE_PRIVATE);
            mDexClassLoader = new DexClassLoader(apkPath, optFile.getAbsolutePath(),
                                                              libFile.getAbsolutePath(),
                                                              mContext.getClassLoader());
            ClassLoaderInjectHelper.inject(mContext.getClassLoader(), mDexClassLoader,
                    "com.skyoung09.legoimpl.Static", true);
            Class clazz1 = mDexClassLoader.loadClass("com.skyoung09.legoimpl.Static");
            JavaCalls.callStaticMethodOrThrow(clazz1, "init", null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void loadRes(String apkPath) {
            try {
                AssetManager am = AssetManager.class.newInstance();
                JavaCalls.callMethod(am, "addAssetPath", new Object[] { apkPath });
                Resources hostResources = mContext.getResources();
                mCurrentResources = new Resources(am, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }


    }
}
