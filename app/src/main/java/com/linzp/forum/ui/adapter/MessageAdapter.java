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

import com.linzp.forum.R;
import com.linzp.forum.data.model.NoticeItem;
import com.linzp.forum.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息列表。
 * 现在只有回复通知一种，样式固定，不需要多 viewType。
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.NoticeViewHolder> {

    private final Context context;
    private final List<NoticeItem> items = new ArrayList<>();

    private OnNoticeClickListener clickListener;

    public interface OnNoticeClickListener {
        void onNoticeClick(NoticeItem item, int position);
    }

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void setOnNoticeClickListener(OnNoticeClickListener listener) {
        this.clickListener = listener;
    }

    public void setData(List<NoticeItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NoticeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvTime;
        private final TextView tvTopic;
        private final TextView tvContent;

        NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notice_title);
            tvTime = itemView.findViewById(R.id.tv_notice_time);
            tvTopic = itemView.findViewById(R.id.tv_notice_topic);
            tvContent = itemView.findViewById(R.id.tv_notice_content);
        }

        void bind(final NoticeItem item, final int position) {
            tvTitle.setText(TextUtils.isEmpty(item.getFromName())
                    ? "有人回复了你的帖子" : item.getFromName() + " 回复了你的帖子");
            tvTime.setText(TimeUtils.formatRelative(item.getCreateTime()));
            tvTopic.setText(TextUtils.isEmpty(item.getTopicTitle())
                    ? "" : "《" + item.getTopicTitle() + "》");
            tvContent.setText(item.getContent());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onNoticeClick(item, position);
                    }
                }
            });
        }
    }
}
