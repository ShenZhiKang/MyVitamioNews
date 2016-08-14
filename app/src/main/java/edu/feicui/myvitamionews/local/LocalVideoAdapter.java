package edu.feicui.myvitamionews.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by DELL on 2016/8/14.
 */
public class LocalVideoAdapter extends CursorAdapter {
    // 用来加载生成视频预览图的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(5 * 1024 * 1024){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public LocalVideoAdapter(Context context) {
        super(context, null, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new LocalVidioView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final LocalVidioView localVidioView = (LocalVidioView) view;
        localVidioView.bind(cursor);
        final String filePath = localVidioView.getFilePath();
        // 从缓存中获取预览图
        final Bitmap bitmap = lruCache.get(filePath);
        if (bitmap!=null){
            // 设置当前视图预览图
            localVidioView.setPreview(bitmap);
        }else {    // 缓存中没有预览图,后台线程进行预览图加载
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap1 = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                    lruCache.put(filePath, bitmap1);
                    localVidioView.setPreview(filePath, bitmap1);
                }
            });
        }
    }

    public void release(){
        executorService.shutdown();
    }
}
