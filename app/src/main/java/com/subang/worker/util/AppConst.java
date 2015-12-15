package com.subang.worker.util;

/**
 * Created by Qiang on 2015/11/7.
 */
public interface AppConst {

    String APP_ID="wx1d5308f09a0aefb5";
    String LOG_TAG = "subang";

    //用于标识handler所发message的类型
    int WHAT_NETWORK_ERR = 0;
    int WHAT_SUCC_LOAD = -1;
    int WHAT_SUCC_SUBMIT = -2;
    int WHAT_INFO = -3;
}
