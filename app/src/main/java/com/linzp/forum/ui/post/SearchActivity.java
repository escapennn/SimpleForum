package com.linzp.forum.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.ui.adapter.TopicAdapter;
import com.linzp.forum.ui.topic.TopicDetailActivity;
import com.linzp.forum.util.ToastUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 搜索页。
 * 搜索历史存在内存里，杀进程就没了，后面换成数据库或者 SP。
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private static final int MAX_HISTORY = 8;

    private ImageView ivBack;
    private EditText etSearch;
    private TextView tvCancel;
    private LinearLayout llHistory;
    private TextView tvClearHistory;
    private ChipGroup chipGroup;
    private RecyclerView rvResult;
    private LinearLayout llEmpty;
    private TextView tvEmptyTip;

    private TopicAdapter adapter;
    private TopicRepository topicRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 搜索历史，最多存 8 条 */
    private final java.util.List<String> historyList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        topicRepository = TopicRepository.getInstance(this);

        ivBack = findViewById(R.id.iv_back);
        etSearch = findViewById(R.id.et_search);
        tvCancel = findViewById(R.id.tv_cancel);
        llHistory = findViewById(R.id.ll_history);
        tvClearHistory = findViewById(R.id.tv_clear_history);
        chipGroup = findViewById(R.id.chip_group);
        rvResult = findViewById(R.id.rv_result);
        llEmpty = findViewById(R.id.ll_empty);
        tvEmptyTip = findViewById(R.id.tv_empty_tip);

        ivBack.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvClearHistory.setOnClickListener(this);

        adapter = new TopicAdapter(this);
        adapter.setOnTopicClickListener(new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onTopicClick(TopicEntity topic, int position) {
                Intent intent = new Intent(SearchActivity.this, TopicDetailActivity.class);
                intent.putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        rvResult.setAdapter(adapter);

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = etSearch.getText().toString().trim();
                    if (TextUtils.isEmpty(keyword)) {
                        ToastUtils.show(SearchActivity.this, "请输入关键词");
                    } else {
                        doSearch(keyword);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void doSearch(final String keyword) {
        addToHistory(keyword);
        hideSoftKeyboard();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<TopicEntity> result = topicRepository.searchTopics(keyword);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        llHistory.setVisibility(View.GONE);
                        if (result.isEmpty()) {
                            rvResult.setVisibility(View.GONE);
                            llEmpty.setVisibility(View.VISIBLE);
                            tvEmptyTip.setText("没有找到和「" + keyword + "」相关的帖子");
                        } else {
                            llEmpty.setVisibility(View.GONE);
                            rvResult.setVisibility(View.VISIBLE);
                            adapter.setData(result);
                        }
                    }
                });
            }
        });
    }

    private void addToHistory(String keyword) {
        if (historyList.contains(keyword)) {
            historyList.remove(keyword);
        }
        historyList.add(0, keyword);
        // 超过上限就把最老的去掉
        while (historyList.size() > MAX_HISTORY) {
            historyList.remove(historyList.size() - 1);
        }
        refreshHistoryChips();
    }

    private void refreshHistoryChips() {
        chipGroup.removeAllViews();
        if (historyList.isEmpty()) {
            llHistory.setVisibility(View.GONE);
            return;
        }
        llHistory.setVisibility(View.VISIBLE);
        for (final String keyword : historyList) {
            Chip chip = new Chip(this);
            chip.setText(keyword);
            chip.setClickable(true);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etSearch.setText(keyword);
                    etSearch.setSelection(keyword.length());
                    doSearch(keyword);
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void hideSoftKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && etSearch != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back || id == R.id.tv_cancel) {
            finish();
        } else if (id == R.id.tv_clear_history) {
            historyList.clear();
            refreshHistoryChips();
            rvResult.setVisibility(View.GONE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdown();
        super.onDestroy();
    }
}
