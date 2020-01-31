package com.crabglory.microvideo.micro.selector;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crabglory.microvideo.R;
import com.crabglory.microvideo.micro.VideoActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.loader.app.LoaderManager;


public class GalleryFragment extends BottomSheetDialogFragment
        implements VideoSelectorView.SelectedChangeListener, View.OnClickListener {
    private VideoSelectorView mGallery;
    private OnSelectedListener mListener;

    public GalleryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 获取我们的GalleryView
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        mGallery = root.findViewById(R.id.galleryView);
        root.findViewById(R.id.iv_cancel).setOnClickListener(this);
        root.findViewById(R.id.iv_select).setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGallery.setup(LoaderManager.getInstance(this), this);
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT; //可以写入自己想要的高度
        }
        final View view = getView();
        assert view != null;
        view.post(() -> {
            View parent = (View) view.getParent();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();
            BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
            assert bottomSheetBehavior != null;
            bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
            parent.setBackgroundColor(Color.WHITE);
        });

    }

    @Override
    public void onSelectedCountChanged(int count) {
        if (count > 0) {
            dismiss();
            if (mListener != null) {
                String[] paths = mGallery.getSelectedPath();
                mListener.onSelectedImage(paths[0]);
                mListener = null;
            }
        }
    }

    @Override
    public void openVideo(VideoSearcher.Video video) {
        Intent intent = new Intent(GalleryFragment.this.getActivity(), VideoActivity.class);
        intent.putExtra("path",video.path);
        startActivity(intent);
    }

    public GalleryFragment setListener(OnSelectedListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                GalleryFragment.this.dismiss();
                break;
            case R.id.iv_select:
                // select 结束
                GalleryFragment.this.dismiss();
                break;
            default:
                GalleryFragment.this.dismiss();
                break;
        }
    }


    /**
     * 选中图片的监听器
     */
    public interface OnSelectedListener {
        void onSelectedImage(String path);
    }


}
