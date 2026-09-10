package com.linzp.forum.ui.main.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.ui.adapter.CategoryAdapter;
import com.linzp.forum.ui.main.MainActivity;

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
                    // "全部"没有单独的帖子页，直接切到首页看全部就行
                    notifySwitchCategory(0);
                    return;
                }
                // 板块页自己不展示帖子，切回首页并筛这个板块，
                // 比在板块页再写一套列表逻辑省事
                notifySwitchCategory(category.getId());
            }
        });
        rvCategory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategory.setAdapter(adapter);
        return root;
    }

    /**
     * 交给宿主 Activity 去切 tab 和筛选，Fragment 之间不直接引用。
     */
    private void notifySwitchCategory(int categoryId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openCategoryOnHome(categoryId);
        }
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
}
