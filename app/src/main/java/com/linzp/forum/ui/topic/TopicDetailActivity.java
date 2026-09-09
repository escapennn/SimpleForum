package com.linzp.forum.ui.topic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.ui.adapter.TopicDetailAdapter;
import com.linzp.forum.ui.profile.UserProfileActivity;
import com.linzp.forum.util.ToastUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 帖子详情页。
 */
public class TopicDetailActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_TOPIC_ID = "extra_topic_id";

    private ImageView ivBack;
    private ImageView ivMore;
    private RecyclerView rvDetail;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private EditText etReply;
    private View btnLike;
    private ImageView ivLike;
    private TextView tvLikeCount;
    private TextView btnSend;

    private TopicDetailAdapter adapter;
    private TopicRepository topicRepository;
    private UserRepository userRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private long topicId;
    private TopicEntity currentTopic;

    /** 回复某个人时记下来，为空就是直接回复楼主 */
    private String replyToName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        topicId = getIntent().getLongExtra(EXTRA_TOPIC_ID, -1L);
        if (topicId <= 0) {
            ToastUtils.show(this, "帖子不存在");
            finish();
            return;
        }

        topicRepository = TopicRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        session = UserSession.getInstance(this);

        bindViews();
        loadDetail();
    }

    private void bindViews() {
        ivBack = findViewById(R.id.iv_back);
        ivMore = findViewById(R.id.iv_more);
        rvDetail = findViewById(R.id.rv_detail);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        etReply = findViewById(R.id.et_reply);
        btnLike = findViewById(R.id.btn_like);
        ivLike = findViewById(R.id.iv_like);
        tvLikeCount = findViewById(R.id.tv_like_count);
        btnSend = findViewById(R.id.btn_send);

        ivBack.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDetail();
            }
        });

        adapter = new TopicDetailAdapter(this);
        adapter.setActionListener(new TopicDetailAdapter.OnReplyActionListener() {
            @Override
            public void onReplyClick(ReplyEntity reply, int position) {
                // 点"回复"就把光标聚焦到输入框，并记住回的是谁
                replyToName = reply.getAuthorName();
                etReply.setHint("回复 @" + replyToName);
                etReply.requestFocus();
                showSoftKeyboard(etReply);
            }

            @Override
            public void onReplyLikeClick(ReplyEntity reply, int position) {
                ToastUtils.show(TopicDetailActivity.this, "评论点赞后面再做");
            }
        });
        adapter.setOnAuthorClickListener(new TopicDetailAdapter.OnAuthorClickListener() {
            @Override
            public void onAuthorClick(long authorId) {
                // 自己点自己就不用跳了
                if (session.isCurrentUser(authorId)) {
                    ToastUtils.show(TopicDetailActivity.this, "这是你自己发的");
                    return;
                }
                Intent intent = new Intent(TopicDetailActivity.this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.EXTRA_USER_ID, authorId);
                startActivity(intent);
            }
        });

        rvDetail.setLayoutManager(new LinearLayoutManager(this));
        rvDetail.setAdapter(adapter);

        etReply.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendReply();
                    return true;
                }
                return false;
            }
        });
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final TopicEntity topic = topicRepository.getTopicDetail(topicId);
                final List<ReplyEntity> replies = topicRepository.getReplies(topicId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (topic == null) {
                            ToastUtils.show(TopicDetailActivity.this, "帖子已被删除");
                            finish();
                            return;
                        }
                        currentTopic = topic;
                        adapter.setTopic(topic);
                        adapter.setReplies(replies);
                        updateLikeBar(topic);
                    }
                });
            }
        });
    }

    private void updateLikeBar(TopicEntity topic) {
        if (topic == null) {
            return;
        }
        tvLikeCount.setText(String.valueOf(topic.getLikeCount()));
        if (topic.isLiked()) {
            ivLike.setImageResource(R.drawable.ic_like_filled);
            tvLikeCount.setTextColor(getResources().getColor(R.color.brand_accent));
        } else {
            ivLike.setImageResource(R.drawable.ic_like_outline);
            tvLikeCount.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void handleLike() {
        if (!session.isLoggedIn()) {
            ToastUtils.show(this, "请先登录");
            return;
        }
        if (currentTopic == null) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean liked = topicRepository.toggleLike(topicId);
                // 只读刷新，不要用 getTopicDetail，否则点个赞浏览量也在涨
                final TopicEntity updated = topicRepository.getTopicById(topicId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed() || updated == null) {
                            return;
                        }
                        currentTopic = updated;
                        updateLikeBar(updated);
                        // 头部的点赞数也要跟着变，单独刷第 0 项
                        adapter.notifyItemChanged(0);
                        ToastUtils.show(TopicDetailActivity.this,
                                liked ? "已点赞" : "已取消点赞");
                    }
                });
            }
        });
    }

    private void sendReply() {
        if (!session.isLoggedIn()) {
            ToastUtils.show(this, "请先登录");
            return;
        }
        final String content = etReply.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtils.show(this, R.string.tip_reply_empty);
            return;
        }
        if (currentTopic == null) {
            return;
        }

        final ReplyEntity reply = new ReplyEntity();
        reply.setContent(content);
        reply.setAuthorId(session.getUserId());
        reply.setAuthorName(session.displayName());
        reply.setAuthorAvatar(session.getAvatar());
        reply.setReplyToName(TextUtils.isEmpty(replyToName) ? null : replyToName);
        reply.setLikeCount(0);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final long id = topicRepository.addReply(topicId, reply);
                if (session.getUserId() > 0) {
                    userRepository.onReplyPublished(session.getUserId());
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (id <= 0) {
                            ToastUtils.show(TopicDetailActivity.this, "评论失败，再试一次");
                            return;
                        }
                        etReply.setText("");
                        // 清掉回复对象和提示文案
                        replyToName = null;
                        etReply.setHint(R.string.hint_reply);
                        hideSoftKeyboard(etReply);
                        loadDetail();
                    }
                });
            }
        });
    }

    private void showMoreMenu() {
        if (currentTopic == null) {
            return;
        }
        final boolean isAuthor = session.isCurrentUser(currentTopic.getAuthorId());
        final String[] items;
        if (isAuthor) {
            items = new String[]{"复制标题", "删除帖子"};
        } else {
            items = new String[]{"复制标题", "举报"};
        }

        new AlertDialog.Builder(this)
                .setTitle("操作")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        if (which == 0) {
                            copyTitle();
                        } else if (which == 1) {
                            if (isAuthor) {
                                confirmDelete();
                            } else {
                                ToastUtils.show(TopicDetailActivity.this, "已收到举报，我们会尽快处理");
                            }
                        }
                    }
                })
                .show();
    }

    private void copyTitle() {
        if (currentTopic == null) {
            return;
        }
        android.content.ClipboardManager cm = (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(android.content.ClipData.newPlainText("title", currentTopic.getTitle()));
            ToastUtils.show(this, "标题已复制");
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setMessage("确定要删除这篇帖子吗？评论也会一起删掉。")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                doDelete();
                            }
                        })
                .show();
    }

    private void doDelete() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean ok = topicRepository.deleteTopic(topicId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        ToastUtils.show(TopicDetailActivity.this,
                                ok ? "已删除" : "删除失败");
                        if (ok) {
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void showSoftKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            view.requestFocus();
            imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideSoftKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_more) {
            showMoreMenu();
        } else if (id == R.id.btn_like) {
            handleLike();
        } else if (id == R.id.btn_send) {
            sendReply();
        }
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        super.onDestroy();
    }
}
