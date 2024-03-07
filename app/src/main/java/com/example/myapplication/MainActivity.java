package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private PlayerView myExoPlayer;
    private ExoPlayer exoPlayer;

    private Button btnPlay, btnPause, btnMiddle;

    private String storyUrl, option1 = "1", option2 = "-1", option3 = "-1", subtitle;

    private String send = "";
    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时
            myExoPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏时
            myExoPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(250))); // 250dp是原始高度，可调整
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        myExoPlayer = findViewById(R.id.video_view);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnMiddle = findViewById(R.id.btnmiddle);

        // 设置视频播放器
        setupPlayer();

        // 隐藏按钮
        setButtonsVisibility(View.GONE);

        // 设置ExoPlayer事件监听器
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Player.Listener.super.onPlaybackStateChanged(state);
                // 当前视频播放结束
                if (state == Player.STATE_ENDED ) {
                    if (option1.equals("-2")) {
                        send += 7;
                        fetchStoryWithOptions(send);
                    } else if (!option1.equals("-1")) {
                        setButtonsVisibility(View.VISIBLE);
                    }

                } else {
                    //todo 进入下一步
                }
            }
        });

        btnPlay.setOnClickListener(view -> {
            send += 1;
            fetchStoryWithOptions(send);
            setButtonsVisibility(View.GONE);
        });
        btnPause.setOnClickListener(view -> {
            send += "3";
            fetchStoryWithOptions(send);
            setButtonsVisibility(View.GONE);
        });
        btnMiddle.setOnClickListener(view -> {
            send += "2";
            fetchStoryWithOptions(send);
            setButtonsVisibility(View.GONE);
        });

        fetchStoryWithOptions("0");
    }

    private void setupPlayer() {
        myExoPlayer.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        exoPlayer = new ExoPlayer.Builder(this).build();
        myExoPlayer.setPlayer(exoPlayer);

        // 开始播放第一个视频
        // playVideo("https://prod-streaming-video-msn-com.akamaized.net/e908e91f-370f-49ad-b4ce-775b7e7a05b4/a6287f74-46f0-42f9-b5d9-997f00585696.mp4");
    }

    private void playVideo(String url) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void setButtonsVisibility(int visibility) {
        btnPlay.setVisibility(visibility);
        btnPause.setVisibility(visibility);
        btnMiddle.setVisibility(visibility);
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查ExoPlayer实例是否存在并且视频没有结束
        if (exoPlayer != null && exoPlayer.getPlaybackState() != Player.STATE_ENDED) {
            // 继续播放视频
            exoPlayer.play();
        }
    }

    private void fetchStoryWithOptions(String selected) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.1.13:8080/getStoryWithOptions"; // 请替换为实际的URL

        // 创建POST请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("selected", selected)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败的情况
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 在这里解析和使用响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        storyUrl = jsonObject.getString("url");
                        option1 = jsonObject.getString("option1");
                        option2 = jsonObject.getString("option2");
                        option3 = jsonObject.getString("option3");
                        subtitle = jsonObject.getString("subtitle");

                        runOnUiThread(() -> {
                            // 更新UI元素，例如视频URL和选项按钮
                            // 这里假设有方法updateUI来处理UI更新
                            updateUI(storyUrl, option1, option2, option3);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateUI(String url, String option1, String option2, String option3) {
        playVideo(url);
        btnPlay.setText(option1);
        btnPause.setText(option3);
        btnMiddle.setText(option2);
    }


}