package com.crabglory.microvideo.micro.VideoSelector;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crabglory.microvideo.R;
import com.crabglory.microvideo.micro.VideoSelector.recycler.RecyclerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 1. 可选项的布局
 * 2. holder的指定
 * 3. 资源加载抽象封装
 */
public class VideoSelectorView extends RecyclerView {

    private Adapter mAdapter = new Adapter();
    private LoaderCallback mLoaderCallback = new LoaderCallback();
    private int LOADER_ID = 0x0100;
    private SelectedChangeListener mListener;
    private List<Video> mSelectedVideo = new LinkedList<>();
    private static final int MAX_VIDEO_COUNT = 3; // 最大选中图片数量

    public VideoSelectorView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoSelectorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoSelectorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    /**
     * 用于建立起加载器
     *
     * @param loaderManager 加载管理器
     * @param listener      监听选择的状态
     * @return loader id
     */
    public int setup(LoaderManager loaderManager, SelectedChangeListener listener) {
        mListener = listener;
        loaderManager.initLoader(LOADER_ID, null, mLoaderCallback);
        return LOADER_ID;
    }


    void init() {
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(mAdapter);
        mAdapter.setListener(new RecyclerAdapter.AdapterListenerImpl<Video>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, Video video) {
                // Cell点击操作，如果说我们的点击是允许的，那么更新对应的Cell的状态
                // 然后更新界面，同理；如果说不能允许点击（已经达到最大的选中数量）那么就不刷新界面
                if (onItemSelectClick(video)) {
                    //noinspection unchecked
                    holder.updateData(video);
                }
            }
        });
    }

    /**
     * Cell点击的具体逻辑
     *
     * @param video Image
     * @return True，代表我进行了数据更改，你需要刷新；反之不刷新
     */
    private boolean onItemSelectClick(Video video) {
        // 是否需要进行刷新
        boolean notifyRefresh;
        if (mSelectedVideo.contains(video)) {
            // 如果之前在那么现在就移除
            mSelectedVideo.remove(video);
            video.isSelect = false;
            // 状态已经改变则需要更新
            notifyRefresh = true;
        } else {
            if (mSelectedVideo.size() >= MAX_VIDEO_COUNT) {
                // 得到提示文字
                String str = getResources().getString(R.string.label_gallery_select_max_size);
                // 格式化填充
                str = String.format(str, MAX_VIDEO_COUNT);
                Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                notifyRefresh = false;
            } else {
                mSelectedVideo.add(video);
                video.isSelect = true;
                notifyRefresh = true;
            }
        }

        // 如果数据有更改，
        // 那么我们需要通知外面的监听者我们的数据选中改变了
        if (notifyRefresh)
            notifySelectChanged();
        return true;
    }

    /**
     * 通知选中状态改变
     */
    private void notifySelectChanged() {
        // 得到监听者，并判断是否有监听者，然后进行回调数量变化
        SelectedChangeListener listener = mListener;
        if (listener != null) {
            listener.onSelectedCountChanged(mSelectedVideo.size());
        }
    }

    /**
     * 用于实际的数据加载的Loader Callback
     */
    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String[] IMAGE_PROJECTION = new String[]{
                MediaStore.Video.Media._ID, // Id
                MediaStore.Video.Media.DATA, // 图片路径
                MediaStore.Video.Media.DATE_ADDED, // 图片的创建时间ø
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // 创建一个Loader
            if (id == LOADER_ID) {
                // 如果是我们的ID则可以进行初始化
                return new CursorLoader(getContext(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        null,
                        null,
                        IMAGE_PROJECTION[2] + " DESC"); // 倒序查询
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // 当Loader加载完成时
            List<Video> videos = new ArrayList<>();
            // 判断是否有数据
            if (data != null) {
                int count = data.getCount();
                if (count > 0) {
                    // 移动游标到开始
                    data.moveToFirst();
                    // 得到对应的列的Index坐标
                    int indexId = data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]);
                    int indexPath = data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]);
                    int indexDate = data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]);
                    int indexDuration = data.getColumnIndex(IMAGE_PROJECTION[3]);
                    int indexName = data.getColumnIndex(IMAGE_PROJECTION[4]);

                    do {
                        // 循环读取，直到没有下一条数据
                        int id = data.getInt(indexId);
                        String path = data.getString(indexPath);
                        long dateTime = data.getLong(indexDate);
                        String name = data.getString(indexName);
                        long duration = data.getLong(indexDuration);
                        // 添加一条新的数据
                        Video video = new Video();
                        video.name = name;
                        video.id = id;
                        video.path = path;
                        video.date = dateTime;
                        video.duration = duration;
                        videos.add(video);
                    } while (data.moveToNext());
                }
            }
            updateSource(videos);
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // 当Loader销毁或者重置了, 进行界面清空
            updateSource(null);
        }
    }


    private void updateSource(List<Video> video) {
        mAdapter.replace(video);
    }


    /**
     * 得到选中的图片的全部地址
     *
     * @return 返回一个数组
     */
    public String[] getSelectedPath() {
        String[] paths = new String[mSelectedVideo.size()];
        int index = 0;
        for (Video image : mSelectedVideo) {
            paths[index++] = image.path;
        }
        return paths;
    }


    private class Video {
        public int id;
        public long duration;
        String name;
        String path;
        long date;
        boolean isSelect;


        /**
         * 为了使得list里面不重复加入当前的image对象,重写，同时为了保持一致，重写hashcode
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Video image = (Video) o;

            return path != null ? path.equals(image.path) : image.path == null;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }

    }

    private class ViewHolder extends RecyclerAdapter.ViewHolder<Video> {

        private ImageView mPic;
        private CheckBox mSelected;
        private final TextView time;
        private final TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            mPic = itemView.findViewById(R.id.im_image);
            mSelected = itemView.findViewById(R.id.cb_select);
            name = itemView.findViewById(R.id.tv_name);
            time = itemView.findViewById(R.id.tv_time);
        }

        @Override
        protected void onBind(Video video) {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(video.path);// videoPath 本地视频的路径
            Bitmap bitmap = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            mPic.setImageBitmap(bitmap);//对应的ImageView
            mSelected.setChecked(video.isSelect);
            mSelected.setVisibility(VISIBLE);
            name.setText(video.name.substring(0, video.name.lastIndexOf(".")));
            time.setText(getTime(video.duration));
        }

        private String getTime(long duration) {

            return duration / (60 * 60 * 60) + ":" + duration / (60 * 60);
        }
    }

    private class Adapter extends RecyclerAdapter<Video> {

        @Override
        protected int getItemViewType(int position, Video image) {
            return R.layout.cell_galley;
        }

        @Override
        protected ViewHolder<Video> onCreateViewHolder(View root, int viewType) {
            return new VideoSelectorView.ViewHolder(root);
        }
    }


    public interface SelectedChangeListener {
        void onSelectedCountChanged(int count);
    }
}
