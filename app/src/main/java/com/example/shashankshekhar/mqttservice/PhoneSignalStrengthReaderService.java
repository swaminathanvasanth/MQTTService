package com.example.shashankshekhar.mqttservice;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class PhoneSignalStrengthReaderService extends Service implements LocationListener {

    public String deviceName;
    public static int mcc = 0, mnc = 0;
    public static TelephonyManager tel;
    public static String networkOperator;
    public static String simOperator;
    public static String simOperatorName;
    public static String NetworkOperatorName;
    public static String RSSI;
    public static String CallState;
    public static String CellLocation;
    public static String DeviceSoftwareVersion;
    public static String DataActivity;
    public static String DeviceId;
    public static String DataState;
    public static String Line1Number;
    public static String NetworkType;
    public static String PhoneCount;
    public static String PhoneType;
    public static String NetworkConnectionType;
    public static CellInfoGsm cellinfogsm;
    public static CellInfoWcdma cellInfoWcdma;
    public static CellInfoLte cellInfoLte;
    public static CellSignalStrengthGsm cellSignalStrengthGsm;
    public static CellSignalStrengthWcdma cellSignalStrengthWcdma;
    public static CellSignalStrengthLte cellSignalStrengthLte;

    public static Calendar cal;
    public static byte currentSecond;
    public static byte currentHour;
    public static byte currentMinutes;
    public static byte currentDate;
    public static byte currentMonth;
    public static String data;

    public static int currentYear_logging;
    public String currentTime;
    public String currentTimefordataupload;
    public String previousTimefordataupload;

    public static BufferedWriter bw = null;

    // flag for GPS status
    public static boolean isGPSEnabled = false;

    // flag for network status
    public static boolean isNetworkEnabled = false;
    public static boolean isMQTTConnected = false;

    // flag for GPS status
    public static boolean canGetLocation = false;

    Location location; // location
    public static double latitude = 0; // latitude
    public static double longitude = 0; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between sampling in milliseconds
    private static final long MIN_TIME_BW_SAMPLING = 1000 * 30 * 1; // 30 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;
    public static int dataCount;
    public static int dataCountFile;

    Context context;
    ODMqtt odMqtt;
    public static SharedPreferences pref;
    public static SharedPreferences prefFile;
    public static SharedPreferences prefUID;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences.Editor editorFile;
    public static SharedPreferences.Editor editorUID;
    public static FileInputStream is;
    public static BufferedReader reader;
    public static File mainfolder;
    public static String root;
    public static File file;
    MyPhoneStateListener myPhoneStateListener;

    public static final int UNKNOW_CODE = 99;
    public static int MAX_SIGNAL_DBM_VALUE = 31;

    BufferedReader br;
    FileInputStream fs;

    public static String MqttStatus;

    public static int UID;

    public PhoneSignalStrengthReaderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        deviceName = getDeviceName();

        // Have a Persistent Storage (Shared Preference) to hold dataCount
        prefUID = context.getSharedPreferences("OpenDaySharedPreferenceUID", MODE_PRIVATE);
        editorUID = prefUID.edit();
        UID = prefUID.getInt("randomUID", 0);
        Log.e("randomUID", "randomUID is " + UID);

        if(UID == 0){
            UID = 1 + (int)(Math.random() * 100000);
            editorUID.putInt("randomUID", UID);
            editorUID.commit();
            Log.e("randomUID", "randomUID is " + UID);
        }

        // Have a Persistent Storage (Shared Preference) to hold dataCount
        pref = context.getSharedPreferences("OpenDaySharedPreference", MODE_PRIVATE);
        editor = pref.edit();
        dataCount = pref.getInt("dataCount", 0);
        Log.e("Data Count", "Value is " + dataCount);

        prefFile = context.getSharedPreferences("OpenDaySharedPreferenceFile", MODE_PRIVATE);
        editorFile = prefFile.edit();
        dataCountFile = prefFile.getInt("dataCountFile", 0);
        Log.e("Data Count File", "Value is " + dataCountFile);

        if (dataCountFile == 0) {
            dataCountFile = 1;
            editorFile.putInt("dataCountFile", dataCountFile);
            editorFile.commit();
            Log.e("Data Count File", "Value is " + dataCountFile);
        }

        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        myPhoneStateListener = new MyPhoneStateListener();
        tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tel.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                PhoneStateListener.LISTEN_CALL_STATE |
                PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                PhoneStateListener.LISTEN_SERVICE_STATE |
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, PhoneSignalStrengthReaderService.this);
        odMqtt = new ODMqtt(getApplicationContext(), randomString());
        odMqtt.connectToMqttBroker();

        new Thread() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            public void run() {
                while (true) {
                    try {

                        NetworkConnectionType = getNetworkTypeName(tel.getNetworkType());
                        Log.e("NetworkConnectionType", NetworkConnectionType);

                        if (tel.getAllCellInfo() != null && tel.getAllCellInfo().size() != 0) {

                            if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                                try {
                                    getNetworkTypeName(tel.getNetworkType());
                                    cellinfogsm = (CellInfoGsm) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthGsm.getDbm();
                                    Log.e("cellinfogsm EDGE", cellinfogsm.toString() + " : " + RSSI);

                                } catch (ClassCastException c) {

                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS) {
                                try {
                                    cellinfogsm = (CellInfoGsm) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthGsm.getDbm();
                                    Log.e("cellinfogsm GPRS", cellinfogsm.toString() + " : " + RSSI);

                                } catch (ClassCastException c) {

                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA) {
                                try {
                                    cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthWcdma.getDbm();
                                    Log.e("cellInfoWcdma HSPA", cellInfoWcdma.toString() + " : " + RSSI);
                                } catch (ClassCastException c) {

                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
                                try {
                                    cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthWcdma.getDbm();
                                    Log.e("cellInfoWcdma HSDPA", cellInfoWcdma.toString() + " : " + RSSI);
                                } catch (ClassCastException c) {

                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                                try {
                                    cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthWcdma.getDbm();
                                    Log.e("cellInfoWcdma HSPAP", cellInfoWcdma.toString() + " : " + RSSI);
                                } catch (ClassCastException c) {
                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA) {
                                try {
                                    cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthWcdma.getDbm();
                                    Log.e("cellInfoWcdma HSUPA", cellInfoWcdma.toString() + " : " + RSSI);
                                } catch (ClassCastException c) {

                                }
                            } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                                try {
                                    cellInfoLte = (CellInfoLte) tel.getAllCellInfo().get(0);
                                    cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                    RSSI = "" + cellSignalStrengthLte.getDbm();
                                    Log.e("cellInfoLTE LTE", cellInfoLte.toString() + " : " + RSSI);
                                } catch (ClassCastException c) {
                                }
                            }
                        }

                        Log.e("Thread Running", "Thread Running");

                        try {
                            Thread.sleep(MIN_TIME_BW_SAMPLING);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        getSignalStrength();
                        isNetworkEnabled = isNetworkAvailable();
                        isMQTTConnected = odMqtt.isMqttConnected();
                        collectData();


                    } catch (Exception es) {
                        es.printStackTrace();
                    }
                }
            }
        }.start();

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "2G_GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G_EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "2G_CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "2G_1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G_IDEN";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G_UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "3G_EVDO_O";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "3G_EVDO_A";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "3G_HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "3G_HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "3G_HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "3G_EVOD_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "3G_EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G_HSPAP";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G_LTE";
            default:
                return "Unknown";
        }
    }


    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);
            System.out.println("onCellInfoChanged" + cellInfo.toString());
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);
            Log.e("networkType", "" + networkType);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);


            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                Log.e("getGsmSignalStrength", "" + signalStrength.getGsmSignalStrength());
                RSSI = "" + ((2 * signalStrength.getGsmSignalStrength()) - 113);
                Log.e("GSM dbm", RSSI);
                getSignalStrength();
            }
        }
    }

    private void collectData() {

        writeToFile(data);
        sendData();
    }

    private void sendData() {
        // Check if the network is available
        // if(true) -> Push the data from queue

        if (isNetworkEnabled && isMQTTConnected) {
            Log.e("NetworkEnabled", "publishMessge");

            if (dataCountFile - dataCount > 0) {
                Log.e("DatacountFile>dataCount", "" + (dataCountFile - dataCount));
                Log.e("Start Line Read", "-- Send remaining data");

                // Read file and Loop from dataCount to dataCountFile
                //sendData

                try {
                    fs = new FileInputStream(file);
                    br = new BufferedReader(new InputStreamReader(fs));
                    for (int i = 0; i <= dataCountFile; i++) {
                        if (i == dataCount) {
                            data = br.readLine();
                            Log.e("data after line read", + i +"-" +dataCount + "-" + dataCountFile + "-" + data);
                            odMqtt.publishMessge(data);
                            dataCount += 1;
                            editor.putInt("dataCount", dataCount);
                            editor.commit();
                            Log.e("dataCount", "" + dataCount + "---" + data);
                            Thread.sleep(1000);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                odMqtt.publishMessge(data);
                Log.e("DatacountFile=dataCount", "" + (dataCountFile - dataCount));
                dataCount += 1;
                editor.putInt("dataCount", dataCount);
                editor.commit();
                Log.e("dataCount", "" + dataCount);
            }
            Log.e("dataCount", "" + dataCount);
        } else if(isNetworkEnabled){
            odMqtt.connectToMqttBroker();
        }
    }

    private boolean isNetworkAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private String randomString() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    public void getSignalStrength() {

        try {
            networkOperator = tel.getNetworkOperator();
            simOperator = tel.getSimOperator();
            simOperatorName = tel.getSimOperatorName();
            NetworkOperatorName = tel.getNetworkOperatorName();
            CallState = "" + tel.getCallState();
            CellLocation = "" + tel.getCellLocation();
            DeviceSoftwareVersion = "" + tel.getDeviceSoftwareVersion();
            DataActivity = "" + tel.getDataActivity();
            DeviceId = "" + tel.getDeviceId();
            DataState = "" + tel.getDataState();
            Line1Number = tel.getLine1Number();
            NetworkType = "" + tel.getNetworkType();
            PhoneType = "" + tel.getPhoneType();

            if (networkOperator != null) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                mnc = Integer.parseInt(networkOperator.substring(3));
            }
        } catch (Exception e){

        }

        Log.e("simOperator", "---" + simOperator);
        Log.e("simOperatorName", "---" + simOperatorName);
        Log.e("networkOperator", "---" + networkOperator);
        Log.e("NetworkOperatorName", "---" + NetworkOperatorName);
        Log.e("NetworkType", "---" + NetworkType);
        Log.e("NetworkConnectionType", "---" + NetworkConnectionType);
        Log.e("PhoneType", "---" + PhoneType);
        Log.e("CallState", "---" + CallState);
        Log.e("CellLocation", "---" + CellLocation);
        Log.e("DeviceSoftwareVersion", "---" + DeviceSoftwareVersion);
        Log.e("latitude", "" + latitude);
        Log.e("longitude", "" + longitude);

        System.out.println("\n --------------------------------");

        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        currentHour = (byte) cal.get(Calendar.HOUR_OF_DAY);
        // currentHour = (byte) (currentHour % 12);
        currentMinutes = (byte) cal.get(Calendar.MINUTE);
        currentDate = (byte) (cal.get(Calendar.DATE));
        currentMonth = (byte) (cal.get(Calendar.MONTH) + 1);
        currentYear_logging = cal.get(Calendar.YEAR);
        MqttStatus = odMqtt.getMqttStatus();
        Log.e("MqttStatus",MqttStatus);
        currentTime = currentDate + "/" + currentMonth + "/" + currentYear_logging + "," + currentHour + ":" + currentMinutes;
        data = deviceName+"@"+UID + "," + currentTime + "," + simOperatorName + "," + NetworkOperatorName + "," + NetworkConnectionType + "," + RSSI + "," + latitude + "," + longitude + "," + MqttStatus;
    }

    private void writeToFile(String _data) {
        try {
            mainfolder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + ("MobileSignalReader").trim());

            if (!mainfolder.exists()) {
                mainfolder.mkdir();
            }

            root = Environment.getExternalStorageDirectory() +
                    File.separator + ("MobileSignalReader").trim();
            file = new File(root, "MobileSignalReader.csv");
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(_data);
            bw.newLine();
            bw.flush();
            bw.close();

            Log.e("dataCountFile", dataCountFile + "," + _data);
            Log.e("writeToFile", "writeToFile");

            dataCountFile += 1;
            editorFile.putInt("dataCountFile", dataCountFile);
            editorFile.commit();
            Log.e("Data Count File ", "Value is " + dataCountFile);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "New Latitude: " + location.getLatitude()
                + "New Longitude: " + location.getLongitude();

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}




