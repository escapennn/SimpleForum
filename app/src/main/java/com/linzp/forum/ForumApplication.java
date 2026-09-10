package com.linzp.forum;

import android.app.Application;
import android.content.Context;

import com.linzp.forum.data.repository.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 全局 Application。
 * 尽早把演示账号准备好，不然用户装完打开发现登不进去。
 */
public class ForumApplication extends Application {

    private static Context appContext;

    /** 只用来跑一次性的初始化，用完就关 */
    private ExecutorService initExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        initDemoAccount();
    }

    private void initDemoAccount() {
        // 数据库操作不能放主线程，虽然是首次安装才跑一次，
        // 但个别低端机建库 + 插几十条数据也要几百毫秒，会拖慢冷启动。
        // 放到子线程去，登录页那些操作会等到真正用到数据时再访问库。
        initExecutor = Executors.newSingleThreadExecutor();
        initExecutor.execute(new Runnable() {
            @Override
            public void run() {
                UserRepository.getInstance(ForumApplication.this).ensureDemoUsers();
            }
        });
    }

    public static Context getAppContext() {
        return appContext;
    }
}
