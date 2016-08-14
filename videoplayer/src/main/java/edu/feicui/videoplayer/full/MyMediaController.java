package edu.feicui.videoplayer.full;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import edu.feicui.videoplayer.R;
import io.vov.vitamio.widget.MediaController;

/**
 * Created by DELL on 2016/8/11.
 */
public class MyMediaController extends MediaController {

    private MediaPlayerControl mediaPlayerControl;

    private final AudioManager audioManager;  // 用来调整音量的(管理)
    private Window window;  // 用来调整亮度的(管理)
    private final int maxVolum;  // 最大音量(获取到的)
    private int currentVolume;  // 当前音量(在开始滑动手势时的音量)
    private float currentBrightness;  // 当前亮度(在开始滑动手势时的亮度(0.0f - 1.0f,如果是负代表自动调整))

    public MyMediaController(Context context) {
        super(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        window = ((Activity) context).getWindow();

//        //设置默认音量 50%
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolum/2, AudioManager.FLAG_SHOW_UI);
//        //设置默认亮度 50%
//        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.screenBrightness = 0.5f;
//        window.setAttributes(layoutParams);
    }

    @Override
    protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.video_controller_view, this);
        initView(view);
        return view;
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        this.mediaPlayerControl = player;
    }

    private void initView(View view) {
        ImageButton btnFastRewind = (ImageButton) findViewById(R.id.btnFastRewind);
        btnFastRewind.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long position = mediaPlayerControl.getCurrentPosition();
                position -= 10000;
                mediaPlayerControl.seekTo(position);
            }
        });

        ImageButton btnFastForward = (ImageButton) findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long position = mediaPlayerControl.getCurrentPosition();
                position += 10000;
                mediaPlayerControl.seekTo(position);
            }
        });

        final View adjustView = view.findViewById(R.id.adjustView);
        final GestureDetector gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        float startX = e1.getX();
                        float startY = e1.getY();
                        float endX = e2.getX();
                        float endY = e2.getY();
                        float width = adjustView.getWidth();
                        float height = adjustView.getHeight();
                        float percentage = (startY - endY) / height;

                        if (startX < width / 4){
                            adjustBrightness(percentage);
                            return true;
                        }else if (startX > width * 3 /4){
                            adjustVolume(percentage);
                            return true;
                        }
                        return false;
                    }
                });
        adjustView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 按下事件时(也代表着马上将开始手势处理)获取到当前音量及亮度
                // 使用ACTION_MASK是为了过滤掉多点触屏事件
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN){
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentBrightness = window.getAttributes().screenBrightness;
                }
                gestureDetector.onTouchEvent(event);
                // 为了在调整过程中，不消失
                MyMediaController.this.show();
                return true;
            }
        });
    }

    private void adjustVolume(float percentage) {
        int volume = (int) ((percentage * maxVolum) + currentVolume);
        volume = volume > maxVolum ? maxVolum : volume;
        volume = volume < 0 ? 0 : volume;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }

    private void adjustBrightness(float percentage) {
        float brightness = percentage + currentBrightness;
        brightness = brightness > 1.0f ? 1.0f : brightness;
        brightness = brightness < 0.1f ? 0.1f : brightness;
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness;
        window.setAttributes(layoutParams);
    }
}
