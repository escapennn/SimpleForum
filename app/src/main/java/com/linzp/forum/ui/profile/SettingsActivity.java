package com.linzp.forum.ui.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.db.ForumDatabase;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.util.ToastUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 设置页。
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivBack;
    private LinearLayout llNickname;
    private LinearLayout llSignature;
    private LinearLayout llClearCache;
    private LinearLayout llVersion;
    private TextView tvNicknameValue;
    private TextView tvSignatureValue;
    private TextView tvVersion;
    private TextView tvCacheSize;

    private UserRepository userRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userRepository = UserRepository.getInstance(this);
        session = UserSession.getInstance(this);

        ivBack = findViewById(R.id.iv_back);
        llNickname = findViewById(R.id.ll_nickname);
        llSignature = findViewById(R.id.ll_signature);
        llClearCache = findViewById(R.id.ll_clear_cache);
        llVersion = findViewById(R.id.ll_version);
        tvNicknameValue = findViewById(R.id.tv_nickname_value);
        tvSignatureValue = findViewById(R.id.tv_signature_value);
        tvVersion = findViewById(R.id.tv_version);
        tvCacheSize = findViewById(R.id.tv_cache_size);

        ivBack.setOnClickListener(this);
        llNickname.setOnClickListener(this);
        llSignature.setOnClickListener(this);
        llClearCache.setOnClickListener(this);
        llVersion.setOnClickListener(this);

        tvVersion.setText(getAppVersion());
        loadUserInfo();
        calcCacheSize();
    }

    private String getAppVersion() {
        try {
            android.content.pm.PackageInfo info =
                    getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }

    private void loadUserInfo() {
        if (!session.isLoggedIn()) {
            tvNicknameValue.setText("未登录");
            tvSignatureValue.setText("未登录");
            return;
        }
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final UserEntity user = userRepository.getUserById(userId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed() || user == null) {
                            return;
                        }
                        tvNicknameValue.setText(TextUtils.isEmpty(user.getNickname())
                                ? "未设置" : user.getNickname());
                        tvSignatureValue.setText(TextUtils.isEmpty(user.getSignature())
                                ? "未设置" : user.getSignature());
                    }
                });
            }
        });
    }

    /**
     * 缓存大小就是数据库文件的大小，先这么算着
     */
    private void calcCacheSize() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File dbFile = getDatabasePath("simple_forum.db");
                final long size = (dbFile != null && dbFile.exists()) ? dbFile.length() : 0;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvCacheSize.setText(formatSize(size));
                    }
                });
            }
        });
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024f);
        }
        return String.format("%.1f MB", bytes / 1024f / 1024f);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.ll_nickname) {
            showEditDialog(true);
        } else if (id == R.id.ll_signature) {
            showEditDialog(false);
        } else if (id == R.id.ll_clear_cache) {
            confirmClearData();
        } else if (id == R.id.ll_version) {
            ToastUtils.show(this, "已经是最新版本了");
        }
    }

    private void showEditDialog(final boolean isNickname) {
        if (!session.isLoggedIn()) {
            ToastUtils.show(this, "请先登录");
            return;
        }
        final EditText input = new EditText(this);
        input.setBackgroundResource(R.drawable.bg_input);
        input.setPadding(30, 30, 30, 30);
        input.setTextSize(15);
        if (isNickname) {
            input.setHint("输入新昵称（最多 12 字）");
            input.setText(session.getNickname());
        } else {
            input.setHint("写点什么吧（最多 30 字）");
            input.setText(tvSignatureValue.getText().toString().equals("未设置")
                    ? "" : tvSignatureValue.getText().toString());
        }

        new AlertDialog.Builder(this)
                .setTitle(isNickname ? "修改昵称" : "个性签名")
                .setView(input)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                String value = input.getText().toString().trim();
                                if (isNickname) {
                                    saveNickname(value);
                                } else {
                                    saveSignature(value);
                                }
                            }
                        })
                .show();
    }

    private void saveNickname(final String nickname) {
        if (TextUtils.isEmpty(nickname)) {
            ToastUtils.show(this, "昵称不能为空");
            return;
        }
        if (nickname.length() > 12) {
            ToastUtils.show(this, "昵称最多 12 个字");
            return;
        }
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userRepository.updateNickname(userId, nickname);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 内存里的登录态也要同步，不然首页发帖还是旧昵称
                        session.updateNickname(nickname);
                        tvNicknameValue.setText(nickname);
                        ToastUtils.show(SettingsActivity.this, "昵称已更新");
                    }
                });
            }
        });
    }

    private void saveSignature(final String signature) {
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userRepository.updateSignature(userId, signature);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvSignatureValue.setText(TextUtils.isEmpty(signature) ? "未设置" : signature);
                        ToastUtils.show(SettingsActivity.this, "签名已更新");
                    }
                });
            }
        });
    }

    private void confirmClearData() {
        new AlertDialog.Builder(this)
                .setTitle("清空本地数据")
                .setMessage("会删掉本地缓存的帖子和评论，\n下次进入会重新加载演示数据。\n确定要继续吗？")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                clearLocalData();
                            }
                        })
                .show();
    }

    private void clearLocalData() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 先把 Room 关掉、缓存清掉，不然文件被占着删不干净
                ForumDatabase.release();
                TopicRepository.releaseInstance();
                UserRepository.releaseInstance();
                boolean deleted = deleteDatabase("simple_forum.db");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvCacheSize.setText("0 B");
                        ToastUtils.show(SettingsActivity.this,
                                deleted ? "已清空，重启 App 生效" : "清空失败，稍后再试");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        super.onDestroy();
    }
}
