package com.crabglory.microvideo.micro.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;


import com.crabglory.microvideo.micro.utils.DensityUtils;

import androidx.appcompat.widget.AppCompatImageView;


/**
 * Description:
 */
public class CircularProgressView extends AppCompatImageView {

    private int mStroke = 5;
    private int mProcess = 0;
    private int mTotal = 100;
    private int mNormalColor = 0xFFFFFFFF;
    private int mSecondColor = 0xFFFEE300;
    private int mStartAngle = -90;
    private RectF mRectF;

    private Paint mPaint;
    private Drawable mDrawable;
    private OnRecordListener mListener;
    private CountDownTimer timer;

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStroke = DensityUtils.dp2px(getContext(), mStroke);
        mPaint = new Paint();
        mPaint.setColor(mNormalColor);
        mPaint.setStrokeWidth(mStroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mDrawable = new Progress();
        setImageDrawable(mDrawable);
    }


    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        this.mTotal = total;
        mDrawable.invalidateSelf();
        Log.e("CircularProgressView", "total-->" + total);

    }

    public void setProcess(int process) {
        this.mProcess = process;
        post(() -> mDrawable.invalidateSelf());
    }

    public int getProcess() {
        return mProcess;
    }

    public void setStroke(float dp) {
        this.mStroke = DensityUtils.dp2px(getContext(), dp);
        mPaint.setStrokeWidth(mStroke);
        mDrawable.invalidateSelf();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onRecordStart();
                break;
            case MotionEvent.ACTION_UP:
//                mListener.onRecordStop();
                break;
        }
        return true;
    }

    public void setOnRecordListener(OnRecordListener listener) {
        mListener = listener;
    }

    public interface OnRecordListener {
        void onRecordStart();

        void onRecordStop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    private class Progress extends Drawable {
        @Override
        public void draw(Canvas canvas) {
            int width = getWidth();
            int pd = mStroke / 2 + 1;
            if (mRectF == null) {
                mRectF = new RectF(pd, pd, width - pd, width - pd);
            }
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mNormalColor);
            canvas.drawCircle(width / 2, width / 2, width / 2 - pd, mPaint);
            mPaint.setColor(mSecondColor);
            canvas.drawArc(mRectF, mStartAngle, mProcess * 360 / (float) mTotal, false, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

}
