package com.skyoung09.legolib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * Created by zhangxiaobo02 on 16/6/15.
 */

public class Util {
    public static final String PLUGIN_PATH = "lego_plugins";
    public static final String PLUGIN_CONFIG = "lego_plugins_config";
    public static final String PLUGIN_DEST_APK = "lego_plugins_dest";
    public static final String PLUGIN_DEST_VERSION = "lego_plugins_version";
    public static final long MAX_AVAI_ROM_SIZE = 30 * 1024 * 1024;
    /**
     * 判断现有的剩余空间是否满足某个apk插件的安装需求
     *
     * @param apkSize
     *            apk插件的大小，如果为《=0的值，则只判断空间是否够30M，不够返回false，够返回true
     * @return true表示可以安装，false表示不可以安装
     */
    public static final boolean isRomSizeCanInstallPlugin(long apkSize) {
        long availableRomSize = getAvailableRomSize();
        if (apkSize <= 0) {
            if (availableRomSize > 0 && availableRomSize < MAX_AVAI_ROM_SIZE) {
                return false;
            } else {
                return true;
            }
        }
        // 4.3及以下android使用dexopt优化dex，增加的大小有限，只是从apk解压的时候会变为压缩大小的2.5-4.5倍左右，odex文件只比原始dex文件大10%以内
        // 所以为3.5*1.1+1=4.85，放宽一下范围，取6
        // 5.0以及4.4开启oat模式后，oat优化会极大的增加dex文件，约为原始dex文件的2倍多一点，所以约为3.5*2+1=8，放宽一下范围，取10
        int rate = 10;
        if (Build.VERSION.SDK_INT < 19) {
            rate = 6;
        }
        long need = apkSize * rate;
        if (need > MAX_AVAI_ROM_SIZE) {
            need = MAX_AVAI_ROM_SIZE;
        }
        if (need < availableRomSize) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取可用rom大小
     *
     * @return
     */
    public static long getAvailableRomSize() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取插件根目录
     *
     * @return
     */
    public static File getPluginsRootPath() {
        try {
            File dir = AppInit.getInstance().getAppContext().getDir(PLUGIN_PATH, 0);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    return null;
                }
            }
            return dir;
        } catch (Exception e) {
        }
        return null;
    }

    public static String getInstalledApkPath() {
        if (AppInit.getInstance().getAppContext() == null) {
            return "";
        }
        SharedPreferences sharedPreferences = AppInit.getInstance().getAppContext()
                                                      .getSharedPreferences(PLUGIN_CONFIG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PLUGIN_DEST_APK, "");
    }

    public static void setInstalledApkPath(String dest) {
        if (AppInit.getInstance().getAppContext() == null) {
            return;
        }
        SharedPreferences sharedPreferences = AppInit.getInstance().getAppContext()
                                                      .getSharedPreferences(PLUGIN_CONFIG, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(PLUGIN_DEST_APK, dest).commit();
    }

    public static int getInstalledApkVersion() {
        if (AppInit.getInstance().getAppContext() == null) {
            return 0;
        }
        SharedPreferences sharedPreferences = AppInit.getInstance().getAppContext()
                                                      .getSharedPreferences(PLUGIN_CONFIG, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(PLUGIN_DEST_VERSION, 0);
    }

    public static void setInstalledApkVersion(int version) {
        if (AppInit.getInstance().getAppContext() == null) {
            return;
        }
        SharedPreferences sharedPreferences = AppInit.getInstance().getAppContext()
                                                      .getSharedPreferences(PLUGIN_CONFIG, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(PLUGIN_DEST_VERSION, version).commit();
    }

    /**
     * 拷贝文件
     *
     * @param inputStream
     * @param destFile
     * @return
     */
    public static String copyToFile(InputStream inputStream, File destFile) {
        if ((inputStream == null) || (destFile == null)) {
            return "illegal_param";
        }
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                if (e == null || TextUtils.isEmpty(e.getMessage())) {
                    return "unknown_error";
                } else {
                    return e.getMessage();
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                    if (e == null || TextUtils.isEmpty(e.getMessage())) {
                        return "unknown_error";
                    } else {
                        return e.getMessage();
                    }
                }
                out.close();
            }
            return null;
        } catch (IOException e) {
            if (e == null || TextUtils.isEmpty(e.getMessage())) {
                return "unknown_error";
            } else {
                return e.getMessage();
            }
        } catch (Exception e) {
            if (e == null || TextUtils.isEmpty(e.getMessage())) {
                return "unknown_error";
            } else {
                return e.getMessage();
            }
        }
    }
}
