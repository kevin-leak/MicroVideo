package com.crabglory.microvideo.micro.widget;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.crabglory.microvideo.micro.carmera.CameraHelper;
import com.crabglory.microvideo.micro.carmera.ScreenFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.RequiresApi;

public class MicroVideoUp extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    private CameraHelper mCameraHelper;
    private int[] mTextures;
    private SurfaceTexture mSurfaceTexture;
    private ScreenFilter mScreenFilter;
    private float[] mtx = new float[16];

    public MicroVideoUp(Context context) {
        super(context);
        init();
    }

    public MicroVideoUp(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);//设置EGL版本
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //初始化的操作
        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK);
        //准备好摄像头绘制的画布
        //通过opengl创建一个纹理id
        mTextures = new int[1];
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new OutlineProvider(5));
        }
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mScreenFilter = new ScreenFilter(getContext());//注意：必须在gl线程操作opengl
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //开启预览
        CameraHelper.startPreview(mSurfaceTexture);
        mScreenFilter.onReady(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 把摄像头的数据先输出来
        // 更新纹理，然后我们才能够使用opengl从SurfaceTexure当中获得数据 进行渲染
        mSurfaceTexture.updateTexImage();
        //surfaceTexture 比较特殊，在opengl当中 使用的是特殊的采样器 samplerExternalOES （不是sampler2D）
        //获得变换矩阵
        mSurfaceTexture.getTransformMatrix(mtx);

        mScreenFilter.onDrawFrame(mTextures[0], mtx);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class OutlineProvider extends ViewOutlineProvider {
        private float mRadius;

        public OutlineProvider(float radius) {
            this.mRadius = radius;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin,
                    rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
            outline.setRoundRect(selfRect, mRadius);
        }
    }
}
