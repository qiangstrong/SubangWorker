package com.subang.worker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.subang.api.WorkerAPI;
import com.subang.bean.Result;
import com.subang.domain.Worker;
import com.subang.worker.helper.MyTextWatcher;
import com.subang.worker.util.AppConf;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;
import com.subang.worker.util.ComUtil;

public class PasswordActivity extends Activity {

    private EditText et_password1, et_password2;
    private TextView tv_ok;

    private Thread thread;
    private String password;

    private MyTextWatcher password1Watcher, password2Watcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (password1Watcher.isAvail() && password2Watcher.isAvail()) {
                tv_ok.setEnabled(true);
            } else {
                tv_ok.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PasswordActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(PasswordActivity.this, info);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    AppUtil.tip(PasswordActivity.this, "密码更改成功。");
                    PasswordActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PasswordActivity.this);
            Result result = WorkerAPI.chgPassword(password);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            AppUtil.conf(PasswordActivity.this);
            Worker worker = new Worker();
            worker.setCellnum(AppConf.cellnum);
            worker.setPassword(password);
            AppUtil.saveConf(PasswordActivity.this, worker);
            AppUtil.conf(PasswordActivity.this);
            AppUtil.confApi(PasswordActivity.this);
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        findView();
        password1Watcher = new MyTextWatcher(1, onPrepareListener);
        et_password1.addTextChangedListener(password1Watcher);
        password2Watcher = new MyTextWatcher(1, onPrepareListener);
        et_password2.addTextChangedListener(password2Watcher);
    }

    private void findView() {
        et_password1 = (EditText) findViewById(R.id.et_password1);
        et_password2 = (EditText) findViewById(R.id.et_password2);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
    }

    public void tv_ok_onClick(View view) {
        String password1 = et_password1.getText().toString();
        String password2 = et_password2.getText().toString();
        if (!password1.equals(password2)) {
            AppUtil.tip(PasswordActivity.this, "两次输入密码不一致。");
            return;
        }
        password = password1;
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }
}
