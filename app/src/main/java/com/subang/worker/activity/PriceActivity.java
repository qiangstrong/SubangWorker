package com.subang.worker.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.OrderAPI;
import com.subang.api.PriceAPI;
import com.subang.applib.util.ComUtil;
import com.subang.applib.util.SwipeMenu;
import com.subang.applib.util.SwipeMenuCreator;
import com.subang.applib.util.SwipeMenuItem;
import com.subang.applib.view.SwipeMenuListView;
import com.subang.applib.view.WheelView;
import com.subang.bean.OrderDetail;
import com.subang.bean.Result;
import com.subang.domain.Price;
import com.subang.util.WebConst;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppShare;
import com.subang.worker.util.AppUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceActivity extends Activity {

    private AppShare appShare;
    private Integer orderid;

    private WheelView wv_price;
    private TextView tv_add, tv_total;
    private SwipeMenuListView lv_moeny;

    private Thread thread, submitThread;
    private List<Price> prices;
    private List<Double> moenys;
    private List<Map<String, Object>> moneyItems;
    private double totalMoney;       //不含运费

    private SimpleAdapter moneySimpleAdapter;

    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
            deleteItem.setWidth(ComUtil.dp2px(PriceActivity.this, 90));
            deleteItem.setIcon(R.drawable.delete_icon);
            menu.addMenuItem(deleteItem);
        }
    };

    private SwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            moenys.remove(position);
            moneyItems.remove(position);
            moneySimpleAdapter.notifyDataSetChanged();
            aboutTotalMoney();
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PriceActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    wv_price.setItems(toPriceList(prices));
                    tv_add.setEnabled(true);
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PriceActivity.this);
            OrderDetail orderDetail = OrderAPI.get(WebConst.ORDER_GET_ID,orderid.toString());
            if (orderDetail == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            Price filter = new Price();
            filter.setId(0);
            filter.setMoney(0.0);
            prices = PriceAPI.listprice(orderDetail.getCategoryid(), filter);
            if (prices == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
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
        List<String> placeholder = new ArrayList<>();
        placeholder.add("加载中");
        wv_price.setItems(placeholder);
        moenys = new ArrayList<>();
        moneyItems = new ArrayList<>();
        moneySimpleAdapter = new SimpleAdapter(PriceActivity.this, moneyItems, R.layout.item_money,
                new String[]{"money"}, new int[]{R.id.tv_money});
        lv_moeny.setAdapter(moneySimpleAdapter);
        lv_moeny.setMenuCreator(swipeMenuCreator);
        lv_moeny.setOnMenuItemClickListener(onMenuItemClickListener);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        wv_price = (WheelView) findViewById(R.id.wv_price);
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_total = (TextView) findViewById(R.id.tv_total);
        lv_moeny = (SwipeMenuListView) findViewById(R.id.lv_moeny);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void tv_add_onClick(View view) {
        Price price = prices.get(wv_price.getSelectedIndex());
        moenys.add(price.getMoney());
        Map<String, Object> moneyItem = new HashMap<>();
        moneyItem.put("money", Double.toString(price.getMoney()));
        moneyItems.add(moneyItem);
        moneySimpleAdapter.notifyDataSetChanged();
        aboutTotalMoney();
    }

    public void tv_ok_onClick(View view) {
        if (moenys.isEmpty()) {
            AppUtil.tip(PriceActivity.this, "请添加价格。");
            return;
        }
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
    }

    private List<String> toPriceList(List<Price> prices) {
        List<String> moneys = new ArrayList<>();
        for (Price price : prices) {
            moneys.add(price.getMoney().toString());
        }
        return moneys;
    }

    private void aboutTotalMoney() {
        double tempTotalMoeny = 0;
        for (double money : moenys) {
            tempTotalMoeny += money;
        }
        totalMoney = com.subang.util.ComUtil.round(tempTotalMoeny);
        tv_total.setText(totalMoney + "元");
    }
}
