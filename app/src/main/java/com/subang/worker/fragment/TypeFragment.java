package com.subang.worker.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.subang.api.OrderAPI;
import com.subang.applib.view.XListView;
import com.subang.bean.OrderDetail;
import com.subang.bean.Result;
import com.subang.domain.Order;
import com.subang.worker.activity.CommentActivity;
import com.subang.worker.activity.OrderDetailActivity;
import com.subang.worker.activity.PriceActivity;
import com.subang.worker.activity.R;
import com.subang.worker.fragment.face.OnFrontListener;
import com.subang.worker.helper.OrderAdapter;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppShare;
import com.subang.worker.util.AppUtil;
import com.subang.worker.util.ComUtil;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;


public class TypeFragment extends Fragment implements OnFrontListener {

    private AppShare appShare;

    private int type;
    private XListView xlv_order;
    private OrderAdapter orderAdapter;

    private Thread thread, operaThread;
    List<OrderDetail> orderDetails;
    private OrderAdapter.DataHolder dataHolder;
    private OrderDetail filter;

    private boolean isLoaded = false;

    AdapterView.OnItemClickListener orderOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position = position - 1;
            if (position >= 0 && position < orderDetails.size()) {
                Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                intent.putExtra("orderid", orderDetails.get(position).getId());
                startActivity(intent);
            }
        }
    };

    XListView.IXListViewListener orderListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(runnable);
                thread.start();
            }
        }

        @Override
        public void onLoadMore() {
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    xlv_order.stopRefresh();
                    AppUtil.networkTip(getActivity());
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    xlv_order.stopRefresh();
                    dataHolder.orderDetails = orderDetails;
                    orderAdapter.notifyDataSetChanged();
                    if (orderDetails.isEmpty()) {
                        xlv_order.setBackgroundResource(R.drawable.listview_no_order);
                    } else {
                        xlv_order.setBackgroundResource(android.R.color.transparent);
                    }
                    isLoaded = true;
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (thread == null || !thread.isAlive()) {
                        thread = new Thread(runnable);
                        thread.start();
                    }
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(getActivity(), info);
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            orderDetails = OrderAPI.workerList(type, filter);
            if (orderDetails == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);           //加载数据失败
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);                //加载数据成功
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getActivity().getApplication();
        if (getArguments().containsKey("type")) {
            type = getArguments().getInt("type");
        }
        dataHolder = new OrderAdapter.DataHolder();
        filter = new OrderDetail();
        filter.setId(0);
        filter.setOrderno("");
        filter.setState(Order.State.accepted);
        filter.setDate(new Date(System.currentTimeMillis()));
        filter.setTime(0);
        filter.setMoney(0.0);
        filter.setFreight(0.0);
        filter.setMoneyTicket(0.0);
        filter.setCategoryname("");

        orderAdapter = new OrderAdapter(getActivity(), dataHolder);
        orderAdapter.setOperationOnClickListener(operationOnClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type, container, false);
        findView(view);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        xlv_order.setAdapter(orderAdapter);
        xlv_order.setOnItemClickListener(orderOnItemClickListener);
        xlv_order.setXListViewListener(orderListViewListener);
        xlv_order.setPullLoadEnable(false);
        xlv_order.setPullRefreshEnable(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refresh;
        if (appShare.map.containsKey("type.refresh")) {
            refresh = (boolean) appShare.map.get("type.refresh");
            appShare.map.remove("type.refresh");
            if (refresh) {
                if (thread == null || !thread.isAlive()) {
                    thread = new Thread(runnable);
                    thread.start();
                }
            }
        }
    }

    @Override
    public void onFront() {
    }

    private void findView(View view) {
        xlv_order = (XListView) view.findViewById(R.id.xlv_order);
    }

    private View.OnClickListener operationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OperaData operaData = new OperaData();
            operaData.operation = (OrderAdapter.Operation) v.getTag(R.id.key_operation);
            operaData.orderid = (Integer) v.getTag(R.id.key_orderid);
            switch (operaData.operation) {
                case price: {
                    Intent intent = new Intent(getActivity(), PriceActivity.class);
                    intent.putExtra("orderid", operaData.orderid);
                    startActivity(intent);
                    break;
                }
                case pay: {
                    break;
                }
                case fetch: {
                    break;
                }
                case comment: {
                    Intent intent = new Intent(getActivity(), CommentActivity.class);
                    intent.putExtra("orderid", operaData.orderid);
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private static class OperaData implements Serializable {
        public OrderAdapter.Operation operation;
        public Integer orderid;
    }

    private class OperaThread extends Thread {

        OperaData operaData;

        public OperaThread(OperaData operaData) {
            super();
            this.operaData = operaData;
        }

        @Override
        public void run() {
            Result result;
            Message msg;
            AppUtil.confApi(getActivity());
            switch (operaData.operation) {

            }
        }
    }
}
