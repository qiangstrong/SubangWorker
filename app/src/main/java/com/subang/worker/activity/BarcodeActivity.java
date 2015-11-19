package com.subang.worker.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.Result;
import com.subang.worker.util.AppUtil;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

public class BarcodeActivity extends FragmentActivity {

    private TextView tv_barcode;
    private BarCodeScannerFragment fm_scan;
    private boolean isStop = false;

    private BarCodeScannerFragment.IResultCallback resultCallback = new BarCodeScannerFragment.IResultCallback() {
        @Override
        public void result(Result lastResult) {
            if (!isStop) {
                tv_barcode.setText(lastResult.getText());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        tv_barcode = (TextView) findViewById(R.id.tv_barcode);
        fm_scan = (BarCodeScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fm_sacn);
        fm_scan.setmCallBack(resultCallback);
    }

    public void btn_stop_onClick(View view) {
        isStop = true;
    }

    public void btn_ok_onClick(View view) {
        int barcodeLength = getResources().getInteger(R.integer.barcode);
        String barcode = tv_barcode.getText().toString();
        if (barcode.length() != barcodeLength) {
            AppUtil.tip(BarcodeActivity.this, "条形码错误。");
            return;
        }
        Intent intent = getIntent();
        intent.putExtra("barcode", barcode);
        setResult(RESULT_OK, intent);
        finish();
    }
}
