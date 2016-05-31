package com.subang.worker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.subang.api.RecordAPI;
import com.subang.api.UserAPI;
import com.subang.bean.AddrDetail;
import com.subang.bean.RecordDetail;
import com.subang.worker.helper.AddrDataHelper;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;
import com.subang.worker.util.ComUtil;

public class RecordDetailActivity extends Activity {

    private static final int WHAT_DETAIL = 1;
    private static final int WHAT_ADDR = 2;

    private Integer recordid;

    private TextView tv_recordno, tv_datetime, tv_categoryname, tv_addrname, tv_addrcellnum,
            tv_area, tv_addrdetail, tv_payType, tv_payment;

    private Thread thread;
    private RecordDetail recordDetail;
    private AddrDetail addrDetail;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(RecordDetailActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(RecordDetailActivity.this, info);
                    break;
                }
                case WHAT_DETAIL: {
                    showDetail();
                    break;
                }
                case WHAT_ADDR:{
                    showAddr();
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(RecordDetailActivity.this);
            recordDetail = RecordAPI.get(recordid);
            if (recordDetail == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_DETAIL);
            addrDetail = UserAPI.getAddr(recordDetail.getAddrid());
            if (addrDetail == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_ADDR);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        findView();
        recordid = getIntent().getIntExtra("recordid", 0);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        tv_recordno = (TextView) findViewById(R.id.tv_recordno);
        tv_datetime = (TextView) findViewById(R.id.tv_datetime);
        tv_categoryname = (TextView) findViewById(R.id.tv_categoryname);
        tv_addrcellnum = (TextView) findViewById(R.id.tv_addrcellnum);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_addrdetail = (TextView) findViewById(R.id.tv_addrdetail);
        tv_payType = (TextView) findViewById(R.id.tv_pay_type);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_addrname = (TextView) findViewById(R.id.tv_addrname);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    private void showDetail() {
        //订单信息
        tv_recordno.setText(recordDetail.getOrderno());
        tv_datetime.setText(recordDetail.getTimeDes());
        tv_categoryname.setText(recordDetail.getName());

        //支付信息
        tv_payType.setText(recordDetail.getPayTypeDes());
        tv_payment.setText(recordDetail.getPaymentDes());
    }

    private void showAddr() {
        //地址信息
        tv_addrname.setText(addrDetail.getName());
        tv_addrcellnum.setText(addrDetail.getCellnum());
        tv_area.setText(AddrDataHelper.getAreaDes(addrDetail));
        tv_addrdetail.setText(addrDetail.getDetail());
    }
}
