package com.baiyuas.media.util;

import android.os.Environment;
import android.text.TextUtils;

import com.baiyuas.media.App;

import java.io.File;

/**
 * author: rivenlee
 * date: 2018/11/1
 * email: rivenlee0@gmail.com
 */
public class FileUtils {
    private static String filePath;
    private static File cacheDir = !isExternalStorageWritable()? App.getInstance()
            .getFilesDir(): App.getInstance().getExternalCacheDir();

    public static boolean deleteFile(String filename) {
        if (TextUtils.isEmpty(filename)){
            return false;
        }
        File file = new File(filename);
        filePath = null;
        return file.exists() && file.delete();
    }

    /**
     * 获取缓存文件地址
     */
    public static File getCacheFilePath(String fileName){
        filePath = cacheDir.getAbsolutePath() + "/record/" + fileName;
        File file = new File(cacheDir.getAbsolutePath() + "/record/");
        if (!file.exists()) {
            boolean result = file.mkdir();
        }
        return new File(filePath);
    }

    public static String getFilePath(){
        return filePath;
    }

    /**
     * 判断外部存储是否可用
     *
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
