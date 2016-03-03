package com.example.shashankshekhar.mqttservice;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by shashankshekhar on 29/02/16.
 */
public class ODMqtt implements MqttCallback {
    private final int QoS = 1;
    private final String BROKER_ADDRESS = "tcp://smartx.cds.iisc.ac.in:1883";
    private final String USERNAME = "ODAppUser";
    private final String PASSWORD = "ODApp@Smartx";
    private final String TAG = "Open-Day";
    private final String TOPIC_NAME = "iisc/smartx/crowd/network/ODRSSI";
    private final String TEST_TOPIC_NAME = "iisc/smartx/mobile/water/data";

    // connection constants

    //DC - DELIVERY complete
    private final String DELIVERY_COMPLETE = "DC";
    //CL - connection lost
    private final String CONNECTION_LOST = "CL";
    //NoPub - could not publish - not connected to mqtt
    private final String NO_PUB = "NoPub";
    // failed when trying to reconnec to mqtt with Non security expection
    private final String CONNECTION_EXP = "ConnectionExp";
    // failed when trying to reconnec to mqtt with security expection
    private final String SECURITY_EXP= "SecurityExp";
    // trying to reconnect
    private final String RECONNECTING = "reconnecting";


    private String mqttStatus;
    private MqttConnectOptions connectOptions = null;
    private MqttAsyncClient mqttClient = null;

    private String clientId;
    private Context applicationContext;
    public ODMqtt(Context appContext, String clientId) {
        this.applicationContext = appContext;
        this.clientId = clientId;
        NetworkConnectivityChangeReceiver.initMqttObj(this);
    }

    private void setConnectionOptions() {
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(PASSWORD.toCharArray());
        // new addition
        connectOptions.setKeepAliveInterval(300);
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
            mqttStatus = RECONNECTING;
            token = mqttClient.connect(connectOptions);
        }
        catch (MqttSecurityException e) {
            if (e.getCause()!= null) {
                mqttStatus = SECURITY_EXP + "-"+e.getCause();
            } else
                mqttStatus = SECURITY_EXP;
        }
        catch (MqttException e) {
            if (e.getCause()!= null) {
                mqttStatus = CONNECTION_EXP + "-"+e.getCause();
            } else

                mqttStatus = CONNECTION_EXP;

        }
    }
    public String getMqttStatus() {
        return mqttStatus;
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
        if (dataString == null || dataString.isEmpty()) {
            return;
        }
        IMqttDeliveryToken deliveryToken;
        MqttMessage message1 = new MqttMessage(dataString.getBytes());
        message1.setQos(QoS);
        if (mqttClient.isConnected() == false) {
            printLog("error in publishing,mqtt is not connected");
            mqttStatus = NO_PUB;
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
        if (cause!= null && cause.getCause()!= null) {
            mqttStatus = CONNECTION_LOST + "-" + cause.getCause();
        } else {
            mqttStatus = CONNECTION_LOST;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken tk) {
        printLog("delivery complete for token: " + tk);
        mqttStatus = DELIVERY_COMPLETE;
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        printLog("message arrived");
    }

    private void printLog(String string) {
        Log.i(TAG, string);
    }
}
