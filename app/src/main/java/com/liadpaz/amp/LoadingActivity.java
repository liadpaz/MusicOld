package com.liadpaz.amp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.liadpaz.amp.notification.MediaNotification;
import com.liadpaz.amp.utils.LocalFiles;

import java.util.concurrent.TimeUnit;

public class LoadingActivity extends AppCompatActivity {
    private static final String TAG = "AmpApp.LoadingActivity";

    private static final int REQUEST_PERMISSION = 459;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            new Handler().postDelayed(this::initializeView, TimeUnit.SECONDS.toMillis(1));
        }

        //        QueueUtil.observeQueue(this, queue -> {
        //            if (queue.size() == 0) {
        //                new Handler().postDelayed(this::initializeView, TimeUnit.SECONDS.toMillis(1));
        //            }
        //        });

        MediaNotification.init(this);
        LocalFiles.init(this);
    }

    private void initializeView() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeView();
        }
    }
}