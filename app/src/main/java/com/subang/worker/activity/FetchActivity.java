package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.subang.api.OrderAPI;
import com.subang.bean.Result;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;

public class FetchActivity extends Activity {

    private Thread thread;
    private Integer orderid;
    private String barcode;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(FetchActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    AppUtil.tip(FetchActivity.this, "订单取走成功。");
                    FetchActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(FetchActivity.this);
            Result result = OrderAPI.fetch(orderid,barcode);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderid = getIntent().getIntExtra("orderid", 0);
        Intent intent = new Intent(FetchActivity.this, BarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode != RESULT_OK) {
                finish();
                return;
            }
            barcode = intent.getStringExtra("barcode");
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(runnable);
                thread.start();
            }
        }
    }
}
