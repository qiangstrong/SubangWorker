package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;
import com.subang.worker.util.ComUtil;

public class LoginActivity extends Activity {

    private static final int WHAT_MAIN = 1;

    private EditText et_cellnum, et_password;
    private TextView tv_login;

    private Thread thread;
    private Worker worker;

    private MyTextWatcher cellnumWatcher, passwordWatcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (cellnumWatcher.isAvail() && passwordWatcher.isAvail()) {
                tv_login.setEnabled(true);
            } else {
                tv_login.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(LoginActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(LoginActivity.this, info);
                    break;
                }
                case WHAT_MAIN: {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(LoginActivity.this);
            Result result = WorkerAPI.login(worker);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "用户名或密码错误。");
                handler.sendMessage(msg);
                return;
            }
            AppUtil.saveConf(LoginActivity.this, worker);
            AppUtil.conf(LoginActivity.this);
            AppUtil.confApi(LoginActivity.this);
            handler.sendEmptyMessage(WHAT_MAIN);                //转主界面
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        worker = new Worker();

        int cellnumLength = getResources().getInteger(R.integer.cellnum);
        cellnumWatcher = new MyTextWatcher(cellnumLength, onPrepareListener);
        et_cellnum.addTextChangedListener(cellnumWatcher);
        passwordWatcher = new MyTextWatcher(1, onPrepareListener);
        et_password.addTextChangedListener(passwordWatcher);
    }

    private void findView() {
        et_cellnum = (EditText) findViewById(R.id.et_cellnum);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_login = (TextView) findViewById(R.id.tv_login);
    }

    public void tv_login_onClick(View view) {
        worker.setCellnum(et_cellnum.getText().toString());
        worker.setPassword(et_password.getText().toString());
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }
}
