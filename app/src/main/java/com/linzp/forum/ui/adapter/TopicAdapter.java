package com.linzp.forum.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.linzp.forum.R;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.model.TopicStats;
import com.linzp.forum.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子列表适配器。
 * 点赞按钮在列表里也支持点击，回调交给外面处理。
 */
public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private final Context context;
    private final List<TopicEntity> data = new ArrayList<>();

    private OnTopicClickListener clickListener;
    private OnLikeClickListener likeListener;

    public interface OnTopicClickListener {
        void onTopicClick(TopicEntity topic, int position);
    }

    public interface OnLikeClickListener {
        void onLikeClick(TopicEntity topic, int position);
    }

    public TopicAdapter(Context context) {
        this.context = context;
    }

    public void setOnTopicClickListener(OnTopicClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.likeListener = listener;
    }

    public void setData(List<TopicEntity> topics) {
        data.clear();
        if (topics != null) {
            data.addAll(topics);
        }
        notifyDataSetChanged();
    }

    public void addData(List<TopicEntity> topics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        int start = data.size();
        data.addAll(topics);
        notifyItemRangeInserted(start, topics.size());
    }

    /**
     * 只更新某一条，点赞用。
     * 整表 notifyDataSetChanged 会导致列表闪烁，体验不好。
     */
    public void updateItem(int position, TopicEntity topic) {
        if (position < 0 || position >= data.size()) {
            return;
        }
        data.set(position, topic);
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        if (position < 0 || position >= data.size()) {
            return;
        }
        data.remove(position);
        notifyItemRemoved(position);
        // 删除后后面的下标都变了，要通知一下
        notifyItemRangeChanged(position, data.size() - position);
    }

    public List<TopicEntity> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        TopicEntity topic = data.get(position);
        holder.bind(topic, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class TopicViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final TextView tvAuthor;
        private final TextView tvTime;
        private final TextView tvCategory;
        private final TextView tvTitle;
        private final TextView tvSummary;
        private final TextView tvViewCount;
        private final TextView tvReplyCount;
        private final TextView tvLikeCount;
        private final ImageView ivLike;
        private final TextView tvTopFlag;
        private final TextView tvEssenceFlag;

        TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvViewCount = itemView.findViewById(R.id.tv_view_count);
            tvReplyCount = itemView.findViewById(R.id.tv_reply_count);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ivLike = itemView.findViewById(R.id.iv_like);
            tvTopFlag = itemView.findViewById(R.id.tv_top_flag);
            tvEssenceFlag = itemView.findViewById(R.id.tv_essence_flag);
        }

        void bind(final TopicEntity topic, final int position) {
            tvAuthor.setText(TextUtils.isEmpty(topic.getAuthorName())
                    ? "匿名用户" : topic.getAuthorName());
            tvTime.setText(TimeUtils.formatShort(topic.getCreateTime()));
            tvCategory.setText(Category.nameOf(topic.getCategoryId()));
            tvTitle.setText(topic.getTitle());

            // 内容里的换行在列表里显示会很难看，统一替换成空格
            String content = topic.getContent() == null ? "" : topic.getContent();
            tvSummary.setText(content.replace("\n", " ").trim());

            tvViewCount.setText(TopicStats.formatCount(topic.getViewCount()));
            tvReplyCount.setText(TopicStats.formatCount(topic.getReplyCount()));
            tvLikeCount.setText(TopicStats.formatCount(topic.getLikeCount()));

            tvTopFlag.setVisibility(topic.isTopTopic() ? View.VISIBLE : View.GONE);
            tvEssenceFlag.setVisibility(topic.isEssenceTopic() ? View.VISIBLE : View.GONE);

            // 点赞状态
            if (topic.isLiked()) {
                ivLike.setImageResource(R.drawable.ic_like_filled);
                tvLikeCount.setTextColor(context.getResources().getColor(R.color.brand_accent));
            } else {
                ivLike.setImageResource(R.drawable.ic_like_outline);
                tvLikeCount.setTextColor(context.getResources().getColor(R.color.text_hint));
            }

            // 头像先留空，后面接真实图床再加
            if (!TextUtils.isEmpty(topic.getAuthorAvatar())) {
                Glide.with(context)
                        .load(topic.getAuthorAvatar())
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                // 复用的时候必须清一下，不然会显示上一条的图
                ivAvatar.setImageDrawable(null);
                ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
                ivAvatar.setImageResource(R.drawable.ic_avatar_default);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTopicClick(topic, position);
                    }
                }
            });

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (likeListener != null) {
                        likeListener.onLikeClick(topic, position);
                    }
                }
            });

            tvLikeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (likeListener != null) {
                        likeListener.onLikeClick(topic, position);
                    }
                }
            });
        }
    }
}
