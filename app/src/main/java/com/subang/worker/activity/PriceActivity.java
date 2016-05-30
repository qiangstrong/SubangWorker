package com.subang.worker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.subang.api.OrderAPI;
import com.subang.bean.Result;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppShare;
import com.subang.worker.util.AppUtil;

public class PriceActivity extends Activity {

    private AppShare appShare;
    private Integer orderid;

    private EditText et_money;

    private Thread submitThread;
    private double totalMoney;       //不含运费


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PriceActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    appShare.map.put("order.position", 0);
                    appShare.map.put("type.refresh", true);
                    AppUtil.tip(PriceActivity.this, "计价成功。");
                    PriceActivity.this.finish();
                    break;
                }
            }

        }
    };


    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PriceActivity.this);
            Result result = OrderAPI.price(orderid, totalMoney);
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
        appShare = (AppShare) getApplication();
        orderid = getIntent().getIntExtra("orderid", 0);
        setContentView(R.layout.activity_price);
        findView();
    }

    private void findView() {
        et_money = (EditText) findViewById(R.id.et_money);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void tv_ok_onClick(View view) {
        String moneyText = et_money.getText().toString();
        if (moneyText.length() == 0) {
            AppUtil.tip(PriceActivity.this, "请输入金额");
            return;
        }
        Double money = null;
        try {
            money = Double.parseDouble(moneyText);
        } catch (Exception e) {
            AppUtil.tip(PriceActivity.this, "金额输入错误");
            return;
        }
        money = com.subang.util.ComUtil.round(money);
        if (!(money > 0 && money < 10000)) {
            AppUtil.tip(PriceActivity.this, "金额必须在0.1元到10000元之间");
            return;
        }
        totalMoney=money;
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
    }
}
