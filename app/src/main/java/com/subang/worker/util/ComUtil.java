package com.subang.worker.util;

import android.os.Message;

/**
 * Created by Qiang on 2015/11/10.
 */
public class ComUtil {

    public static Message getMessage(int what,String info){
        Message msg = new Message();
        msg.what = what;
        msg.obj=info;
        return msg;
    }

    public static String getInfo(Message msg){
        return (String)msg.obj;
    }
}
