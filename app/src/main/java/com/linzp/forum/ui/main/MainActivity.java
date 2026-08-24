package com.linzp.forum.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.ui.main.fragment.CategoryFragment;
import com.linzp.forum.ui.main.fragment.HomeFragment;
import com.linzp.forum.ui.main.fragment.MessageFragment;
import com.linzp.forum.ui.main.fragment.MineFragment;

/**
 * 主界面。
 * 四个 tab 用 Fragment 的 show/hide 切换，不用 replace，
 * 这样来回切不会每次都重建，列表滚动位置能保留。
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG_HOME = "tab_home_frag";
    private static final String TAG_CATEGORY = "tab_category_frag";
    private static final String TAG_MESSAGE = "tab_message_frag";
    private static final String TAG_MINE = "tab_mine_frag";

    private LinearLayout tabHome;
    private LinearLayout tabCategory;
    private LinearLayout tabMessage;
    private LinearLayout tabMine;

    private ImageView ivTabHome;
    private ImageView ivTabCategory;
    private ImageView ivTabMessage;
    private ImageView ivTabMine;

    private TextView tvTabHome;
    private TextView tvTabCategory;
    private TextView tvTabMessage;
    private TextView tvTabMine;

    private Fragment homeFragment;
    private Fragment categoryFragment;
    private Fragment messageFragment;
    private Fragment mineFragment;

    private Fragment currentFragment;

    /** 当前选中的下标，onResume 回来时用来判断要不要刷新 */
    private int currentIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        initFragments(savedInstanceState);
        switchTab(0);
    }

    private void bindViews() {
        tabHome = findViewById(R.id.tab_home);
        tabCategory = findViewById(R.id.tab_category);
        tabMessage = findViewById(R.id.tab_message);
        tabMine = findViewById(R.id.tab_mine);

        ivTabHome = findViewById(R.id.iv_tab_home);
        ivTabCategory = findViewById(R.id.iv_tab_category);
        ivTabMessage = findViewById(R.id.iv_tab_message);
        ivTabMine = findViewById(R.id.iv_tab_mine);

        tvTabHome = findViewById(R.id.tv_tab_home);
        tvTabCategory = findViewById(R.id.tv_tab_category);
        tvTabMessage = findViewById(R.id.tv_tab_message);
        tvTabMine = findViewById(R.id.tv_tab_mine);

        tabHome.setOnClickListener(this);
        tabCategory.setOnClickListener(this);
        tabMessage.setOnClickListener(this);
        tabMine.setOnClickListener(this);
    }

    /**
     * 从 savedInstanceState 里把 Fragment 捞回来，横竖屏切换才不会重建页面。
     */
    private void initFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null) {
            homeFragment = fm.findFragmentByTag(TAG_HOME);
            categoryFragment = fm.findFragmentByTag(TAG_CATEGORY);
            messageFragment = fm.findFragmentByTag(TAG_MESSAGE);
            mineFragment = fm.findFragmentByTag(TAG_MINE);
        }

        FragmentTransaction transaction = fm.beginTransaction();
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
            transaction.add(R.id.fragment_container, homeFragment, TAG_HOME);
        }
        if (categoryFragment == null) {
            categoryFragment = new CategoryFragment();
            transaction.add(R.id.fragment_container, categoryFragment, TAG_CATEGORY);
        }
        if (messageFragment == null) {
            messageFragment = new MessageFragment();
            transaction.add(R.id.fragment_container, messageFragment, TAG_MESSAGE);
        }
        if (mineFragment == null) {
            mineFragment = new MineFragment();
            transaction.add(R.id.fragment_container, mineFragment, TAG_MINE);
        }
        transaction.commitNow();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tab_home) {
            switchTab(0);
        } else if (id == R.id.tab_category) {
            switchTab(1);
        } else if (id == R.id.tab_message) {
            switchTab(2);
        } else if (id == R.id.tab_mine) {
            switchTab(3);
        }
    }

    private void switchTab(int index) {
        if (index == currentIndex) {
            return;
        }
        Fragment target;
        switch (index) {
            case 0:
                target = homeFragment;
                break;
            case 1:
                target = categoryFragment;
                break;
            case 2:
                target = messageFragment;
                break;
            case 3:
                target = mineFragment;
                break;
            default:
                target = homeFragment;
                index = 0;
                break;
        }
        showFragment(target);
        currentIndex = index;
        updateTabVisual(index);
    }

    private void showFragment(Fragment target) {
        if (target == null || target == currentFragment) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!target.isAdded()) {
            transaction.add(R.id.fragment_container, target);
        } else {
            transaction.show(target);
        }
        transaction.commitAllowingStateLoss();
        currentFragment = target;
    }

    /**
     * 选中态：图标和文字都变主色，未选中的灰掉
     */
    private void updateTabVisual(int index) {
        setTabSelected(ivTabHome, tvTabHome, index == 0);
        setTabSelected(ivTabCategory, tvTabCategory, index == 1);
        setTabSelected(ivTabMessage, tvTabMessage, index == 2);
        setTabSelected(ivTabMine, tvTabMine, index == 3);
    }

    private void setTabSelected(ImageView icon, TextView text, boolean selected) {
        int color = getResources().getColor(selected ? R.color.brand_primary : R.color.text_tertiary);
        if (icon != null) {
            icon.setColorFilter(color);
        }
        if (text != null) {
            text.setTextColor(color);
            text.setTypeface(null, selected ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }
    }

    /**
     * 发帖完成后回来刷首页，其他地方调这个方法。
     */
    public void refreshHome() {
        if (homeFragment instanceof HomeFragment) {
            ((HomeFragment) homeFragment).reloadTopics();
        }
    }

    public void switchToMine() {
        switchTab(3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
