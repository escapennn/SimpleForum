package com.linzp.forum.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.data.prefs.UserSession;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.ui.main.MainActivity;
import com.linzp.forum.ui.register.RegisterActivity;
import com.linzp.forum.util.ToastUtils;

/**
 * 登录页。
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etAccount;
    private EditText etPassword;
    private ImageView ivTogglePwd;
    private CheckBox cbRemember;
    private TextView btnLogin;
    private TextView tvToRegister;
    private TextView tvForget;

    private boolean passwordVisible = false;

    private UserRepository userRepository;
    private UserSession session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = UserRepository.getInstance(this);
        session = UserSession.getInstance(this);

        bindViews();
        fillRememberedAccount();
    }

    private void bindViews() {
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        ivTogglePwd = findViewById(R.id.iv_toggle_pwd);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
        tvToRegister = findViewById(R.id.tv_to_register);
        tvForget = findViewById(R.id.tv_forget);

        btnLogin.setOnClickListener(this);
        tvToRegister.setOnClickListener(this);
        tvForget.setOnClickListener(this);
        ivTogglePwd.setOnClickListener(this);

        // 输入框有内容的时候按钮才有点击感，不然灰色一片没法判断能不能点
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButtonState();
            }
        };
        etAccount.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        updateLoginButtonState();
    }

    /**
     * 两个框都填了才让点登录，减少无效点击
     */
    private void updateLoginButtonState() {
        boolean ready = etAccount.getText().length() > 0 && etPassword.getText().length() > 0;
        btnLogin.setEnabled(ready);
        btnLogin.setAlpha(ready ? 1.0f : 0.5f);
    }

    private void fillRememberedAccount() {
        if (session.isRememberEnabled()) {
            cbRemember.setChecked(true);
            etAccount.setText(session.getSavedAccount());
            etPassword.setText(session.getSavedPassword());
            // 光标移到末尾，不然默认选中全部，用户一输入就把清掉了
            etAccount.setSelection(etAccount.getText().length());
            etPassword.setSelection(etPassword.getText().length());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            doLogin();
        } else if (id == R.id.tv_to_register) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (id == R.id.tv_forget) {
            ToastUtils.show(this, "暂时还没有找回密码的入口，先用注册吧");
        } else if (id == R.id.iv_toggle_pwd) {
            togglePasswordVisible();
        }
    }

    private void togglePasswordVisible() {
        passwordVisible = !passwordVisible;
        int selection = etPassword.getSelectionEnd();
        if (passwordVisible) {
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePwd.setImageResource(R.drawable.ic_eye_on);
        } else {
            etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePwd.setImageResource(R.drawable.ic_eye_off);
        }
        // 切换 inputType 会让光标跑到最前面，这里恢复一下
        etPassword.setSelection(Math.min(selection, etPassword.getText().length()));
    }

    private void doLogin() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(account)) {
            ToastUtils.show(this, R.string.tip_username_empty);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.show(this, R.string.tip_password_empty);
            return;
        }

        // Application 里初始化演示账号是异步的，用户手快的话可能还没插完，
        // 这里兜一下。ensureDemoUsers 内部有 count 判断和 synchronized，重复调没有副作用。
        userRepository.ensureDemoUsers();

        UserEntity user = userRepository.login(account, password);
        if (user == null) {
            // 不区分"账号不存在"和"密码错误"，避免被撞库
            ToastUtils.show(this, "账号或密码不正确");
            return;
        }

        session.saveLogin(user);
        if (cbRemember.isChecked()) {
            session.saveRememberedAccount(account, password);
        } else {
            session.clearRememberedAccount();
        }

        ToastUtils.show(this, R.string.tip_login_success);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /** 简化 TextWatcher，省得三个方法都实现一遍 */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
