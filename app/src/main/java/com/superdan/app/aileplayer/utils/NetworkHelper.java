package com.superdan.app.aileplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrator on 2016/4/8.
 *通常可重复复用的网络方法
 */
public class NetworkHelper {
    /**
     * @param context to use to check for network connectivity.
     *                用来检测网络连接的context
     * @return true if connected, false otherwise.
     *          如果连接返回true，否则false
     */
    public static boolean isOnline(Context context){
        ConnectivityManager connMgr=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connMgr.getActiveNetworkInfo();
        return (networkInfo!=null&&networkInfo.isConnected());

    }
}
