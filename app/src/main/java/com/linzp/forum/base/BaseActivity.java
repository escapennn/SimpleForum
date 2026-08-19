package com.linzp.forum.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity 基类。
 * 目前只做了统一的页面跳转和返回，后面如果要加埋点、权限申请放这里。
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 页面跳转，集中管理方便以后加转场动画
     */
    protected void navigateTo(Class<?> target) {
        startActivity(new android.content.Intent(this, target));
    }

    protected void navigateTo(Class<?> target, Bundle extras) {
        android.content.Intent intent = new android.content.Intent(this, target);
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
    }

    /**
     * 子类如果不想让返回键直接退出，可以覆盖这个方法
     */
    protected boolean handleBackPressed() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
