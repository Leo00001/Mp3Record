package com.baiyuas.record;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.content.ContextCompat;

/**
 * @author 拜雨
 * @date 2020-10
 * mp3录音
 * 其中第一个参数就是选择录音源的，其可选参数如下：
 * MediaRecorder.AudioSource.CAMCORDER 设定录音来源于同方向的相机麦克风相同，若相机无内置相机或无法识别，则使用预设的麦克风
 * MediaRecorder.AudioSource.DEFAULT  默认音频源
 * MediaRecorder.AudioSource.MIC 设定录音来源为主麦克风。
 * MediaRecorder.AudioSource.VOICE_CALL 设定录音来源为语音拨出的语音与对方说话的声音
 * MediaRecorder.AudioSource.VOICE_COMMUNICATION 摄像头旁边的麦克风
 * MediaRecorder.AudioSource.VOICE_DOWNLINK 下行声音
 * MediaRecorder.AudioSource.VOICE_RECOGNITION 语音识别
 * MediaRecorder.AudioSource.VOICE_UPLINK 上行声音
 * mBufferSize：           期望录音时系统为其提供的缓冲区大小
 */
public final class Mp3Record {

    private final static String TAG = Mp3Record.class.getName();

    private final Context context;

    /**
     * 转换周期，录音每满160帧，进行一次转换
     */
    private static final int FRAME_COUNT = 160;
    /**
     * 采样率  默认44100
     */
    private final int DEFAULT_SAMPLING_RATE;

    /**
     * 默认立体音，双声道
     */
    private final int DEFAULT_CHANNEL_CONFIG;

    /**
     * 录音的比特数，当前设置的 PCMFormat.PCM_16BIT
     */
    private final int DEFAULT_AUDIO_FORMAT;

    /**
     * 缓冲区大小
     */
    private int mBufferSize;

    /**
     * 录音控制变量
     */
    private boolean isRecording;

    /**
     * mp3存储文件
     */
    private File mp3File;

    /**
     * 写mp3流
     */
    private FileOutputStream os;

    /**
     * 录音器
     */
    private AudioRecord mAudioRecord;

    /**
     * 转码线程
     */
    private DataEncodeThread mDataEncodeThread;

    /**
     * 线程池
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public Mp3Record(Context context) {
        this(context, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    public Mp3Record(Context context, int sampleRate, int channelConfig, int audioFormat) {
        this.context = context;
        DEFAULT_SAMPLING_RATE = sampleRate;
        DEFAULT_CHANNEL_CONFIG = channelConfig;
        DEFAULT_AUDIO_FORMAT = audioFormat;
    }

    /**
     * 初始化录音
     */
    private void initAudioRecord() throws FileNotFoundException {
        int bytesPerFrame = DEFAULT_AUDIO_FORMAT;
        int frameSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT) / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize = frameSize + (FRAME_COUNT - frameSize % FRAME_COUNT);
        }
        Log.d(TAG, "Frame size: " + frameSize);
        mBufferSize = frameSize * bytesPerFrame;
        Log.d(TAG, "设置缓存区大小: " + mBufferSize);
        Lame.init(DEFAULT_SAMPLING_RATE, 1, DEFAULT_SAMPLING_RATE, 32);
        os = new FileOutputStream(mp3File);
        mDataEncodeThread = new DataEncodeThread(os, mBufferSize);
        mDataEncodeThread.start();
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT,
                mBufferSize);
        mAudioRecord.setRecordPositionUpdateListener(mDataEncodeThread, mDataEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(DEFAULT_AUDIO_FORMAT);
    }

    /**
     * 开始录音
     *
     * @param mp3File  保存文件
     * @param listener 监听回调
     */
    public void startRecord(File mp3File, final Mp3RecordListener listener) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_DENIED) {
            listener.onError("缺少录音权限");
            Log.e("baiyuas", "未申请录音权限");
            return;
        }

        this.mp3File = mp3File;
        // 初始化适AudioRecord
        if (mAudioRecord == null) {
            try {
                initAudioRecord();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                listener.onError("录音初始化失败");
            }
        }
        //检测AudioRecord初始化是否成功
        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            mAudioRecord = null;
            mBufferSize = 0;
            listener.onError("录音初始化失败");
            Log.e(TAG, "录音初始化失败");
            return;
        }

        //创建一个位置用于存放后续的PCM数据
        final short[] mPCMBuffer = new short[mBufferSize];
        mAudioRecord.startRecording();

        Runnable runnable = () -> {
            isRecording = true;
            //循环的从AudioRecord获取录音的PCM数据
            while (isRecording) {
                int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                Log.d(TAG, "当前读取到缓存区数据大小:  " + readSize);
                if (readSize > 0) {
                    //待转换的PCM数据放到转换线程中
                    mDataEncodeThread.addChangeBuffer(mPCMBuffer, readSize);
                    int volume = Mp3Util.calculateRealVolume(mPCMBuffer, readSize);
                    Log.d(TAG, "当前录音音量:  " + volume);
                    if (listener != null) listener.onRecording(volume);
                }
            }

            // 录音完毕，释放AudioRecord的资源
            try {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                // 录音完毕，通知转换线程停止，并等待直到其转换完毕
                Message msg = Message.obtain(mDataEncodeThread.getHandler(), DataEncodeThread.PROCESS_STOP);
                msg.sendToTarget();

                Log.d(TAG, "waiting for encoding thread");
                mDataEncodeThread.join();
                Log.d(TAG, "done encoding thread");
                //转换完毕后回调监听
                if (listener != null) listener.onFinish(mp3File.getPath());
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (listener != null) listener.onError("未知异常");
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        executor.execute(runnable);
    }

    /**
     * 停止录音
     */
    public void stop() {
        isRecording = false;
    }

    /**
     * 释放资源
     */
    public void release() {
        mDataEncodeThread = null;
        try {
            if (os != null) {
                os.close();
            }
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Lame.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
