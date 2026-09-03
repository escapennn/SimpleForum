package com.linzp.forum.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.data.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * 板块列表适配器。
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> data = new ArrayList<>();
    private int[] counts = new int[0];

    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(Context context) {
        this.context = context;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.clickListener = listener;
    }

    public void setData(Category[] categories, int[] topicCounts) {
        data.clear();
        if (categories != null) {
            for (Category c : categories) {
                data.add(c);
            }
        }
        this.counts = topicCounts == null ? new int[0] : topicCounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final View colorBar;
        private final TextView tvName;
        private final TextView tvDesc;
        private final TextView tvCount;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorBar = itemView.findViewById(R.id.v_color_bar);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvDesc = itemView.findViewById(R.id.tv_category_desc);
            tvCount = itemView.findViewById(R.id.tv_topic_count);
        }

        void bind(final Category category, final int position) {
            tvName.setText(category.getName());
            tvDesc.setText(TextUtils.isEmpty(category.getDescription())
                    ? "暂无简介" : category.getDescription());

            int count = (position < counts.length) ? counts[position] : 0;
            tvCount.setText(count + " 帖");

            // 左侧色条按板块颜色走，让每个板块有点辨识度
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(4f);
            bg.setColor(category.getColorRes());
            colorBar.setBackground(bg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onCategoryClick(category, position);
                    }
                }
            });
        }
    }
}
