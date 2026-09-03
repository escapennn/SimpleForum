package com.linzp.forum.ui.main.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.ui.adapter.CategoryAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 板块页。列出所有板块和每个板块的帖子数。
 */
public class CategoryFragment extends Fragment {

    private RecyclerView rvCategory;

    private CategoryAdapter adapter;
    private TopicRepository topicRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_category, container, false);
        rvCategory = root.findViewById(R.id.rv_category);
        topicRepository = TopicRepository.getInstance(requireContext());

        adapter = new CategoryAdapter(requireContext());
        adapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category, int position) {
                if (category.getId() == 0) {
                    // "全部"不单独开页，提示去首页
                    return;
                }
                ToastHelper.show(requireContext(), "「" + category.getName() + "」的帖子可以在首页筛选查看");
            }
        });
        rvCategory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategory.setAdapter(adapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCounts();
    }

    private void loadCounts() {
        if (topicRepository == null || !isAdded()) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 每个板块查一次帖子数，板块就 6 个，直接查没问题
                final int[] counts = new int[Category.ALL.length];
                for (int i = 0; i < Category.ALL.length; i++) {
                    Category c = Category.ALL[i];
                    if (c.getId() == 0) {
                        counts[i] = topicRepository.getTotalTopicCount();
                    } else {
                        counts[i] = topicRepository.loadHomeTopics(c.getId()).size();
                    }
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }
                        adapter.setData(Category.ALL, counts);
                    }
                });
            }
        });
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

    /** 小工具，避免直接依赖 Activity 的 Toast */
    private static class ToastHelper {
        static void show(android.content.Context context, String msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
