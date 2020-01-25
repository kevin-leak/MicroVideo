package com.crabglory.microvideo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.crabglory.microvideo.micro.VideoSelector.GalleryFragment;
import com.crabglory.microvideo.micro.carmera.CameraHelper;
import com.crabglory.microvideo.micro.widget.CircularProgressView;
import com.crabglory.microvideo.micro.widget.MicroVideoUp;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private MicroVideoUp mvuRecord;
    private ImageView btnCameraSwitch;
    private CircularProgressView progressView;
    private ImageView finish;
    private RoundedImageView upVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCameraSwitch = findViewById(R.id.btn_camera_switch);
        mvuRecord = findViewById(R.id.mvu_record);
        progressView = findViewById(R.id.mCapture);
        finish = findViewById(R.id.finish);
        upVideo = findViewById(R.id.iv_preview);

        progressView.setTotal(10);
        finish.setOnClickListener(this);
        mvuRecord.setOnLongClickListener(v -> {
            new Handler().postDelayed(() ->
                    progressView.setProcess(progressView.getProcess() + 1), 1);
            return true;
        });
        btnCameraSwitch.setOnClickListener(this);
        upVideo.setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_camera_switch:
                CameraHelper.switchCamera();
                break;
            case R.id.iv_preview:
                showVideoList();
                break;
            case R.id.finish:
                finish();
                break;
        }
    }

    private void showVideoList() {
        new GalleryFragment().setListener(path -> {
                    Toast.makeText(MainActivity.this, path, Toast.LENGTH_SHORT).show();
                }).show(getSupportFragmentManager(), GalleryFragment.class.getName());
    }
}
