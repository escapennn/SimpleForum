package com.linzp.forum.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.linzp.forum.R;
import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的回复列表适配器。
 */
public class MyContentAdapter extends RecyclerView.Adapter<MyContentAdapter.ReplyViewHolder> {

    private final Context context;
    private final List<ReplyEntity> data = new ArrayList<>();

    /** 缓存一下帖子标题，避免每条都去查库 */
    private final Map<Long, String> topicTitleCache = new HashMap<>();

    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(ReplyEntity reply, int position);
    }

    public MyContentAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setData(List<ReplyEntity> replies) {
        data.clear();
        topicTitleCache.clear();
        if (replies != null) {
            data.addAll(replies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        holder.bind(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * 拿帖子标题。列表里只存了 topicId，要反查一下标题，
     * 查过的缓存起来，同一帖子多条回复就只查一次。
     */
    private String getTopicTitle(long topicId) {
        if (topicTitleCache.containsKey(topicId)) {
            return topicTitleCache.get(topicId);
        }
        TopicEntity topic = TopicRepository.getInstance(context).getTopicDetail(topicId);
        String title = (topic == null) ? "帖子已删除" : topic.getTitle();
        topicTitleCache.put(topicId, title);
        return title;
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvContent;
        private final TextView tvTopicTitle;
        private final TextView tvTime;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_reply_content);
            tvTopicTitle = itemView.findViewById(R.id.tv_topic_title);
            tvTime = itemView.findViewById(R.id.tv_reply_time);
        }

        void bind(final ReplyEntity reply, final int position) {
            if (reply == null) {
                return;
            }
            tvContent.setText(reply.getContent());
            tvTime.setText(TimeUtils.formatRelative(reply.getCreateTime()));

            // 标题查一下，注意 getTopicDetail 会给浏览量 +1，
            // 这里只是展示用，不用管那个副作用（后面单独抽个只读方法）
            String title = getTopicTitle(reply.getTopicId());
            tvTopicTitle.setText(TextUtils.isEmpty(title) ? "无标题" : title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClick(reply, position);
                    }
                }
            });
        }
    }
}
