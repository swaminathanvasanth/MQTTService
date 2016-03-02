package com.example.shashankshekhar.mqttservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by shashankshekhar on 01/03/16.
 */
public class NetworkConnectivityChangeReceiver extends BroadcastReceiver {
    static ODMqtt odMqttObj;
    public static void initMqttObj (ODMqtt odMqtt) {
        odMqttObj = odMqtt;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (odMqttObj.isMqttConnected() == true) {
            Log.i("Open-Day", "client already connected ...returning from br");
            return;
        }
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected() || mobile.isConnected()) {
            Log.i("Open-Day", "in br receiver. sending reconnect call");
            odMqttObj.connectToMqttBroker();
        }
    }
}
