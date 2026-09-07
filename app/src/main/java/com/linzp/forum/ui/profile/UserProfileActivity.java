package com.linzp.forum.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.ui.adapter.TopicAdapter;
import com.linzp.forum.ui.topic.TopicDetailActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 别人的个人主页，从帖子详情点作者头像进来。
 */
public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_USER_ID = "extra_user_id";

    private ImageView ivBack;
    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvSignature;
    private TextView tvTopicCount;
    private TextView tvReplyCount;
    private TextView tvLevel;
    private RecyclerView rvTopics;

    private TopicAdapter adapter;
    private UserRepository userRepository;
    private TopicRepository topicRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private long userId = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1L);
        if (userId <= 0) {
            finish();
            return;
        }

        userRepository = UserRepository.getInstance(this);
        topicRepository = TopicRepository.getInstance(this);

        ivBack = findViewById(R.id.iv_back);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        tvSignature = findViewById(R.id.tv_signature);
        tvTopicCount = findViewById(R.id.tv_topic_count);
        tvReplyCount = findViewById(R.id.tv_reply_count);
        tvLevel = findViewById(R.id.tv_level);
        rvTopics = findViewById(R.id.rv_topics);

        ivBack.setOnClickListener(this);

        adapter = new TopicAdapter(this);
        adapter.setOnTopicClickListener(new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(TopicEntity topic, int position) {
                Intent intent = new Intent(UserProfileActivity.this, TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        rvTopics.setAdapter(adapter);

        loadProfile();
    }

    private void loadProfile() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final UserEntity user = userRepository.getUserById(userId);
                final List<TopicEntity> topics = topicRepository.getTopicsByAuthor(userId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        renderUser(user);
                        adapter.setData(topics);
                    }
                });
            }
        });
    }

    private void renderUser(UserEntity user) {
        if (user == null) {
            tvNickname.setText("未知用户");
            return;
        }
        tvNickname.setText(user.getNickname());
        tvSignature.setText(TextUtils.isEmpty(user.getSignature())
                ? "这个人很懒，什么都没写" : user.getSignature());
        tvTopicCount.setText(String.valueOf(user.getTopicCount()));
        tvReplyCount.setText(String.valueOf(user.getReplyCount()));
        tvLevel.setText("Lv." + user.getLevel());

        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        }
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
