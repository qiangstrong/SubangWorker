package com.subang.worker.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.subang.bean.RecordDetail;
import com.subang.worker.activity.R;

import java.util.List;

/**
 * Created by Qiang on 2015/11/1.
 */
public class RecordAdapter extends BaseAdapter {

    private ImageView iv_categorylogo;
    private TextView tv_categoryname, tv_state, tv_orderno, tv_datetime, tv_totalMoney, tv_operation1, tv_operation2;
    private RecordDetail recordDetail;

    private LayoutInflater inflater;
    private DataHolder dataHolder;
    private View.OnClickListener operationOnClickListener;


    public RecordAdapter(Context context, DataHolder dataHolder) {
        super();
        this.dataHolder = dataHolder;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View.OnClickListener getOperationOnClickListener() {
        return operationOnClickListener;
    }

    public void setOperationOnClickListener(View.OnClickListener operationOnClickListener) {
        this.operationOnClickListener = operationOnClickListener;
    }

    @Override
    public int getCount() {
        if (dataHolder.recordDetails != null) {
            return dataHolder.recordDetails.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_order, null);
        }
        findView(view);
        if (dataHolder.recordDetails == null) {
            return null;       //应该不会执行到
        }
        recordDetail = dataHolder.recordDetails.get(position);
        bindData();
        bindOperation();
        return view;
    }

    private void findView(View view) {
        iv_categorylogo = (ImageView) view.findViewById(R.id.iv_categorylogo);
        tv_categoryname = (TextView) view.findViewById(R.id.tv_categoryname);
        tv_state = (TextView) view.findViewById(R.id.tv_state);
        tv_orderno = (TextView) view.findViewById(R.id.tv_orderno);
        tv_datetime = (TextView) view.findViewById(R.id.tv_datetime);
        tv_totalMoney = (TextView) view.findViewById(R.id.tv_total_money);
        tv_operation1 = (TextView) view.findViewById(R.id.tv_operation_1);
        tv_operation2 = (TextView) view.findViewById(R.id.tv_operation_2);
    }

    private void bindData() {
        iv_categorylogo.setImageResource(R.drawable.washing_clothes);
        tv_categoryname.setText(recordDetail.getName());
        tv_state.setText(recordDetail.getStateDes());
        tv_orderno.setText("订单编号：" + recordDetail.getOrderno());
        tv_datetime.setText("下单时间： " + recordDetail.getTimeDes());
        tv_totalMoney.setText(recordDetail.getPaymentDes());
    }

    private void bindOperation() {

        tv_operation1.setVisibility(View.INVISIBLE);
        tv_operation2.setVisibility(View.INVISIBLE);

        switch (recordDetail.getStateEnum()) {
            case paid: {
                tv_operation1.setVisibility(View.VISIBLE);
                tv_operation1.setText(R.string.operation_deliver);
                tv_operation1.setTag(R.id.key_operation, Operation.deliver);
                break;
            }
        }
        tv_operation1.setTag(R.id.key_orderid, recordDetail.getId());
        tv_operation1.setOnClickListener(operationOnClickListener);
        tv_operation2.setTag(R.id.key_orderid, recordDetail.getId());
        tv_operation2.setOnClickListener(operationOnClickListener);
    }

    public static class DataHolder {
        public List<RecordDetail> recordDetails;
    }

    public enum Operation {
        price, pay, fetch, comment, deliver
    }
}
