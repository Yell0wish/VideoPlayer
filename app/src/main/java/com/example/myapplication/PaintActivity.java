package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class PaintActivity extends AppCompatActivity {
    private DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        drawingView = findViewById(R.id.drawing_view);

        Button clearButton = findViewById(R.id.btn_clear);
        Button colorButton = findViewById(R.id.btn_color);
        Button eraseButton = findViewById(R.id.btn_erase);

        // 清屏按钮
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());

        // 更换颜色按钮（示例为红色）
        colorButton.setOnClickListener(v -> showColorPicker());

        eraseButton.setOnClickListener(v -> {
            drawingView.enableEraser();
        });


        Button saveButton = findViewById(R.id.btn_save); // 假设你的布局文件中有一个保存按钮
        saveButton.setOnClickListener(v -> {
            Bitmap bitmap = drawingView.getBitmap();
            String base64String = convertToBase64(bitmap);
            // 现在base64String包含了画布内容的Base64字符串
            // 这里可以根据需要处理这个字符串，例如显示、发送到服务器等
//            Log.d("base64", base64String);
            fetchStoryWithOptions(base64String);
        });

        // 根据需要添加更多按钮的监听器，例如保存画图、更换画笔粗细等
    }


    private void showColorPicker() {
        int initialColor = drawingView.getColor(); // 获取当前画笔颜色
        int initialStrokeWidth = drawingView.getPaintStrokeWidth(); // 获取当前画笔宽度

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.color_picker_dialog); // 使用自定义布局文件

        View colorPickerView = dialog.findViewById(R.id.color_picker_view);
        colorPickerView.setBackgroundColor(initialColor); // 设置颜色视图的背景为当前画笔颜色

        final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                drawingView.setColor(color); // 设置所选颜色
                colorPickerView.setBackgroundColor(color); // 更新颜色视图的背景颜色
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // 对话框被取消
            }
        });

        SeekBar strokeWidthSeekBar = dialog.findViewById(R.id.stroke_width_seekbar);
        strokeWidthSeekBar.setProgress(initialStrokeWidth);
        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setPaintStrokeWidth(progress); // 实时更新画笔宽度
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        colorPickerView.setOnClickListener(v -> colorPicker.show()); // 点击颜色视图时显示颜色选择器
        dialog.show();
    }

    public String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // 压缩位图
        byte[] imageBytes = outputStream.toByteArray(); // 转换为字节数组
        return Base64.encodeToString(imageBytes, Base64.DEFAULT); // 将字节数组编码为字符串
    }


    private void fetchStoryWithOptions(String pic_ori) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://handsomegxc.natapp1.cc/getModifiedPicture/"; // 请替换为实际的URL
        // 创建POST请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("pic_ori", pic_ori)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d("EERRR", "yes");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败的情况
                Log.d("EERRR", "coming");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 在这里解析和使用响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        Log.d("returnMSG", jsonObject.getString("msg"));
                        JSONObject data = jsonObject.getJSONObject("data"); // 获取嵌套的"data"对象
                        String pic_now = data.getString("url");
                        fetchStoryWithOptions1(pic_now);
//                        String passed = data.getString("passed");
//                        if (passed.equals("1")) {
//                            passed = "闯关成功";
//                        } else {
//                            passed = "闯关失败TAT";
//                        }
//                        String finalPassed = passed;
                        runOnUiThread(() -> {
                            Log.d("ERRRR", pic_now);
                            showImageInNewWindow(pic_now); // 在这里调用方法显示图片
//                            showToastMessage(finalPassed);
                        });
                    } catch (JSONException e) {
                        Log.e("JSONError", "JSON parsing error", e);
                        e.printStackTrace();
                    }
                } else {
                    // 打印出详细的错误信息
                    Log.d("EERRR", "HTTP Status Code: " + response.code());
                    Log.d("EERRR", "Response message: " + response.message());

                    if (response.body() != null) {
                        String errorBody = response.body().string();
                        Log.d("EERRR", "Error response body: " + errorBody);
                    }

//                    Log.d("EERRR", "aaas");
                }
            }
        });
    }
    private void showImageInNewWindow(String imageUrl) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image);

        ImageView imageView = dialog.findViewById(R.id.dialog_image_view);

        // 使用Picasso加载图片到ImageView
        Picasso.get().load(imageUrl).into(imageView);

        // 点击图片关闭Dialog
        imageView.setOnClickListener(v -> dialog.dismiss());

        // 显示对话框
        dialog.show();
    }




    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchStoryWithOptions1(String pic_url) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://handsomegxc.natapp1.cc/picture/recognition"; // 请替换为实际的URL

        // 创建 JSON 对象并放入 image_url
        JSONObject json = new JSONObject();
        try {
            json.put("image_url", pic_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 将 JSON 对象转换为字符串
        String jsonString = json.toString();

        // 创建 JSON 的 MediaType
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        // 创建 POST 请求体
        RequestBody requestBody = RequestBody.create(jsonString, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d("EERRR", "yes");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理请求失败的情况
                Log.d("EERRR", "coming");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 在这里解析和使用响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String result = jsonObject.getString("msg");
                        Log.d("returnMSG", jsonObject.getString("msg"));
                        // 根据需要处理响应
                        runOnUiThread(() -> {
                            if (!result.equals("ok")) {
                                // todo 跳转到视频播放界面 字符串 += 1
                                String globalString = ((MyApp) getApplication()).getGlobalString();
                                globalString += "1";
                                ((MyApp) getApplication()).setGlobalString(globalString);
                                Intent intent = new Intent(PaintActivity.this, VideoActivity.class);
                                startActivity(intent);
                                finish(); // 关闭当前活动

                            } else {
                                // 播放音乐重画
                                MediaPlayer mediaPlayer = MediaPlayer.create(PaintActivity.this, R.raw.fail); // 假设您的音乐文件名为 music.mp3
                                mediaPlayer.start(); // 开始播放
                                showToastMessage("闯关失败，请再试一次吧TAT");
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        // 音乐播放完毕后的操作
                                        mp.release(); // 释放资源
                                    }
                                });
                            }
                            Log.d("ERRRR", responseBody);
                            // 根据实际情况显示图片或处理数据
                        });
                    } catch (JSONException e) {
                        Log.e("JSONError", "JSON parsing error", e);
                        e.printStackTrace();
                    }
                } else {
                    // 打印出详细的错误信息
                    Log.d("EERRR", "HTTP Status Code: " + response.code());
                    Log.d("EERRR", "Response message: " + response.message());

                    if (response.body() != null) {
                        String errorBody = response.body().string();
                        Log.d("EERRR", "Error response body: " + errorBody);
                    }
                }
            }
        });
    }


}