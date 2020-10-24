package com.baiyuas.media.util;

import android.os.Environment;
import android.text.TextUtils;

import com.baiyuas.media.App;

import java.io.File;
import java.util.Objects;

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

    public static void clear() {
        if (cacheDir.listFiles() != null && Objects.requireNonNull(cacheDir.listFiles()).length > 0) {
            for (File f : Objects.requireNonNull(Objects.requireNonNull(cacheDir.listFiles())[0].listFiles())) {
                deleteFile(f.getAbsolutePath());
            }
        }
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
