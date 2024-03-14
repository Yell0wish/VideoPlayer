package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class VideoActivity extends AppCompatActivity {
    private String currentPlayingUrl = null;
    private boolean begin_1 = true;
    private boolean choice2_times = false;
    private PlayerView myExoPlayer;
    private ExoPlayer exoPlayer;

    private boolean isVideoEnded = false;

    private Button btnPlay, btnPause, btnMiddle;

    private String storyUrl, option1 = "1", option2 = "-1", option3 = "-1";
    private String url_problem, url_solution;
    int answer;
    private String nextType = "1";

    private Player.Listener currentPlayerListener;

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
        setContentView(R.layout.activity_video);
        // 初始化

        // 初始化UI组件
        myExoPlayer = findViewById(R.id.video_view);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnMiddle = findViewById(R.id.btnmiddle);

        btnPlay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 设置文字大小为 12sp
        btnPause.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 设置文字大小为 12sp
        btnMiddle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // 设置文字大小为 12sp

        // 设置视频播放器
        setupPlayer();

        // 隐藏按钮
        setButtonsVisibility(View.GONE);

        btnPlay.setOnClickListener(view -> {
            if (choice2_times) {
                choice2_times = false;
                if (answer != 1) {
                    runOnUiThread(() -> {
                        // 更新UI元素，例如视频URL和选项按钮
                        // 这里假设有方法updateUI来处理UI更新
                        setButtonsVisibility(View.GONE);
                        playVideo(url_solution);
                        answer = 1;
                        //后面代码跳过
                    });
                    return;
                }
            }
            String globalString = ((MyApp) getApplication()).getGlobalString();
            globalString += "1";
            fetchStoryWithOptions(globalString);
            ((MyApp) getApplication()).setGlobalString(globalString);
            setButtonsVisibility(View.GONE);
        });
        btnPause.setOnClickListener(view -> {
            if (choice2_times) {
                choice2_times = false;
                if (answer != 3) {
                    runOnUiThread(() -> {
                        // 更新UI元素，例如视频URL和选项按钮
                        // 这里假设有方法updateUI来处理UI更新
                        setButtonsVisibility(View.GONE);
                        playVideo(url_solution);
                        answer = 1;
                        //后面代码跳过
                    });
                    return;
                } else {
                    String globalString = ((MyApp) getApplication()).getGlobalString();
                    globalString += "1";
                    fetchStoryWithOptions(globalString);
                    setButtonsVisibility(View.GONE);
                }
            }
            String globalString = ((MyApp) getApplication()).getGlobalString();
            globalString += "3";
            fetchStoryWithOptions(globalString);
            setButtonsVisibility(View.GONE);
        });
        btnMiddle.setOnClickListener(view -> {
            if (choice2_times) {
                choice2_times = false;
                if (answer != 2) {
                    runOnUiThread(() -> {
                        // 更新UI元素，例如视频URL和选项按钮
                        // 这里假设有方法updateUI来处理UI更新
                        setButtonsVisibility(View.GONE);
                        playVideo(url_solution);
                        answer = 1;
                        //后面代码跳过
                    });
                    return;
                } else {
                    String globalString = ((MyApp) getApplication()).getGlobalString();
                    globalString += "1";
                    fetchStoryWithOptions(globalString);
                    setButtonsVisibility(View.GONE);
                }
            }
            String globalString = ((MyApp) getApplication()).getGlobalString();
            globalString += "2";
            fetchStoryWithOptions(globalString);
            setButtonsVisibility(View.GONE);
        });

        fetchStoryWithOptions(((MyApp) getApplication()).getGlobalString());
    }

    private void setupPlayer() {
        myExoPlayer.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        exoPlayer = new ExoPlayer.Builder(this).build();
        myExoPlayer.setPlayer(exoPlayer);

        setListener();
        // 开始播放第一个视频
        // playVideo("https://prod-streaming-video-msn-com.akamaized.net/e908e91f-370f-49ad-b4ce-775b7e7a05b4/a6287f74-46f0-42f9-b5d9-997f00585696.mp4");
    }

    public void setListener() {
        currentPlayerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Player.Listener.super.onPlaybackStateChanged(state);
                isVideoEnded = (state == Player.STATE_ENDED);
                if (isVideoEnded) {
                    if (!nextType.equals("0")) {
                        if (nextType.equals("1") || nextType.equals("3")) {
                            setButtonsVisibility(View.VISIBLE);
                        } else if (nextType.equals("2")) {
                            if (currentPlayingUrl.equals(storyUrl)) {
                                return;
                            } else if (currentPlayingUrl.equals(url_problem)) {
                                setButtonsVisibility(View.VISIBLE);
                            } else if (currentPlayingUrl.equals(url_solution)) {
                                String globalString = ((MyApp) getApplication()).getGlobalString();
                                globalString += "1";
                                fetchStoryWithOptions(globalString);
                                ((MyApp) getApplication()).setGlobalString(globalString);
                                setButtonsVisibility(View.GONE);
                            }
                        } else if (nextType.equals("4")) {
                            Intent intent = new Intent(VideoActivity.this, PaintActivity.class);
                            startActivity(intent);
                            finish(); // 关闭当前活动
                        } else if (nextType.equals("3")) {

                        }
                    } else {
                        //todo 进入下一步
                        // 我也不知道要干什么
                        // 反正就是整个流程走完了
                    }
                }
            }
        };
        exoPlayer.addListener(currentPlayerListener);
    }

    private void playVideo(String url) {
        currentPlayingUrl = url;
        exoPlayer.setMediaItem(MediaItem.fromUri(url));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void setButtonsVisibility(int visibility) {
        if (!btnPlay.getText().equals("-1")) {
            btnPlay.setVisibility(visibility);
        }
        if (!btnMiddle.getText().equals("-1")) {
            btnMiddle.setVisibility(visibility);
        }
        if (!btnPause.getText().equals("-1")) {
            btnPause.setVisibility(visibility);
        }
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
        String url = "http://handsomegxc.natapp1.cc/getStoryWithOptions"; // 请替换为实际的URL

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
                        JSONObject data = jsonObject.getJSONObject("data");
                        storyUrl = data.getString("url");
                        option1 = data.getString("option1");
                        option2 = data.getString("option2");
                        option3 = data.getString("option3");
                        nextType = data.getString("nextType");

                        if (nextType.equals("2")) {
                            fetchMathProblem();
                        }
                        Log.d("I am testing", nextType);
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

    private void fetchMathProblem() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(1200, TimeUnit.SECONDS)
                .connectTimeout(1200, TimeUnit.SECONDS)
                .build();
        String url = "http://handsomegxc.natapp1.cc/getMathProblem";

        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d("I am testing", "coming");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("I am testing", "Request failed1: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject data = jsonObject.getJSONObject("data");
                        url_problem = data.getString("url_problem");
                        url_solution = data.getString("url_solution");
                        option1 = data.getString("option1");
                        option2 = data.getString("option2");
                        option3 = data.getString("option3");
                        answer = data.getInt("answer");
                        Log.d("I am testing", "answer: " + answer);
                        Log.d("I am testing", "url: " + url_problem);
                        if (isVideoEnded) {
                            runOnUiThread(() -> {
                                // 更新UI元素，例如视频URL和选项按钮
                                // 这里假设有方法updateUI来处理UI更新
                                choice2_times = true;
                                updateUI(url_problem, option1, option2, option3);
                            });
                        } else {
                            if (currentPlayerListener != null) {
                                exoPlayer.removeListener(currentPlayerListener);
                            }
                            // 设置新的监听器
                            currentPlayerListener = new Player.Listener() {
                                @Override
                                public void onPlaybackStateChanged(int state) {
                                    if (state == Player.STATE_ENDED) {
                                        // 处理视频播放结束后的逻辑
                                        runOnUiThread(() -> {
                                            // 更新UI元素，例如视频URL和选项按钮
                                            // 这里假设有方法updateUI来处理UI更新
                                            updateUI(url_problem, option1, option2, option3);
                                            choice2_times = true;
                                            exoPlayer.removeListener(currentPlayerListener);
                                            setListener();
                                        });
                                    }
                                }
                            };
                            exoPlayer.addListener(currentPlayerListener);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 打印失败的状态码和状态消息
                    Log.e("I am testing", "Request Failed. Code: " + response.code() + " Message: " + response.message());

                    // 尝试获取错误响应的详细信息
                    ResponseBody errorBody = response.body();
                    if (errorBody != null) {
                        String errorContent = errorBody.string(); // 获取错误响应体内容
                        Log.e("I am testing", "Error Response Body: " + errorContent);
                    }
                }
            }
        });
    }


}