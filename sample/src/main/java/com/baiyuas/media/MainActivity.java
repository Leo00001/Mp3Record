package com.baiyuas.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baiyuas.media.util.FileUtils;
import com.baiyuas.media.util.ScreenUtils;
import com.baiyuas.record.MediaPlayUtil;
import com.google.android.flexbox.FlexboxLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 拜雨
 */
public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, OnRecordListener {

    private static final int RC_STORAGE_AND_RECORD = 1;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final List<PlayClick> playClickList = new ArrayList<>();
    private final List<CloseClick> closeClickList = new ArrayList<>();
    private FlexboxLayout viewGroup;

    private float max;
    private float min;
    private int height;
    private int margin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playClickList.clear();
        closeClickList.clear();
        max = dip2px(this, ScreenUtils.getScreenWidth(this) - 48);
        min = dip2px(this, 100);
        height = dip2px(this, 36);
        margin = dip2px(this, 5);

        viewGroup = findViewById(R.id.flex_layout);
        viewGroup.removeAllViews();
        findViewById(R.id.btn_record).setOnClickListener(v -> checkPermission());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void checkPermission() {
        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, RC_STORAGE_AND_RECORD, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .setRationale(R.string.storage_and_record_rationale)
                        .setPositiveButtonText(R.string.rationale_ask_ok)
                        .setNegativeButtonText(R.string.rationale_ask_cancel)
                        .build());
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        RecordDialogFragment dialogFragment = RecordDialogFragment.newInstance();
        dialogFragment.setListener(this);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    @Override
    public void onBack(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            addToView(path, duration / 1000 + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToView(String path, int duration) {
        View view = getLayoutInflater().inflate(R.layout.record_item_layout, null);

        ImageView play = view.findViewById(R.id.iv_media);
        PlayClick playClick = new PlayClick(path, play);
        playClickList.add(playClick);
        play.setOnClickListener(playClick);

        ImageView close = view.findViewById(R.id.iv_close);
        CloseClick closeClick = new CloseClick(playClickList.size() - 1);
        closeClickList.add(closeClick);
        close.setOnClickListener(closeClick);

        TextView time = view.findViewById(R.id.tv_duration);
        time.setText(String.format(getString(R.string.format_str), duration));

        float width = max / 20 * duration;
        FlexboxLayout.LayoutParams vl = new FlexboxLayout.LayoutParams((int) Math.max(width, min), height);
        vl.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(vl);
        viewGroup.addView(view);
    }

    private class CloseClick implements View.OnClickListener {

        private int index;

        public CloseClick(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            closeClickList.remove(index);
            playClickList.remove(index);
            viewGroup.removeViewAt(index);
            for (int i = 0; i < playClickList.size(); i++) {
                closeClickList.get(i).updateIndex(i);
            }
        }

        public void updateIndex(int i) {
            index = i;
        }
    }

    private class PlayClick implements View.OnClickListener {

        private final String path;
        private final ImageView iv;
        private boolean isPlay = false;

        public PlayClick(String path, ImageView iv) {
            this.path = path;
            this.iv = iv;
        }

        @Override
        public void onClick(View v) {
            try {
                if (!isPlay) {
                    for (PlayClick playClick : playClickList) {
                        playClick.reset();
                    }
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(mp -> reset());
                    mediaPlayer.start();
                    iv.setImageResource(R.drawable.ic_stop);
                    isPlay = true;
                } else {
                    mediaPlayer.stop();
                    iv.setImageResource(R.drawable.ic_play);
                    isPlay = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void reset() {
            iv.setImageResource(R.drawable.ic_play);
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.reset();
        mediaPlayer.release();
        FileUtils.clear();
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}