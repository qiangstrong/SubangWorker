package com.subang.worker.activity.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.subang.bean.BasePrepayResult;
import com.subang.worker.activity.OrderActivity;
import com.subang.worker.activity.PayResultActivity;
import com.subang.worker.util.AppConst;
import com.subang.worker.util.AppShare;
import com.subang.worker.util.AppUtil;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private AppShare appShare;
    private IWXAPI wxapi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        wxapi = WXAPIFactory.createWXAPI(this, AppConst.APP_ID, true);
        wxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {            //支付成功
                appShare.map.put("order.position", 0);
                appShare.map.put("type.refresh", true);
                Intent intent = new Intent(WXPayEntryActivity.this, OrderActivity.class);
                startActivity(intent);
            } else if (resp.errCode == BaseResp.ErrCode.ERR_COMM) {    //错误
                Intent intent = new Intent(WXPayEntryActivity.this, PayResultActivity.class);
                BasePrepayResult basePrepayResult = new BasePrepayResult();
                basePrepayResult.setCode(BasePrepayResult.Code.fail);
                basePrepayResult.setMsg("支付错误");
                intent.putExtra("payresult", basePrepayResult);
                startActivity(intent);
            } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {     //用户取消
                AppUtil.tip(WXPayEntryActivity.this, "取消支付");
            }
        }
        finish();
    }
}