package com.baiyuas.record;

/**
 * @author 拜雨
 * @date 2020-10
 */
public interface Mp3RecordListener {

    void onRecording(int volume);

    void onFinish(String mp3FilePath);

    void onError(String msg);
}
