package com.baiyuas.media;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.baiyuas.record.MediaPlayUtil;
import com.baiyuas.record.Mp3Record;
import com.baiyuas.record.Mp3RecordListener;

import java.io.File;

/**
 * 拜雨
 */
public class MainActivity extends AppCompatActivity {

    private Mp3Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayUtil.init(this);

        TextView tvMsg = findViewById(R.id.tv_msg);
        TextView tvPath = findViewById(R.id.tv_path);
        Mp3Record record = new Mp3Record(this);

        findViewById(R.id.btn_play).setOnClickListener(v -> {
                String path = tvPath.getText().toString().trim();
                if (!TextUtils.isEmpty(path)) {
                    MediaPlayUtil.playSound(path, mp -> {
                        Toast.makeText(this, "播放完成", Toast.LENGTH_SHORT).show();
                    });
                }
        });

        findViewById(R.id.btn_start).setOnClickListener(v -> {
            record.startRecord(new File("/sdcard/temp.mp3"), new Mp3RecordListener() {

                @Override
                public void onRecording(int volume) {
                    runOnUiThread(()-> tvMsg.setText("当前音量：" + volume));
                }

                @Override
                public void onFinish(String mp3FilePath) {
                    runOnUiThread(()-> tvPath.setText(mp3FilePath));
                }

                @Override
                public void onError(String msg) {
                    runOnUiThread(()-> tvMsg.setText(msg));
                }
            });
        });

        findViewById(R.id.btn_stop).setOnClickListener(v -> {
            record.stop();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        record.release();
    }
}