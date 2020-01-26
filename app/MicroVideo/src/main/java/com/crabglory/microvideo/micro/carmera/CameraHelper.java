package com.crabglory.microvideo.micro.carmera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

public class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = "CameraHelper";
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static int mCameraId;
    private static Camera mCamera;
    private static byte[] buffer;
    private static Camera.PreviewCallback mPreviewCallback;
    private static SurfaceTexture mSurfaceTexture;

    public CameraHelper(int cameraId) {
        mCameraId = cameraId;
    }

    public static void switchCamera() {
        mCameraId = (mCameraId == 0 ? 1:0);
        stopPreview();
        startPreview(mSurfaceTexture);
    }

    public int getCameraId() {
        return mCameraId;
    }

    private static void stopPreview() {
        if (mCamera != null) {
            //预览数据回调接口
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }

    public static void startPreview(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        try {
            //获得camera对象
            mCamera = Camera.open(mCameraId);
            //配置camera的属性
            Camera.Parameters parameters = mCamera.getParameters();
            //设置预览数据格式为nv21
            parameters.setPreviewFormat(ImageFormat.NV21);
            //这是摄像头宽、高
            parameters.setPreviewSize(WIDTH, HEIGHT);
            // 设置摄像头 图像传感器的角度、方向
            mCamera.setParameters(parameters);
            buffer = new byte[WIDTH * HEIGHT * 3 / 2];
            //数据缓存区
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mPreviewCallback = this;
                }
            });
            //设置预览画面
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // data数据依然是倒的
        if (null != mPreviewCallback) {
            mPreviewCallback.onPreviewFrame(data, camera);
        }
        camera.addCallbackBuffer(buffer);
    }


}
