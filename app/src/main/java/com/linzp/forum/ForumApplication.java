package com.linzp.forum;

import android.app.Application;
import android.content.Context;

import com.linzp.forum.data.repository.UserRepository;

/**
 * 全局 Application。
 * 尽早把演示账号准备好，不然用户装完打开发现登不进去。
 */
public class ForumApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        initDemoAccount();
    }

    private void initDemoAccount() {
        // 这里放主线程做 IO 不太好，但只有第一次装的时候跑一次，数据量也小，
        // 先这么放着，等接了后端这段直接删掉。
        UserRepository.getInstance(this).ensureDemoUsers();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
