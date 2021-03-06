package com.subang.worker.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.subang.util.WebConst;
import com.subang.worker.fragment.TypeFragment;
import com.subang.worker.helper.MyFragmentPagerAdapter;
import com.subang.worker.util.AppShare;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends Activity {

    private static final int NUM_FRAGMENT = 3;

    private AppShare appShare;

    private TextView tv_title;
    private ViewPager vp_order;
    private ImageView[] imageViews;

    List<Fragment> fragments;
    private MyFragmentPagerAdapter fragmentPagerAdapter;

    private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            iv_onClick(imageViews[position]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare=(AppShare)getApplication();
        setContentView(R.layout.activity_order);
        findView();

        createFragments();
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager(), fragments);
        vp_order.setAdapter(fragmentPagerAdapter);
        vp_order.setOnPageChangeListener(simpleOnPageChangeListener);

        vp_order.setCurrentItem(0);
        tv_title.setText("未取订单");
    }

    @Override
    public void onResume() {
        super.onResume();
        Integer position;
        if (appShare.map.containsKey("order.position")){
            position=(Integer)appShare.map.get("order.position");
            appShare.map.remove("order.position");
            vp_order.setCurrentItem(position);
        }
    }

    private void findView() {
        tv_title=(TextView)findViewById(R.id.tv_title);
        vp_order = (ViewPager) findViewById(R.id.vp_order);
        imageViews = new ImageView[NUM_FRAGMENT];
        imageViews[0] = (ImageView) findViewById(R.id.iv_fetch);
        imageViews[1] = (ImageView) findViewById(R.id.iv_deliver);
        imageViews[2] = (ImageView) findViewById(R.id.iv_finish);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void iv_onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_fetch: {
                tv_title.setText("未取订单");
                imageViews[0].setImageResource(R.drawable.fetch_press_icon);
                imageViews[1].setImageResource(R.drawable.deliver_default_icon);
                imageViews[2].setImageResource(R.drawable.finish_default_icon);
                vp_order.setCurrentItem(0);
                break;
            }
            case R.id.iv_deliver: {
                tv_title.setText("未送订单");
                imageViews[0].setImageResource(R.drawable.fetch_default_icon);
                imageViews[1].setImageResource(R.drawable.deliver_press_icon);
                imageViews[2].setImageResource(R.drawable.finish_default_icon);
                vp_order.setCurrentItem(1);
                break;
            }
            case R.id.iv_finish: {
                tv_title.setText("完成订单");
                imageViews[0].setImageResource(R.drawable.fetch_default_icon);
                imageViews[1].setImageResource(R.drawable.deliver_default_icon);
                imageViews[2].setImageResource(R.drawable.finish_press_icon);
                vp_order.setCurrentItem(2);
                break;
            }
        }

    }

    private void createFragments() {
        fragments = new ArrayList<Fragment>(NUM_FRAGMENT);
        Bundle args = new Bundle();
        args.putInt("type", WebConst.ORDER_STATE_FETCH);
        Fragment fragment = new TypeFragment();
        fragment.setArguments(args);
        fragments.add(fragment);

        args = new Bundle();
        args.putInt("type", WebConst.ORDER_STATE_DELIVER);
        fragment = new TypeFragment();
        fragment.setArguments(args);
        fragments.add(fragment);

        args = new Bundle();
        args.putInt("type", WebConst.ORDER_STATE_FINISH);
        fragment = new TypeFragment();
        fragment.setArguments(args);
        fragments.add(fragment);
    }
}
