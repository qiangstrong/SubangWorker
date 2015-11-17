package com.subang.worker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.subang.api.SubangAPI;
import com.subang.domain.Worker;
import com.subang.util.WebConst;
import com.subang.worker.activity.R;

/**
 * Created by Qiang on 2015/10/31.
 */
public class AppUtil {

    //把用户信息（app配置）保存在磁盘
    public static void saveConf(Context context, Worker worker) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_worker), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cellnum", worker.getCellnum());
        editor.putString("password", worker.getPassword());
        editor.commit();
        AppConf.invalidate();
        SubangAPI.invalidate();
    }

    //从磁盘删除用户信息（app配置）
    public static void deleteConf(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_worker), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        AppConf.invalidate();
        SubangAPI.invalidate();
    }

    //配置app，使用AppConf前调用此函数。如果没有配置，则配置
    public static boolean conf(Context context) {
        if (AppConf.isConfed()) {
            return true;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_worker), Context.MODE_PRIVATE);
        String basePath = context.getFilesDir().getAbsolutePath() + "/";
        String cellnum = sharedPreferences.getString("cellnum", null);
        String password = sharedPreferences.getString("password", null);
        if (cellnum != null && password != null) {
            AppConf.basePath = basePath;
            AppConf.cellnum = cellnum;
            AppConf.password = password;
            return true;
        }
        return false;
    }

    //配置api，使用api前调用此函数。如果没有配置，则配置
    public static void confApi(Context context) {
        if (SubangAPI.isConfed()) {
            return;
        }
        conf(context);
        SubangAPI.conf(WebConst.WORKER, AppConf.cellnum, AppConf.password, AppConf.basePath);
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    public static void networkTip(Context context) {
        Toast toast = Toast.makeText(context, R.string.err_network, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void tip(Context context, String info) {
        Toast toast = Toast.makeText(context, info, Toast.LENGTH_SHORT);
        toast.show();
    }
}
