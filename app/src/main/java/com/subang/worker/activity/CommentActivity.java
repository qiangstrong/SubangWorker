package com.subang.worker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.subang.api.OrderAPI;
import com.subang.bean.Result;
import com.subang.worker.helper.MyTextWatcher;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppUtil;

public class CommentActivity extends Activity {

    private Integer orderid;

    private EditText et_comment;
    private TextView tv_ok;

    private Thread thread;
    private String comment;

    private MyTextWatcher commentWatcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (commentWatcher.isAvail()) {
                tv_ok.setEnabled(true);
            } else {
                tv_ok.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(CommentActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    AppUtil.tip(CommentActivity.this, "备注提交成功，谢谢。");
                    CommentActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(CommentActivity.this);
            Result result = OrderAPI.comment(orderid, comment);
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
        orderid = getIntent().getIntExtra("orderid", 0);
        setContentView(R.layout.activity_comment);
        findView();
        commentWatcher = new MyTextWatcher(1, onPrepareListener);
        et_comment.addTextChangedListener(commentWatcher);
    }

    private void findView() {
        et_comment = (EditText) findViewById(R.id.et_comment);
        tv_ok=(TextView)findViewById(R.id.tv_ok);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void tv_ok_onClick(View view) {
        comment= et_comment.getText().toString();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }
}
