package com.linzp.forum.ui.post;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.util.CommonUtils;
import com.linzp.forum.util.ToastUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发帖页。
 */
public class PublishTopicActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private static final int CONTENT_MAX = 5000;

    private ImageView ivBack;
    private View llPickCategory;
    private TextView tvCategory;
    private EditText etTitle;
    private EditText etContent;
    private TextView tvWordCount;
    private TextView btnPublish;

    private TopicRepository topicRepository;
    private UserRepository userRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 默认发到综合讨论 */
    private int selectedCategoryId = 1;

    /** 防止连点重复发帖 */
    private boolean publishing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_topic);

        topicRepository = TopicRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        session = UserSession.getInstance(this);

        int fromIntent = getIntent().getIntExtra(EXTRA_CATEGORY_ID, 0);
        if (fromIntent > 0) {
            selectedCategoryId = fromIntent;
        }

        bindViews();
        updateCategoryText();
    }

    private void bindViews() {
        ivBack = findViewById(R.id.iv_back);
        llPickCategory = findViewById(R.id.ll_pick_category);
        tvCategory = findViewById(R.id.tv_category);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        tvWordCount = findViewById(R.id.tv_word_count);
        btnPublish = findViewById(R.id.btn_publish);

        ivBack.setOnClickListener(this);
        llPickCategory.setOnClickListener(this);
        btnPublish.setOnClickListener(this);

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvWordCount.setText(s.length() + " / " + CONTENT_MAX);
                // 快满了标个红提醒一下
                if (s.length() > CONTENT_MAX - 100) {
                    tvWordCount.setTextColor(getResources().getColor(R.color.state_error));
                } else {
                    tvWordCount.setTextColor(getResources().getColor(R.color.text_hint));
                }
            }
        });
    }

    private void updateCategoryText() {
        tvCategory.setText(Category.nameOf(selectedCategoryId));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            handleClose();
        } else if (id == R.id.ll_pick_category) {
            showCategoryPicker();
        } else if (id == R.id.btn_publish) {
            doPublish();
        }
    }

    /**
     * 有内容的时候返回要确认一下，不然辛苦打的字就没了
     */
    private void handleClose() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("内容还没发布，确定要退出吗？")
                .setNegativeButton("继续编辑", null)
                .setPositiveButton("退出", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void showCategoryPicker() {
        // 排除掉"全部"这个伪板块，只能发到具体板块
        final Category[] publishable = new Category[Category.ALL.length - 1];
        System.arraycopy(Category.ALL, 1, publishable, 0, publishable.length);

        final String[] names = new String[publishable.length];
        int checkedIndex = 0;
        for (int i = 0; i < publishable.length; i++) {
            names[i] = publishable[i].getName();
            if (publishable[i].getId() == selectedCategoryId) {
                checkedIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择板块")
                .setSingleChoiceItems(names, checkedIndex,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                selectedCategoryId = publishable[which].getId();
                                updateCategoryText();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    private void doPublish() {
        if (publishing) {
            return;
        }
        if (!session.isLoggedIn()) {
            ToastUtils.show(this, "登录后才能发帖");
            return;
        }

        final String title = etTitle.getText().toString().trim();
        final String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            ToastUtils.show(this, R.string.tip_title_empty);
            return;
        }
        if (title.length() < 5) {
            ToastUtils.show(this, R.string.tip_title_too_short);
            return;
        }
        if (TextUtils.isEmpty(content)) {
            ToastUtils.show(this, R.string.tip_content_empty);
            return;
        }

        publishing = true;
        btnPublish.setEnabled(false);
        btnPublish.setAlpha(0.6f);

        final TopicEntity topic = new TopicEntity();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setCategoryId(selectedCategoryId);
        topic.setAuthorId(session.getUserId());
        topic.setAuthorName(session.displayName());
        topic.setAuthorAvatar(session.getAvatar());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final long newId = topicRepository.publishTopic(topic);
                if (newId > 0) {
                    userRepository.onTopicPublished(session.getUserId());
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        publishing = false;
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        btnPublish.setEnabled(true);
                        btnPublish.setAlpha(1.0f);

                        if (newId > 0) {
                            ToastUtils.show(PublishTopicActivity.this, R.string.tip_publish_success);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            ToastUtils.show(PublishTopicActivity.this, "发布失败，请重试");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        handleClose();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        super.onDestroy();
    }
}
