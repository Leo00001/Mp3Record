package com.baiyuas.media;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baiyuas.media.util.DateUtils;
import com.baiyuas.media.util.FileUtils;
import com.baiyuas.media.view.WaveView;
import com.baiyuas.record.Mp3Record;
import com.baiyuas.record.Mp3RecordListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pub.devrel.easypermissions.EasyPermissions;
import www.linwg.org.lib.LCardView;

/**
 * @author 拜雨
 */
public class RecordDialogFragment extends BaseDialogFragment {

    private TextView tvRecording;
    private TextView tvRecordTime;
    private TextView tvStartRecording;
    private WaveView waveView;

    /**
     *当前录音的毫秒数
     */
    private long currentMilliseconds = 0;
    private int seconds = 0;
    private boolean isShortRecord;
    private Mp3Record mp3Record;
    private CountDownTimer countDownTimer;
    private OnRecordListener listener;

    public void setListener(OnRecordListener listener) {
        this.listener = listener;
    }

    static RecordDialogFragment newInstance() {
        return new RecordDialogFragment();
    }

    @Override
    protected int setView() {
        return R.layout.dialog_fragment_record;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvRecording = find(R.id.tv_recording);
        tvRecordTime = find(R.id.tv_record_time);
        tvStartRecording = find(R.id.tv_start_recording);
        waveView = find(R.id.wave_view);

        tvStartRecording.setOnLongClickListener(v -> {
            readyRecord();
            return true;
        });

        tvStartRecording.setOnTouchListener((v, motionEvent) -> {
            @SuppressLint("ClickableViewAccessibility") int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if (seconds < 3) {
                    isShortRecord = true;
                    Toast.makeText(getContext(), R.string.record_time_short, Toast.LENGTH_SHORT).show();
                }
                stopRecord();
                return false;
            }
            return false;
        });
        mp3Record = new Mp3Record(getContext());
    }


    /**
     * 准备录音
     */
    private void readyRecord() {
        FileUtils.deleteFile(FileUtils.getFilePath());
        record();
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        waveView.clear();
        tvRecording.setVisibility(View.VISIBLE);
        tvStartRecording.setTextColor(getResources().getColor(R.color.white));
        seconds = (int) (currentMilliseconds / 1000);
        currentMilliseconds = 0;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        mp3Record.stop();
    }

    /**
     * 开始录音
     */
    private void record() {
        String fileName = "recode" + System.currentTimeMillis() + ".mp3";
        //如果需要保存录音文件  设置好保存路径就会自动保存  也可以通过onRecordData 回调自己保存  不设置 不会保存录音
        mp3Record.startRecord(FileUtils.getCacheFilePath(fileName), new Mp3RecordListener() {

            @Override
            public void onRecording(int volume) {
               Objects.requireNonNull(getActivity()).runOnUiThread(()-> waveView.addData((short) volume));
            }

            @Override
            public void onFinish(String mp3FilePath) {
                if (isShortRecord) {
                    FileUtils.deleteFile(FileUtils.getFilePath());
                    isShortRecord = false;
                    return;
                }
                if (listener != null) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> listener.onBack(mp3FilePath));
                }
                dismiss();
            }

            @Override
            public void onError(String msg) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    tvStartRecording.setTextColor(getResources().getColor(R.color.white));
                });
                seconds = (int) (currentMilliseconds / 1000);
                currentMilliseconds = 0;
            }
        });

        countDownTimer();
        tvStartRecording.setTextColor(getResources().getColor(R.color.gray_7b7b7b));
        tvRecording.setVisibility(View.GONE);
        waveView.setVisibility(View.VISIBLE);
        tvRecordTime.setVisibility(View.VISIBLE);
    }

    /**
     * 启用计时器功能
     */
    private void countDownTimer() {
        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMilliseconds += 1000;
                seconds = (int) (currentMilliseconds / 1000);
                String hms = DateUtils.getFormatHMS(currentMilliseconds);
                tvRecordTime.setText(hms);
            }

            /**
             *倒计时结束后调用的
             */
            @Override
            public void onFinish() {
                mp3Record.stop();
                seconds = (int) (currentMilliseconds / 1000);
                currentMilliseconds = 0;
                tvStartRecording.setTextColor(getResources().getColor(R.color.white));
            }

        };
        countDownTimer.start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mp3Record != null) {
            mp3Record.release();
        }
    }
}
