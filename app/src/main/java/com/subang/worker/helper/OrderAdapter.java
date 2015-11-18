package com.subang.worker.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.subang.bean.OrderDetail;
import com.subang.worker.activity.R;

import java.util.List;

/**
 * Created by Qiang on 2015/11/1.
 */
public class OrderAdapter extends BaseAdapter {

    private ImageView iv_categorylogo;
    private TextView tv_categoryname, tv_state, tv_orderno, tv_datetime, tv_totalMoney, tv_operation1, tv_operation2;
    private OrderDetail orderDetail;

    private LayoutInflater inflater;
    private DataHolder dataHolder;
    private View.OnClickListener operationOnClickListener;


    public OrderAdapter(Context context, DataHolder dataHolder) {
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
        if (dataHolder.orderDetails != null) {
            return dataHolder.orderDetails.size();
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
        if (dataHolder.orderDetails == null) {
            return null;       //应该不会执行到
        }
        orderDetail = dataHolder.orderDetails.get(position);
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
        if (orderDetail.getCategoryname().equals("洗鞋")) {
            iv_categorylogo.setImageResource(R.drawable.washing_shoes);
        } else {
            iv_categorylogo.setImageResource(R.drawable.washing_clothes);
        }
        tv_categoryname.setText(orderDetail.getCategoryname());
        tv_state.setText(orderDetail.getStateDes());
        tv_orderno.setText("订单编号：" + orderDetail.getOrderno());
        tv_datetime.setText("取件时间： " + orderDetail.getDateDes() + " " + orderDetail.getTimeDes());
        tv_totalMoney.setText("订单总额： " + orderDetail.getTotalMoneyDes());
    }

    private void bindOperation() {

        switch (orderDetail.getStateEnum()) {
            case accepted: {
                tv_operation1.setVisibility(View.VISIBLE);
                tv_operation1.setText(R.string.operation_price);
                tv_operation1.setTag(R.id.key_operation, Operation.price);

                tv_operation2.setVisibility(View.VISIBLE);
                tv_operation2.setText(R.string.operation_comment);
                tv_operation2.setTag(R.id.key_operation, Operation.comment);
                break;
            }
            case priced: {
                tv_operation1.setVisibility(View.VISIBLE);
                tv_operation1.setText(R.string.operation_pay);
                tv_operation1.setTag(R.id.key_operation, Operation.pay);

                tv_operation2.setVisibility(View.VISIBLE);
                tv_operation2.setText(R.string.operation_price);
                tv_operation2.setTag(R.id.key_operation, Operation.price);
                break;
            }
            case paid: {
                tv_operation1.setVisibility(View.VISIBLE);
                tv_operation1.setText(R.string.operation_fetch);
                tv_operation1.setTag(R.id.key_operation, Operation.fetch);

                tv_operation2.setVisibility(View.VISIBLE);
                tv_operation2.setText(R.string.operation_comment);
                tv_operation2.setTag(R.id.key_operation, Operation.comment);
                break;
            }
        }
        tv_operation1.setTag(R.id.key_orderid, orderDetail.getId());
        tv_operation1.setOnClickListener(operationOnClickListener);
        tv_operation2.setTag(R.id.key_orderid, orderDetail.getId());
        tv_operation2.setOnClickListener(operationOnClickListener);
    }

    public static class DataHolder {
        public List<OrderDetail> orderDetails;
    }

    public enum Operation {
        price, pay, fetch, comment
    }
}
