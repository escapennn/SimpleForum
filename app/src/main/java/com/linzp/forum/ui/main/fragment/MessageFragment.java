package com.linzp.forum.ui.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.data.model.NoticeItem;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.ui.adapter.MessageAdapter;
import com.linzp.forum.ui.topic.TopicDetailActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 消息页。
 * 没做长连接推送，就是每次进来查一遍：谁回复了我的帖子、收到多少赞。
 */
public class MessageFragment extends Fragment {

    private TextView tvStatReply;
    private TextView tvStatLike;
    private TextView tvStatSystem;
    private RecyclerView rvNotice;
    private LinearLayout llEmpty;
    private TextView tvEmptyHint;

    private MessageAdapter adapter;
    private TopicRepository topicRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topicRepository = TopicRepository.getInstance(requireContext());
        session = UserSession.getInstance(requireContext());

        tvStatReply = view.findViewById(R.id.tv_stat_reply);
        tvStatLike = view.findViewById(R.id.tv_stat_like);
        tvStatSystem = view.findViewById(R.id.tv_stat_system);
        rvNotice = view.findViewById(R.id.rv_notice);
        llEmpty = view.findViewById(R.id.ll_empty);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);

        adapter = new MessageAdapter(requireContext());
        adapter.setOnNoticeClickListener(new MessageAdapter.OnNoticeClickListener() {
            @Override
            public void onNoticeClick(NoticeItem item, int position) {
                if (item.getTopicId() <= 0) {
                    return;
                }
                Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, item.getTopicId());
                startActivity(intent);
            }
        });
        rvNotice.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotice.setAdapter(adapter);
    }

    private void loadNotices() {
        if (!session.isLoggedIn()) {
            tvStatReply.setText("0");
            tvStatLike.setText("0");
            tvStatSystem.setText("0");
            adapter.setData(null);
            toggleEmpty(true);
            tvEmptyHint.setText("登录后可以查看别人给你回复的内容");
            return;
        }
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<NoticeItem> notices = topicRepository.getNotices(userId);
                final int replyCount = topicRepository.countRepliesToMe(userId);
                final int likeCount = topicRepository.countLikesReceived(userId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }
                        tvStatReply.setText(String.valueOf(replyCount));
                        tvStatLike.setText(String.valueOf(likeCount));
                        // 系统通知先固定 1，只有个欢迎语
                        tvStatSystem.setText("1");
                        adapter.setData(notices);
                        toggleEmpty(notices.isEmpty());
                        tvEmptyHint.setText("还没有人回复你的帖子");
                    }
                });
            }
        });
    }

    private void toggleEmpty(boolean empty) {
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvNotice.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 别的地方可能发了帖子或者收到新回复，每次回来都重新查
        if (topicRepository != null) {
            loadNotices();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
