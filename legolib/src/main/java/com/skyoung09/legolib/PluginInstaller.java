package com.skyoung09.legolib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import dalvik.system.DexClassLoader;

/**
 * Created by zhangxiaobo02 on 16/6/15.
 */

public class PluginInstaller {
    private static final String APK_SUFFIX = ".apk";

    public static final String SCHEME_ASSETS = "assets://";
    public static final String SCHEME_FILE = "file://";

    /**
     * 动态库(so)目录名称
     */
    public static final String SO_LIB_DIR_NAME = "lib";

    private static volatile PluginInstaller mInstance = null;

    /**
     * 获取单例
     *
     * @return
     */
    public static PluginInstaller getInstance() {
        if (mInstance == null) {
            synchronized (PluginInstaller.class) {
                if (mInstance == null) {
                    mInstance = new PluginInstaller();
                }
            }
        }
        return mInstance;
    }

    public boolean installBuildIn(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        if (!path.startsWith(SCHEME_ASSETS)) {
            return false;
        }

        if (!path.endsWith(APK_SUFFIX)) {
            return false;
        }

        if (AppInit.getInstance().getAppContext() == null) {
            return false;
        }

        String assetsPath = path.substring(SCHEME_ASSETS.length());
        InputStream is = null;
        try {
            is = AppInit.getInstance().getAppContext().getAssets().open(assetsPath);
            if (!Util.isRomSizeCanInstallPlugin(is.available())) {
                return false;
            }
            doInstall(is, path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            BdCloseHelper.close(is);
        }
    }

    public boolean installApkFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        if (!path.startsWith(SCHEME_FILE)) {
            return false;
        }

        if (!path.endsWith(APK_SUFFIX)) {
            return false;
        }

        if (AppInit.getInstance().getAppContext() == null) {
            return false;
        }

        String apkFilePath = path.substring(SCHEME_FILE.length());
        File source = new File(apkFilePath);
        InputStream is = null;
        try {
            is = new FileInputStream(source);
            if (!Util.isRomSizeCanInstallPlugin(is.available())) {
                return false;
            }
            doInstall(is, path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            BdCloseHelper.close(is);
        }
    }

    private static String doInstall(InputStream is, String srcPathWithScheme) {
        if ((is == null) || TextUtils.isEmpty(srcPathWithScheme)) {
            return null;
        }

        if (AppInit.getInstance().getAppContext() == null) {
            return null;
        }

        File pluginsRoot = Util.getPluginsRootPath();
        if (pluginsRoot == null) {
            return null;
        }
        File tempFile = new File(pluginsRoot, String.valueOf(System.currentTimeMillis()));
        String result = Util.copyToFile(is, tempFile);
        if (!TextUtils.isEmpty(result)) {
            try {
                tempFile.delete();
            } catch (Exception e) {
            }
            return null;
        }
        String packageName;
        int versionCode = 0;

        PackageManager pm = AppInit.getInstance().getAppContext().getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(tempFile.getAbsolutePath(), PackageManager.GET_META_DATA);
        if (pkgInfo == null || pkgInfo.applicationInfo == null || pkgInfo.packageName == null
                    || pkgInfo.versionCode == 0) {
            ZipFile file;
            try {
                file = new ZipFile(tempFile, ZipFile.OPEN_READ);
                ZipEntry entry = file.getEntry("assets/AndroidManifest.xml");
                if (entry == null) {
                    try {
                        tempFile.delete();
                    } catch (Exception e) {
                    }
                    return null;
                }
                DocumentBuilder myDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document myDoc = myDocBuilder.parse(new InputSource(file.getInputStream(entry)));
                Element root = myDoc.getDocumentElement();
                packageName = root.getAttribute("package");
                versionCode = Integer.parseInt(root.getAttribute("android:versionCode"));
            } catch (Exception e) {
                packageName = null;
            }

            if (TextUtils.isEmpty(packageName)) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                }
                return null;
            }
        } else {
            packageName = pkgInfo.packageName;
            versionCode = pkgInfo.versionCode;
        }

        // 之前安装的插件的版本
        int destVersion = Util.getInstalledApkVersion();
        if (destVersion == versionCode) {
            return packageName;
        }

        // 之前安装的插件的路径
        String destFileName = packageName + "_" + System.currentTimeMillis() + "_" + versionCode;
        File destFile = new File(Util.getPluginsRootPath(), destFileName + ".apk");

        if (tempFile.getParent().equals(destFile.getParent())) {
            try {
                boolean ret = tempFile.renameTo(destFile);
                if (!ret) {
                    return null;
                }
                if (!destFile.exists() || destFile.length() == 0) {
                    return null;
                }
            } catch (Exception e) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                return null;
            }
        } else {
            InputStream tempIs = null;
            try {
                tempIs = new FileInputStream(tempFile);
                String tempResult = Util.copyToFile(tempIs, destFile);
                tempFile.delete();
                if (!TextUtils.isEmpty(tempResult)) {
                    return null;
                }
            } catch (Exception e) {
                if (destFile.exists()) {
                    destFile.delete();
                }
                return null;
            } finally {
                BdCloseHelper.close(tempIs);
            }
        }

        File pkgDir = new File(Util.getPluginsRootPath(), destFileName);
        try {
            pkgDir.mkdir();
        } catch (Exception e) {
        }

        File libDir = new File(pkgDir, SO_LIB_DIR_NAME);
        try {
            libDir.mkdir();
        } catch (Exception e) {
        }

        // 暂不考虑安装so
        // ....

        installDex(destFile.getAbsolutePath(), packageName, pkgDir);
        Util.setInstalledApkVersion(versionCode);
        Util.setInstalledApkPath(destFile.getAbsolutePath());
        return packageName;
    }

    private static void installDex(String apkFile, String packageName, File output) {
        File file = new File(apkFile);
        if (!file.exists()) {
            return;
        }
        if (file.length() == 0) {
            return;
        }
        if (AppInit.getInstance().getAppContext() == null) {
            return;
        }
        ClassLoader classloader = null;
        try {
            File optFile = AppInit.getInstance().getAppContext().getDir("lego_dex", Context.MODE_PRIVATE);
            File libFile = AppInit.getInstance().getAppContext().getDir("lego_lib", Context.MODE_PRIVATE);
            classloader = new DexClassLoader(apkFile, optFile.getAbsolutePath(), libFile.getAbsolutePath(),
                                                    AppInit.getInstance().getAppContext().getClassLoader());
        } catch (Exception e) {
        }

        try {
            if (classloader != null) {
                classloader.loadClass(packageName + ".Static");
            }
        } catch (ClassNotFoundException e) {

        } catch (Exception e) {

        }
    }
}
