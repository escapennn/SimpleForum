package com.linzp.forum.ui.register;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.linzp.forum.R;
import com.linzp.forum.base.BaseActivity;
import com.linzp.forum.data.repository.UserRepository;
import com.linzp.forum.util.CommonUtils;
import com.linzp.forum.util.ToastUtils;

/**
 * 注册页。
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivBack;
    private EditText etUsername;
    private EditText etNickname;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private CheckBox cbAgreement;
    private TextView btnRegister;
    private TextView tvToLogin;

    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = UserRepository.getInstance(this);

        ivBack = findViewById(R.id.iv_back);
        etUsername = findViewById(R.id.et_username);
        etNickname = findViewById(R.id.et_nickname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        cbAgreement = findViewById(R.id.cb_agreement);
        btnRegister = findViewById(R.id.btn_register);
        tvToLogin = findViewById(R.id.tv_to_login);

        ivBack.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        tvToLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back || id == R.id.tv_to_login) {
            finish();
        } else if (id == R.id.btn_register) {
            doRegister();
        }
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etPasswordConfirm.getText().toString();

        // 先本地校验一遍再走 Repository，能少查几次库
        if (TextUtils.isEmpty(username)) {
            ToastUtils.show(this, R.string.tip_username_empty);
            return;
        }
        if (!CommonUtils.isValidUsername(username)) {
            ToastUtils.show(this, "用户名只能包含字母、数字和下划线，长度 3-16 位");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            ToastUtils.show(this, "请填写邮箱");
            return;
        }
        if (!CommonUtils.isValidEmail(email)) {
            ToastUtils.show(this, R.string.tip_email_invalid);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtils.show(this, R.string.tip_password_empty);
            return;
        }
        if (!CommonUtils.isValidPassword(password)) {
            ToastUtils.show(this, R.string.tip_password_short);
            return;
        }
        if (!password.equals(confirm)) {
            ToastUtils.show(this, R.string.tip_password_not_match);
            return;
        }
        if (!cbAgreement.isChecked()) {
            ToastUtils.show(this, "请先勾选同意用户协议");
            return;
        }

        UserRepository.RegisterResult result =
                userRepository.register(username, nickname, email, password);

        switch (result) {
            case SUCCESS:
                ToastUtils.show(this, R.string.tip_register_success);
                finish();
                break;
            case USERNAME_EXISTS:
                ToastUtils.show(this, "这个用户名已经被人用了，换一个吧");
                break;
            case EMAIL_EXISTS:
                ToastUtils.show(this, "这个邮箱已经注册过了");
                break;
            case INVALID_USERNAME:
                ToastUtils.show(this, "用户名格式不对");
                break;
            case INVALID_EMAIL:
                ToastUtils.show(this, R.string.tip_email_invalid);
                break;
            case INVALID_PASSWORD:
                ToastUtils.show(this, R.string.tip_password_short);
                break;
            default:
                ToastUtils.show(this, "注册失败，请稍后再试");
                break;
        }
    }
}
