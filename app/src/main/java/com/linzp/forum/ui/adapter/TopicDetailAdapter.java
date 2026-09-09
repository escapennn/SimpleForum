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
import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.model.Category;
import com.linzp.forum.data.model.TopicStats;
import com.linzp.forum.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 详情页的适配器。
 * 第一个 item 是帖子本身，后面是评论，用两个 viewType 区分。
 */
public class TopicDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_REPLY = 1;

    private final Context context;
    private final List<ReplyEntity> replies = new ArrayList<>();
    private TopicEntity topic;

    private OnReplyActionListener actionListener;
    private OnAuthorClickListener authorClickListener;

    public interface OnReplyActionListener {
        void onReplyClick(ReplyEntity reply, int position);

        void onReplyLikeClick(ReplyEntity reply, int position);
    }

    /** 点头像/昵称进个人主页 */
    public interface OnAuthorClickListener {
        void onAuthorClick(long authorId);
    }

    public void setOnAuthorClickListener(OnAuthorClickListener listener) {
        this.authorClickListener = listener;
    }

    public TopicDetailAdapter(Context context) {
        this.context = context;
    }

    public void setActionListener(OnReplyActionListener listener) {
        this.actionListener = listener;
    }

    public void setTopic(TopicEntity topic) {
        this.topic = topic;
        notifyItemChanged(0);
    }

    public void setReplies(List<ReplyEntity> list) {
        replies.clear();
        if (list != null) {
            replies.addAll(list);
        }
        notifyDataSetChanged();
    }

    public TopicEntity getTopic() {
        return topic;
    }

    public List<ReplyEntity> getReplies() {
        return replies;
    }

    public int getReplyCount() {
        return replies.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_REPLY;
    }

    @Override
    public int getItemCount() {
        // 帖子本身占一位，评论从 1 开始
        if (topic == null) {
            return 0;
        }
        return replies.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_topic_header, parent, false));
        }
        return new ReplyViewHolder(inflater.inflate(R.layout.item_reply, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(topic);
        } else if (holder instanceof ReplyViewHolder) {
            // 位置要减 1，扣掉头部那一格
            final int replyIndex = position - 1;
            if (replyIndex < 0 || replyIndex >= replies.size()) {
                return;
            }
            ((ReplyViewHolder) holder).bind(replies.get(replyIndex), replyIndex);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final TextView tvAuthorName;
        private final TextView tvPublishTime;
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvCategory;
        private final TextView tvView;
        private final TextView tvCommentHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_author_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvPublishTime = itemView.findViewById(R.id.tv_publish_time);
            tvTitle = itemView.findViewById(R.id.tv_detail_title);
            tvContent = itemView.findViewById(R.id.tv_detail_content);
            tvCategory = itemView.findViewById(R.id.tv_detail_category);
            tvView = itemView.findViewById(R.id.tv_detail_view);
            tvCommentHeader = itemView.findViewById(R.id.tv_comment_header);
        }

        void bind(TopicEntity t) {
            if (t == null) {
                return;
            }
            tvAuthorName.setText(TextUtils.isEmpty(t.getAuthorName()) ? "匿名用户" : t.getAuthorName());
            tvPublishTime.setText(TimeUtils.formatFull(t.getCreateTime()));
            tvTitle.setText(t.getTitle());
            tvContent.setText(t.getContent());
            tvCategory.setText(Category.nameOf(t.getCategoryId()));
            tvView.setText(TopicStats.formatCount(t.getViewCount()));
            tvCommentHeader.setText("全部评论 " + replies.size());

            if (!TextUtils.isEmpty(t.getAuthorAvatar())) {
                Glide.with(context)
                        .load(t.getAuthorAvatar())
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageDrawable(null);
                ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
                ivAvatar.setImageResource(R.drawable.ic_avatar_default);
            }

            // 点头像或昵称都能进 TA 的主页
            View.OnClickListener authorClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (authorClickListener != null && t.getAuthorId() > 0) {
                        authorClickListener.onAuthorClick(t.getAuthorId());
                    }
                }
            };
            ivAvatar.setOnClickListener(authorClick);
            tvAuthorName.setOnClickListener(authorClick);
        }
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final TextView tvAuthor;
        private final TextView tvFloor;
        private final TextView tvTime;
        private final TextView tvContent;
        private final TextView tvReplyTo;
        private final TextView tvAction;
        private final ImageView ivLike;
        private final TextView tvLikeCount;
        private final View divider;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_reply_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_reply_author);
            tvFloor = itemView.findViewById(R.id.tv_reply_floor);
            tvTime = itemView.findViewById(R.id.tv_reply_time);
            tvContent = itemView.findViewById(R.id.tv_reply_content);
            tvReplyTo = itemView.findViewById(R.id.tv_reply_to);
            tvAction = itemView.findViewById(R.id.tv_reply_action);
            ivLike = itemView.findViewById(R.id.iv_reply_like);
            tvLikeCount = itemView.findViewById(R.id.tv_reply_like_count);
            divider = itemView.findViewById(R.id.v_reply_divider);
        }

        void bind(final ReplyEntity reply, final int index) {
            tvAuthor.setText(TextUtils.isEmpty(reply.getAuthorName())
                    ? "匿名用户" : reply.getAuthorName());
            tvFloor.setText(reply.getFloorNo() + "楼");
            tvTime.setText(TimeUtils.formatRelative(reply.getCreateTime()));
            tvContent.setText(reply.getContent());
            tvLikeCount.setText(String.valueOf(reply.getLikeCount()));

            if (!TextUtils.isEmpty(reply.getReplyToName())) {
                tvReplyTo.setVisibility(View.VISIBLE);
                tvReplyTo.setText("回复 @" + reply.getReplyToName());
            } else {
                tvReplyTo.setVisibility(View.GONE);
            }

            // 最后一条不画分割线，不然底部会多一条
            divider.setVisibility(index == replies.size() - 1 ? View.GONE : View.VISIBLE);

            if (!TextUtils.isEmpty(reply.getAuthorAvatar())) {
                Glide.with(context)
                        .load(reply.getAuthorAvatar())
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageDrawable(null);
                ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
                ivAvatar.setImageResource(R.drawable.ic_avatar_default);
            }

            tvAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onReplyClick(reply, index);
                    }
                }
            });

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onReplyLikeClick(reply, index);
                    }
                }
            });

            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (authorClickListener != null && reply.getAuthorId() > 0) {
                        authorClickListener.onAuthorClick(reply.getAuthorId());
                    }
                }
            });
        }
    }
}
