package edu.feicui.videoplayer.full;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import edu.feicui.videoplayer.R;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity {

    private static final String VIDEO_PATH = "VIDEO_PATH";

    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoViewActivity.class);
        intent.putExtra(VIDEO_PATH, videoPath);
        context.startActivity(intent);
    }

    private VideoView videoView;
    private ImageView ivLoading; // 缓冲信息(图像)
    private TextView tvBufferInfo; // 缓冲信息(文本信息,显示78kb/s, 67%)
    private int downloadSpeed; // 当前缓冲速度
    private int bufferPercent; // 当前缓冲百分比
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        Vitamio.isInitialized(this);
        // 取消状态栏 ,设置视屏大小（为全屏的）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置窗口的背景色
        getWindow().setBackgroundDrawableResource(android.R.color.black);

        ivLoading = (ImageView) findViewById(R.id.ivLoading);
        tvBufferInfo = (TextView) findViewById(R.id.tvBufferInfo);
        ivLoading.setVisibility(View.GONE);
        tvBufferInfo.setVisibility(View.GONE);

        videoView = (VideoView) findViewById(R.id.videoView);
        initVideoView();

    }

    private void initVideoView() {
//        videoView.setMediaController(new MediaController(this));
        videoView.setMediaController(new MyMediaController(this));
        videoView.setKeepScreenOn(true);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                mediaPlayer.setBufferSize(512 * 1024);
            }
        });
        videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                bufferPercent = percent;
                String info = String.format(Locale.CHINA, "%d%%,%dkb/s", bufferPercent, downloadSpeed);
                tvBufferInfo.setText(info);
            }
        });
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        ivLoading.setVisibility(View.VISIBLE);
                        tvBufferInfo.setVisibility(View.VISIBLE);
                        bufferPercent = 0;
                        downloadSpeed = 0;
                        if (videoView.isPlaying()){
                            videoView.pause();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        ivLoading.setVisibility(View.GONE);
                        tvBufferInfo.setVisibility(View.GONE);
                        videoView.start();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        downloadSpeed = extra;
                        String info = String.format(Locale.CHINA, "%d%%,%dkb/s", bufferPercent, downloadSpeed);
                        tvBufferInfo.setText(info);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.setVideoPath(getIntent().getStringExtra(VIDEO_PATH));
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.stopPlayback();
    }
}
