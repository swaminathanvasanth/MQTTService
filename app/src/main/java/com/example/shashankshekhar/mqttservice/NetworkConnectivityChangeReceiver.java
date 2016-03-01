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
    ODMqtt odMqttObj;


//    NetworkConnectivityChangeReceiver(ODMqtt odMqtt) {
//        odMqttObj = odMqtt;
//    }
    public void initMqttObj (ODMqtt odMqtt) {
        this.odMqttObj = odMqtt;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (odMqttObj.isMqttConnected() == true) {
            Log.i("Open_Day", "client already connected ...returning from br");
            return;
        }
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected() || mobile.isConnected()) {
            Log.i("Open_Day", "in br receiver. sending reconnect call");
            odMqttObj.connectToMqttBroker();
        }
    }
}
