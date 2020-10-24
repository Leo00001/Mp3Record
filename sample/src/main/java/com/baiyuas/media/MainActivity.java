package com.baiyuas.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.baiyuas.record.MediaPlayUtil;
import com.baiyuas.record.Mp3Record;
import com.baiyuas.record.Mp3RecordListener;

import java.io.File;
import java.util.List;

/**
 * @author 拜雨
 */
public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, OnRecordListener{

    private static final int RC_STORAGE_AND_RECORD = 1;
    private TextView tvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayUtil.init(this);

        tvPath = findViewById(R.id.tv_path);
        findViewById(R.id.btn_start).setOnClickListener(v -> checkPermission());
        findViewById(R.id.btn_play).setOnClickListener(v -> {
            String path = tvPath.getText().toString();
            if (!TextUtils.isEmpty(path)) {
                MediaPlayUtil.playSound(path, mp -> Toast.makeText(this, "播放完成", Toast.LENGTH_SHORT).show());
            }
        });
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
        tvPath.setText(path);
    }
}