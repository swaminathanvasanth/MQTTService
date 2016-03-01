package com.example.shashankshekhar.mqttservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

/**
 * Created by shashankshekhar on 29/02/16.
 */
public class ODMqtt implements MqttCallback {
    private final int QoS = 1;
    private final String BROKER_ADDRESS = "tcp://smartx.cds.iisc.ac.in:1883";
    private final String USERNAME = "admin";
    private final String PASSWORD = "password";
    private final String TAG = "Open-Day";
    private final String TOPIC_NAME = "iisc/smartx/crowd/network/ODRSSI";
    private final String TEST_TOPIC_NAME = "iisc/smartx/mobile/water/data";
    /*
    sample data format
    UID,TimeStamp,Latitude,Longitude,OperatorName,AreaCode,NetworkName,GSMSignalStrength
     */
    //smartx.cloudapp.net
    //smartx.cds.iisc.ac.in

    private MqttConnectOptions connectOptions = null;
    private MqttAsyncClient mqttClient = null;
    private String clientId;
    private Context applicationContext;

    public ODMqtt(Context appContext, String clientId) {
        this.applicationContext = appContext;
        this.clientId = clientId;
        NetworkConnectivityChangeReceiver obj = new NetworkConnectivityChangeReceiver();
        obj.initMqttObj(this);
    }

    private void setConnectionOptions() {
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());
        connectOptions.setConnectionTimeout(20);
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttAsyncClient(BROKER_ADDRESS, clientId, persistence);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception, could not instantiate mqttclient");
        }
    }

    public void connectToMqttBroker() {
        setConnectionOptions();
        mqttClient.setCallback(this);
        IMqttToken token;
        try {
            token = mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception, could not connect to mqtt");
        }
    }

    public void disconnectMqtt() {
        if (mqttClient.isConnected() == false) {
            return;
        }
        try {
            mqttClient.disconnect();
            printLog("successfully disconnected");
        } catch (MqttException e) {
            printLog("Error.Could not disconnect mqtt. Trying force");
            try {
                mqttClient.disconnectForcibly();
                printLog("disconnection successful with force");
            } catch (MqttException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void publishMessge(String dataString) {
        IMqttDeliveryToken deliveryToken;
        MqttMessage message1 = new MqttMessage(dataString.getBytes());
        message1.setQos(QoS);
        if (mqttClient.isConnected() == false) {
            printLog("error in publishing,mqtt is not connected");
            return;
        }
        try {
            deliveryToken = mqttClient.publish(TOPIC_NAME, message1);
        } catch (MqttException e) {
            e.printStackTrace();
            printLog("exception in publishing");
        }
    }

    public boolean isMqttConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public void connectionLost(Throwable cause) {
        printLog("connection lost in receiver!!");
        printLog("cause: " + cause.getCause());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken tk) {
        printLog("delivery complete for token: " + tk);
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        printLog("message arrived");
    }

    private void printLog(String string) {
        Log.i(TAG, string);
    }
}
