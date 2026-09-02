package com.linzp.forum.ui.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.linzp.forum.R;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.TopicRepository;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.ui.MyContentActivity;
import com.linzp.forum.ui.login.LoginActivity;
import com.linzp.forum.ui.profile.SettingsActivity;
import com.linzp.forum.util.ToastUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 我的（个人中心）。
 */
public class MineFragment extends Fragment implements View.OnClickListener {

    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvSignature;
    private TextView tvLevel;
    private TextView tvTopicCount;
    private TextView tvReplyCount;
    private TextView tvExp;

    private LinearLayout llStatTopic;
    private LinearLayout llStatReply;
    private LinearLayout llStatExp;
    private LinearLayout llMyTopics;
    private LinearLayout llMyReplies;
    private LinearLayout llMyCollect;
    private LinearLayout llSettings;
    private LinearLayout llAbout;
    private TextView btnLogout;

    private UserRepository userRepository;
    private TopicRepository topicRepository;
    private UserSession session;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mine, container, false);
        initViews(root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = UserRepository.getInstance(requireContext());
        topicRepository = TopicRepository.getInstance(requireContext());
        session = UserSession.getInstance(requireContext());
    }

    private void initViews(View root) {
        ivAvatar = root.findViewById(R.id.iv_avatar);
        tvNickname = root.findViewById(R.id.tv_nickname);
        tvSignature = root.findViewById(R.id.tv_signature);
        tvLevel = root.findViewById(R.id.tv_level);
        tvTopicCount = root.findViewById(R.id.tv_topic_count);
        tvReplyCount = root.findViewById(R.id.tv_reply_count);
        tvExp = root.findViewById(R.id.tv_exp);

        llStatTopic = root.findViewById(R.id.ll_stat_topic);
        llStatReply = root.findViewById(R.id.ll_stat_reply);
        llStatExp = root.findViewById(R.id.ll_stat_exp);
        llMyTopics = root.findViewById(R.id.ll_my_topics);
        llMyReplies = root.findViewById(R.id.ll_my_replies);
        llMyCollect = root.findViewById(R.id.ll_my_collect);
        llSettings = root.findViewById(R.id.ll_settings);
        llAbout = root.findViewById(R.id.ll_about);
        btnLogout = root.findViewById(R.id.btn_logout);

        llStatTopic.setOnClickListener(this);
        llStatReply.setOnClickListener(this);
        llMyTopics.setOnClickListener(this);
        llMyReplies.setOnClickListener(this);
        llMyCollect.setOnClickListener(this);
        llSettings.setOnClickListener(this);
        llAbout.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        root.findViewById(R.id.ll_user_info).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {
        if (!isAdded() || userRepository == null) {
            return;
        }
        if (!session.isLoggedIn()) {
            // 没登录就展示个默认样子，点任何地方都提示去登录
            renderLoggedOut();
            return;
        }
        final long userId = session.getUserId();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final UserEntity user = userRepository.getUserById(userId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded() || isDetached()) {
                            return;
                        }
                        renderUser(user);
                    }
                });
            }
        });
    }

    private void renderLoggedOut() {
        tvNickname.setText("未登录");
        tvSignature.setText("登录后可以发帖和评论");
        tvLevel.setText("Lv.0");
        tvTopicCount.setText("0");
        tvReplyCount.setText("0");
        tvExp.setText("0");
        ivAvatar.setImageDrawable(null);
        ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
        ivAvatar.setImageResource(R.drawable.ic_avatar_default);
    }

    private void renderUser(UserEntity user) {
        if (user == null) {
            // 数据库里查不到（比如被清了数据），就当没登录处理
            renderLoggedOut();
            return;
        }
        tvNickname.setText(user.getNickname());
        tvSignature.setText(user.getSignature() == null || user.getSignature().isEmpty()
                ? "这个人很懒，什么都没写" : user.getSignature());
        tvLevel.setText("Lv." + user.getLevel());
        tvTopicCount.setText(String.valueOf(user.getTopicCount()));
        tvReplyCount.setText(String.valueOf(user.getReplyCount()));
        tvExp.setText(String.valueOf(user.getExp()));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageDrawable(null);
            ivAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder);
            ivAvatar.setImageResource(R.drawable.ic_avatar_default);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_user_info) {
            if (!session.isLoggedIn()) {
                goLogin();
            }
        } else if (id == R.id.ll_stat_topic || id == R.id.ll_my_topics) {
            openMyContent(MyContentActivity.TYPE_TOPIC);
        } else if (id == R.id.ll_stat_reply || id == R.id.ll_my_replies) {
            openMyContent(MyContentActivity.TYPE_REPLY);
        } else if (id == R.id.ll_stat_exp) {
            showExpTip();
        } else if (id == R.id.ll_my_collect) {
            ToastUtils.show(requireContext(), "收藏功能还没做，先占个位");
        } else if (id == R.id.ll_settings) {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        } else if (id == R.id.ll_about) {
            showAboutDialog();
        } else if (id == R.id.btn_logout) {
            confirmLogout();
        }
    }

    private void openMyContent(int type) {
        if (!session.isLoggedIn()) {
            goLogin();
            return;
        }
        Intent intent = new Intent(requireContext(), MyContentActivity.class);
        intent.putExtra(MyContentActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    private void goLogin() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
    }

    private void showExpTip() {
        ToastUtils.show(requireContext(), "发帖 +5 经验，评论 +2 经验");
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("关于")
                .setMessage("微光论坛 v1.0.0\n\n" +
                        "一个练手用的安卓论坛 App，\n" +
                        "数据都是本地的，还在慢慢完善。")
                .setPositiveButton(R.string.action_confirm, null)
                .show();
    }

    private void confirmLogout() {
        if (!session.isLoggedIn()) {
            ToastUtils.show(requireContext(), "你还没登录");
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setMessage("确定要退出登录吗？")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                session.clearLogin();
                                renderLoggedOut();
                                ToastUtils.show(requireContext(), "已退出登录");
                                startActivity(new Intent(requireContext(), LoginActivity.class));
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }
                        })
                .show();
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
