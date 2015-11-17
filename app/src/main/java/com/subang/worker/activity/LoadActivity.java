package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.subang.api.WorkerAPI;
import com.subang.bean.Result;
import com.subang.domain.Worker;
import com.subang.worker.util.AppConf;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;

public class LoadActivity extends Activity {

    private static final int WHAT_LOGIN = 1;
    private static final int WHAT_MAIN = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(LoadActivity.this);
                    break;
                }
                case WHAT_LOGIN: {
                    Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
                case WHAT_MAIN: {
                    Intent intent = new Intent(LoadActivity.this, MainActivity.class);
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
            if (!AppUtil.checkNetwork(LoadActivity.this)) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }

            if (!AppUtil.conf(LoadActivity.this)) {
                handler.sendEmptyMessage(WHAT_LOGIN);    //转登录界面
                return;
            }
            Worker worker = new Worker();
            worker.setCellnum(AppConf.cellnum);
            worker.setPassword(AppConf.password);
            Result result = WorkerAPI.login(worker);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                handler.sendEmptyMessage(WHAT_LOGIN);    //转登录界面
                return;
            }
            AppUtil.confApi(LoadActivity.this);
            handler.sendEmptyMessage(WHAT_MAIN);        //转主界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        new Thread(runnable).start();
    }
}
