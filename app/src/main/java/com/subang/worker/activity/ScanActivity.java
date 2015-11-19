package com.subang.worker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.subang.util.WebConst;

public class ScanActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(ScanActivity.this, BarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode != RESULT_OK) {
                finish();
                return;
            }
            String barcode = intent.getStringExtra("barcode");
            Intent intent1 = new Intent(ScanActivity.this, OrderDetailActivity.class);
            intent1.putExtra("type", WebConst.ORDER_GET_BARCODE);
            intent1.putExtra("arg", barcode);
            startActivity(intent1);
            finish();
        }
    }
}
