package com.linzp.forum.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * 统一的轻提示。
 * 有些页面用 Toast 有些用 Snackbar 太乱了，收敛到这里。
 */
public final class ToastUtils {

    private static Toast currentToast;

    private ToastUtils() {
    }

    public static void show(Context context, String message) {
        if (context == null || message == null) {
            return;
        }
        // 连续点按钮时复用同一个 Toast，避免排队弹出的难看效果
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    public static void show(Context context, int stringResId) {
        if (context == null) {
            return;
        }
        show(context, context.getString(stringResId));
    }

    public static void showOnView(View anchor, String message) {
        if (anchor == null || message == null) {
            return;
        }
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }
}
