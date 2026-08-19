package com.linzp.forum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.linzp.forum.R;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.ui.login.LoginActivity;
import com.linzp.forum.ui.main.MainActivity;

/**
 * 启动页。
 * 停 1.2 秒：一是让 logo 露个脸，二是顺便判断登录态。
 */
public class SplashActivity extends AppCompatActivity {

    private static final long DELAY_MILLIS = 1200L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable jumpTask = new Runnable() {
        @Override
        public void run() {
            jumpNext();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(jumpTask, DELAY_MILLIS);
    }

    private void jumpNext() {
        if (isFinishing()) {
            return;
        }
        UserSession session = UserSession.getInstance(this);
        Intent intent;
        if (session.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // 页面销毁一定要把 handler 里的任务摘掉，否则 Activity 已经没了还会回调
        handler.removeCallbacks(jumpTask);
        super.onDestroy();
    }
}
