package com.linzp.forum.ui.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.linzp.forum.R;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.ui.adapter.TopicAdapter;
import com.linzp.forum.ui.post.PublishTopicActivity;
import com.linzp.forum.ui.post.SearchActivity;
import com.linzp.forum.ui.topic.TopicDetailActivity;
import com.linzp.forum.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 首页：帖子列表。
 * 数据库操作都放到子线程，主线程只负责刷 UI。
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTopics;
    private LinearLayout llEmpty;
    private ImageView ivSearch;
    private ImageView ivPublish;
    private LinearLayout llCategoryTabs;

    private TopicAdapter adapter;
    private TopicRepository topicRepository;
    private UserRepository userRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 当前选中的板块，0 表示全部 */
    private int currentCategoryId = 0;

    private final List<TopicEntity> topicList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topicRepository = TopicRepository.getInstance(requireContext());
        userRepository = UserRepository.getInstance(requireContext());
        session = UserSession.getInstance(requireContext());

        buildCategoryTabs();
        reloadTopics();
    }

    private void initViews(View root) {
        swipeRefresh = root.findViewById(R.id.swipe_refresh);
        rvTopics = root.findViewById(R.id.rv_topics);
        llEmpty = root.findViewById(R.id.ll_empty);
        ivSearch = root.findViewById(R.id.iv_search);
        ivPublish = root.findViewById(R.id.iv_publish);
        llCategoryTabs = root.findViewById(R.id.ll_category_tabs);

        ivSearch.setOnClickListener(this);
        ivPublish.setOnClickListener(this);

        swipeRefresh.setColorSchemeResources(R.color.brand_primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTopics();
            }
        });

        adapter = new TopicAdapter(requireContext());
        adapter.setOnTopicClickListener(new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(TopicEntity topic, int position) {
                Intent intent = new Intent(requireContext(), TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });
        adapter.setOnLikeClickListener(new TopicAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(TopicEntity topic, int position) {
                handleLike(topic, position);
            }
        });

        rvTopics.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTopics.setAdapter(adapter);
        rvTopics.setHasFixedSize(false);
    }

    /**
     * 板块 tab 是动态加的，以后加板块不用改布局
     */
    private void buildCategoryTabs() {
        llCategoryTabs.removeAllViews();
        for (int i = 0; i < Category.ALL.length; i++) {
            final Category category = Category.ALL[i];
            TextView tab = (TextView) LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_tab, llCategoryTabs, false);
            tab.setText(category.getName());
            tab.setTag(category.getId());
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCategorySelected(category.getId());
                }
            });
            llCategoryTabs.addView(tab);
        }
        highlightCategoryTab(currentCategoryId);
    }

    private void onCategorySelected(int categoryId) {
        if (categoryId == currentCategoryId) {
            return;
        }
        currentCategoryId = categoryId;
        highlightCategoryTab(categoryId);
        reloadTopics();
    }

    private void highlightCategoryTab(int categoryId) {
        for (int i = 0; i < llCategoryTabs.getChildCount(); i++) {
            View child = llCategoryTabs.getChildAt(i);
            if (!(child instanceof TextView)) {
                continue;
            }
            TextView tab = (TextView) child;
            Object tag = tab.getTag();
            boolean selected = tag instanceof Integer && ((Integer) tag) == categoryId;
            tab.setSelected(selected);
            tab.setTextColor(getResources().getColor(
                    selected ? R.color.brand_primary : R.color.text_secondary));
            tab.setTypeface(null, selected
                    ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    /**
     * 重新加载列表。供外部调用（发帖成功后刷新）。
     */
    public void reloadTopics() {
        if (topicRepository == null) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<TopicEntity> result =
                        topicRepository.loadHomeTopics(currentCategoryId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            // Fragment 已经 detach 了，不要再碰 View
                            return;
                        }
                        topicList.clear();
                        topicList.addAll(result);
                        adapter.setData(topicList);
                        updateEmptyState();
                    }
                });
            }
        });
    }

    private void refreshTopics() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<TopicEntity> result =
                        topicRepository.refreshHomeTopics(currentCategoryId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }
                        topicList.clear();
                        topicList.addAll(result);
                        adapter.setData(topicList);
                        updateEmptyState();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = topicList.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTopics.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void handleLike(final TopicEntity topic, final int position) {
        if (!session.isLoggedIn()) {
            ToastUtils.show(requireContext(), "请先登录");
            return;
        }
        if (topic == null) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final boolean nowLiked = topicRepository.toggleLike(topic.getId());
                final TopicEntity updated = topicRepository.getTopicDetail(topic.getId());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded() || updated == null) {
                            return;
                        }
                        // 详情那里浏览数会 +1，列表里的数字要还原回去，不然每次点赞都涨
                        updated.setViewCount(topic.getViewCount());
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_search) {
            startActivity(new Intent(requireContext(), SearchActivity.class));
        } else if (id == R.id.iv_publish) {
            checkLoginAndPublish();
        }
    }

    private void checkLoginAndPublish() {
        if (!session.isLoggedIn()) {
            ToastUtils.show(requireContext(), "登录后才能发帖");
            return;
        }
        Intent intent = new Intent(requireContext(), PublishTopicActivity.class);
        if (currentCategoryId > 0) {
            intent.putExtra(PublishTopicActivity.EXTRA_CATEGORY_ID, currentCategoryId);
        }
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从详情页回来，点赞状态可能变了，重新拉一遍
        if (topicRepository != null) {
            reloadTopics();
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
