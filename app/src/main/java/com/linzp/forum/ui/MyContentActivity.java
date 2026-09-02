package com.linzp.forum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.ui.adapter.MyContentAdapter;
import com.linzp.forum.ui.adapter.TopicAdapter;
import com.linzp.forum.ui.topic.TopicDetailActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 我的帖子 / 我的回复。
 * 两个列表结构差太多，用两个 adapter 分开处理比硬塞一个简单。
 */
public class MyContentActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_TYPE = "extra_type";
    public static final int TYPE_TOPIC = 1;
    public static final int TYPE_REPLY = 2;

    private ImageView ivBack;
    private TextView tvTitle;
    private RecyclerView rvContent;
    private LinearLayout llEmpty;
    private TextView tvEmpty;

    private TopicAdapter topicAdapter;
    private MyContentAdapter replyAdapter;

    private TopicRepository topicRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int contentType = TYPE_TOPIC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_content);

        contentType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_TOPIC);
        topicRepository = TopicRepository.getInstance(this);
        session = UserSession.getInstance(this);

        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        rvContent = findViewById(R.id.rv_content);
        llEmpty = findViewById(R.id.ll_empty);
        tvEmpty = findViewById(R.id.tv_empty);

        ivBack.setOnClickListener(this);
        rvContent.setLayoutManager(new LinearLayoutManager(this));

        if (contentType == TYPE_TOPIC) {
            tvTitle.setText(R.string.my_topics);
            tvEmpty.setText("你还没有发过帖子");
            setupTopicList();
        } else {
            tvTitle.setText(R.string.my_replies);
            tvEmpty.setText("你还没有回复过别人");
            setupReplyList();
        }

        loadData();
    }

    private void setupTopicList() {
        topicAdapter = new TopicAdapter(this);
        topicAdapter.setOnTopicClickListener(new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(TopicEntity topic, int position) {
                Intent intent = new Intent(MyContentActivity.this, TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });
        // 这里不允许点赞，作者看自己的帖子点赞没意义
        topicAdapter.setOnLikeClickListener(null);
        rvContent.setAdapter(topicAdapter);
    }

    private void setupReplyList() {
        replyAdapter = new MyContentAdapter(this);
        replyAdapter.setOnItemClickListener(new MyContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ReplyEntity reply, int position) {
                Intent intent = new Intent(MyContentActivity.this, TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, reply.getTopicId());
                startActivity(intent);
            }
        });
        rvContent.setAdapter(replyAdapter);
    }

    private void loadData() {
        if (!session.isLoggedIn()) {
            showEmpty();
            return;
        }
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (contentType == TYPE_TOPIC) {
                    final List<TopicEntity> topics = topicRepository.getTopicsByAuthor(userId);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            topicAdapter.setData(topics);
                            toggleEmpty(topics.isEmpty());
                        }
                    });
                } else {
                    final List<ReplyEntity> replies = topicRepository.getRepliesByAuthor(userId);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            replyAdapter.setData(replies);
                            toggleEmpty(replies.isEmpty());
                        }
                    });
                }
            }
        });
    }

    private void toggleEmpty(boolean empty) {
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvContent.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showEmpty() {
        llEmpty.setVisibility(View.VISIBLE);
        rvContent.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 详情页可能删了帖子，回来要刷新
        loadData();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        super.onDestroy();
    }
}
