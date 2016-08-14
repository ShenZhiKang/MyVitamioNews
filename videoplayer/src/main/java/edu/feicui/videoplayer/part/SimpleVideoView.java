package edu.feicui.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import edu.feicui.videoplayer.R;
import edu.feicui.videoplayer.full.VideoViewActivity;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by DELL on 2016/8/11.
 */
public class SimpleVideoView extends FrameLayout {

    private String videoPath;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView ivPreview;
    private ImageButton btnToggle,btnFullScreen;
    private ProgressBar progressBar;

    private MediaPlayer mediaPlayer;

    private boolean isPlaying,isPrepared;

    private static final int PROGRESS_MAX = 1000;  //进度条最大值

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public SimpleVideoView(Context context) {
        super(context, null);
    }

    public SimpleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SimpleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.video_player_view, this, true);
        Vitamio.isInitialized(getContext());
        initSurfaceView();
        initControllerView();
    }

    private void initSurfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    private void initControllerView() {
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        //暂停
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pausePlayer();
                } else if (isPrepared) {
                    startPlayer();
                } else {
                    Toast.makeText(getContext(), "Can't play now !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //全屏
        btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
        btnFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoViewActivity.open(getContext(), videoPath);
            }
        });
        //进度条
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(PROGRESS_MAX);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(getContext());
        mediaPlayer.setDisplay(surfaceHolder);  //设置显示
        //视频准备好start后的监听
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                startPlayer();
            }
        });
        //视频大小改变的监听
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                // 根据宽的数据,去适配高的数据
                int videoWidth = surfaceView.getWidth();
                int videoHieght = videoWidth * height / width;
                // 重置surfaceview宽高
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                layoutParams.width = videoWidth;
                layoutParams.height = videoHieght;
                surfaceView.setLayoutParams(layoutParams);
            }
        });
        //用于记录录制时出现的信息事件
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    return true;
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    return true;
                }
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK) {
                    // 注意：Vitamio5.0 要对音频进行设置才能播放
                    // 否则，不能播放在线视频
                    long size = mediaPlayer.audioTrackInit();
                    mediaPlayer.audioInitedOk(size);
                    return true;
                }
                return false;
            }
        });
    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isPlaying){
                long current = mediaPlayer.getCurrentPosition();
                long duration = mediaPlayer.getDuration();  //获取音频的时长
                int progress = (int) (current * PROGRESS_MAX / duration);
                progressBar.setProgress(progress);
                handler.sendEmptyMessageDelayed(0, 200);
            }
        }
    };

    private void startPlayer() {
        if (isPrepared){
            mediaPlayer.start();
        }
        isPlaying = true;
        handler.removeMessages(0);
        btnToggle.setImageResource(R.drawable.ic_pause);
    }

    private void pausePlayer() {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        isPlaying = false;
        handler.removeMessages(0);
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
    }

    public void onResume(){
        //初始化mediaPlayer及监听
        initMediaPlayer();
        //设置资源进行准备
        prepareMediaPlayer();
    }

    public void onPause(){
        //暂停mediaPlayer
        pausePlayer();
        //释放MediaPlayer
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        mediaPlayer.release();
        mediaPlayer = null;
        isPrepared = false;
        isPlaying = false;
    }

    private void prepareMediaPlayer() {
        try {
            //为了重用处于Error错误状态的MediaPlayer对象
            mediaPlayer.reset();
            // 设置资源
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setLooping(true);
            // 异步准备
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
