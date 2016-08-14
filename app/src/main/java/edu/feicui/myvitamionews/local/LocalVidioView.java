package edu.feicui.myvitamionews.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import edu.feicui.myvitamionews.R;
import edu.feicui.videoplayer.full.VideoViewActivity;

/**
 * Created by DELL on 2016/8/14.
 */
public class LocalVidioView extends FrameLayout {

    private ImageView ivPreview;  // 视频预览图
    private TextView tvVideoName;  // 视频名称
    private String filePath; // 本地视频文件路径

    public LocalVidioView(Context context) {
        super(context, null);
    }

    public LocalVidioView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public LocalVidioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.item_local_video, this);
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        tvVideoName = (TextView) findViewById(R.id.tvVideoName);

        ivPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoViewActivity.open(getContext(), filePath);
            }
        });
    }

    public void bind(Cursor cursor){
        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        String videoName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        tvVideoName.setText(videoName);
        // 清除old预览图
        ivPreview.setImageBitmap(null);
    }

    public void setPreview(Bitmap bitmap){
        ivPreview.setImageBitmap(bitmap);
    }

    public void setPreview(final String filePath,final Bitmap bitmap){
        if (!filePath.equals(this.filePath)){
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                // 二次确认
                if (!filePath.equals(LocalVidioView.this.filePath)){
                    return;
                }
                ivPreview.setImageBitmap(bitmap);
            }
        });
    }

    public String getFilePath() {
        return filePath;
    }

}
