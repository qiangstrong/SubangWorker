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

import com.subang.api.RecordAPI;
import com.subang.applib.view.XListView;
import com.subang.bean.RecordDetail;
import com.subang.bean.Result;
import com.subang.domain.Order;
import com.subang.domain.Payment;
import com.subang.worker.activity.R;
import com.subang.worker.activity.RecordDetailActivity;
import com.subang.worker.fragment.face.OnFrontListener;
import com.subang.worker.helper.RecordAdapter;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppShare;
import com.subang.worker.util.AppUtil;
import com.subang.worker.util.ComUtil;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;


public class RecordFragment extends Fragment implements OnFrontListener {

    private AppShare appShare;

    private int type;
    private XListView xlv_record;
    private RecordAdapter recordAdapter;

    private Thread thread, operaThread;
    List<RecordDetail> recordDetails;
    private RecordAdapter.DataHolder dataHolder;
    private RecordDetail filter;

    private boolean isLoaded = false;

    AdapterView.OnItemClickListener recordOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position = position - 1;
            if (position >= 0 && position < recordDetails.size()) {
                Intent intent = new Intent(getActivity(), RecordDetailActivity.class);
                intent.putExtra("recordid", recordDetails.get(position).getId());
                startActivity(intent);
            }
        }
    };

    XListView.IXListViewListener recordListViewListener = new XListView.IXListViewListener() {
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
                    xlv_record.stopRefresh();
                    AppUtil.networkTip(getActivity());
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    xlv_record.stopRefresh();
                    dataHolder.recordDetails = recordDetails;
                    recordAdapter.notifyDataSetChanged();
                    if (recordDetails.isEmpty()) {
                        xlv_record.setBackgroundResource(R.drawable.listview_no_order);
                    } else {
                        xlv_record.setBackgroundResource(android.R.color.transparent);
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
            recordDetails = RecordAPI.workerList(type, filter);
            if (recordDetails == null) {
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
        dataHolder = new RecordAdapter.DataHolder();
        filter = new RecordDetail();
        filter.setId(0);
        filter.setOrderno("");
        filter.setState(Order.State.accepted);
        filter.setTime(new Timestamp(System.currentTimeMillis()));
        filter.setMoney(0.0);
        filter.setScore(0);
        filter.setName("");
        filter.setPayType(Payment.PayType.balance);

        recordAdapter = new RecordAdapter(getActivity(), dataHolder);
        recordAdapter.setOperationOnClickListener(operationOnClickListener);
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

        xlv_record.setAdapter(recordAdapter);
        xlv_record.setOnItemClickListener(recordOnItemClickListener);
        xlv_record.setXListViewListener(recordListViewListener);
        xlv_record.setPullLoadEnable(false);
        xlv_record.setPullRefreshEnable(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refresh;
        if (appShare.map.containsKey("record.type.refresh")) {
            refresh = (boolean) appShare.map.get("record.type.refresh");
            appShare.map.remove("record.type.refresh");
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
        xlv_record = (XListView) view.findViewById(R.id.xlv_order);
    }

    private View.OnClickListener operationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OperaData operaData = new OperaData();
            operaData.operation = (RecordAdapter.Operation) v.getTag(R.id.key_operation);
            operaData.recordid = (Integer) v.getTag(R.id.key_orderid);
            switch (operaData.operation) {
                case deliver: {
                    if (operaThread == null || !operaThread.isAlive()) {
                        operaThread = new OperaThread(operaData);
                        operaThread.start();
                    }
                    break;
                }
            }
        }
    };

    private static class OperaData implements Serializable {
        public RecordAdapter.Operation operation;
        public Integer recordid;
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
                case deliver: {
                    result = RecordAPI.deliver(operaData.recordid);
                    if (result == null) {
                        handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                        return;
                    }
                    msg = ComUtil.getMessage(AppConst.WHAT_SUCC_SUBMIT, "订单送达成功。");
                    handler.sendMessage(msg);
                    break;
                }
            }
        }
    }
}
