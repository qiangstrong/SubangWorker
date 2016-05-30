package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.subang.util.WebConst;
import com.subang.worker.util.AppConf;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int NUM_ACTION = 3;

    private Thread thread;
    private PushAgent pushAgent;

    private GridView gv_action;
    private SimpleAdapter actionSimpleAdapter;
    private List<Map<String, Object>> actionItems;

    private AdapterView.OnItemClickListener actionOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                    startActivity(intent);
                    break;
                }
                case 2: {
                    Intent intent = new Intent(MainActivity.this, MineActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                pushAgent.addAlias(AppConf.cellnum, WebConst.ALIAS_TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        createItems();
        actionSimpleAdapter = new SimpleAdapter(MainActivity.this, actionItems, R.layout.item_main_action,
                new String[]{"text"}, new int[]{R.id.tv_text});
        gv_action.setAdapter(actionSimpleAdapter);
        gv_action.setOnItemClickListener(actionOnItemClickListener);

        //友盟消息推送
        pushAgent = PushAgent.getInstance(MainActivity.this);
        pushAgent.enable(umengRegisterCallback);
        pushAgent.onAppStart();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
        PushAgent.getInstance(MainActivity.this).setMuteDurationSeconds(3);
        String device_token = UmengRegistrar.getRegistrationId(MainActivity.this);
        Log.e(AppConst.LOG_TAG, device_token);

        //友盟自动更新
        UmengUpdateAgent.update(this);
    }

    private void findView() {
        gv_action = (GridView) findViewById(R.id.gv_action);
    }

    private void createItems() {
        actionItems = new ArrayList<Map<String, Object>>(NUM_ACTION);
        String[] texts = {"扫码", "订单", "个人"};
        Map<String, Object> actionItem;
        for (int i = 0; i < NUM_ACTION; i++) {
            actionItem = new HashMap<String, Object>();
            actionItem.put("text", texts[i]);
            actionItems.add(actionItem);
        }
    }

    private IUmengRegisterCallback umengRegisterCallback = new IUmengRegisterCallback() {
        @Override
        public void onRegistered(String registrationId) {
            AppUtil.conf(MainActivity.this);

            pushAgent.setNoDisturbMode(0, 0, 0, 0);
            try {
                pushAgent.addAlias(AppConf.cellnum, WebConst.ALIAS_TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(AppConst.LOG_TAG, "IUmengRegisterCallback");
        }
    };
}
