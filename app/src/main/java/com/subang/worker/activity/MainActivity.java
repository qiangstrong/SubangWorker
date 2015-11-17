package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int NUM_ACTION = 3;

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
}
