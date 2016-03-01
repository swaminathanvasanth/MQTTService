package com.example.shashankshekhar.mqttservice;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class PhoneSignalStrengthReaderService extends Service implements LocationListener {


    public static int mcc = 0, mnc = 0;
    public static TelephonyManager tel;
    public static String networkOperator;
    public static String simOperator;
    public static String simOperatorName;
    public static String NetworkOperatorName;
    //String AllCellInfo = tel.getAllCellInfo();
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

    public static BufferedWriter bw = null;


    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    Context context;
    ODMqtt odMqtt;
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

        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);/*
        MyListener = new MyPhoneStateListener();
        tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);*/

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        odMqtt = new ODMqtt(getApplicationContext(),randomString());
        boolean isConnected =  odMqtt.connectToMqttBroker();
        odMqtt.setBroadcastReceiver();

        new Thread() {
            public void run() {
                while (true) {
                    try {

                        if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                            cellinfogsm = (CellInfoGsm) tel.getAllCellInfo().get(0);
                            cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthGsm.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_EDGE";
                            Log.e("cellinfogsm EDGE", cellinfogsm.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS) {
                            cellinfogsm = (CellInfoGsm) tel.getAllCellInfo().get(0);
                            cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthGsm.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_GPRS";
                            Log.e("cellinfogsm GPRS", cellinfogsm.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA) {
                            cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                            cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthWcdma.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_HSPA";
                            Log.e("cellInfoWcdma HSPA", cellInfoWcdma.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
                            cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                            cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthWcdma.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_HSDPA";
                            Log.e("cellInfoWcdma HSDPA", cellInfoWcdma.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                            cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                            cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthWcdma.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_HSPAP";
                            Log.e("cellInfoWcdma HSPAP", cellInfoWcdma.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA) {
                            cellInfoWcdma = (CellInfoWcdma) tel.getAllCellInfo().get(0);
                            cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthWcdma.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_HSUPA";
                            Log.e("cellInfoWcdma HSUPA", cellInfoWcdma.toString() + " : " + RSSI);
                            collectData();
                        } else if (tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                            cellInfoLte = (CellInfoLte) tel.getAllCellInfo().get(0);
                            cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            RSSI = "" + cellSignalStrengthLte.getDbm();
                            NetworkConnectionType = "NETWORK_TYPE_LTE";
                            Log.e("cellInfoLTE LTE", cellInfoLte.toString() + " : " + RSSI);
                            collectData();
                        }
                        Log.e("Thread Running", "Thread Running");
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (Exception es) {
                        es.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private void collectData() {

        getSignalStrength();
        writeToFile(data);
        odMqtt.publishMessge(data);

    }

    private String randomString() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    public void getSignalStrength() {

        networkOperator = tel.getNetworkOperator();
        simOperator = tel.getSimOperator();
        simOperatorName = tel.getSimOperatorName();
        NetworkOperatorName = tel.getNetworkOperatorName();
        //String AllCellInfo = tel.getAllCellInfo();
        CallState = "" + tel.getCallState();
        CellLocation = "" + tel.getCellLocation();
        DeviceSoftwareVersion = "" + tel.getDeviceSoftwareVersion();
        DataActivity = "" + tel.getDataActivity();
        DeviceId = "" + tel.getDeviceId();
        DataState = "" + tel.getDataState();
        Line1Number = tel.getLine1Number();
        NetworkType = "" + tel.getNetworkType();
        // PhoneCount = ""+tel.getPhoneCount();
        PhoneType = "" + tel.getPhoneType();


        if (networkOperator != null) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));
        }


        Log.e("simOperator", simOperator);
        Log.e("simOperatorName", simOperatorName);
        Log.e("networkOperator", networkOperator);
        Log.e("NetworkOperatorName", NetworkOperatorName);
        Log.e("NetworkType", NetworkType);
        Log.e("NetworkConnectionType", NetworkConnectionType);
        Log.e("PhoneType", PhoneType);
        Log.e("CallState", CallState);
        Log.e("CellLocation", CellLocation);
        Log.e("DeviceSoftwareVersion", DeviceSoftwareVersion);
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


        currentTime = currentDate + "/" + currentMonth + "/" + currentYear_logging + "," + currentHour + ":" + currentMinutes;
        data = currentTime + "," + simOperatorName + "," + NetworkOperatorName + "," + NetworkConnectionType + "," + RSSI + "," + latitude + "," + longitude;

        Log.e("data", data);

    }

    private void writeToFile(String _data) {
        try {

            File mainfolder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + ("MobileSignalReader").trim());

            if (!mainfolder.exists()) {
                mainfolder.mkdir();
            }

            String root = Environment.getExternalStorageDirectory() +
                    File.separator + ("MobileSignalReader").trim();

            File file = new File(root, "MobileSignalReader.csv");


            bw = new BufferedWriter(new FileWriter(file, true));

            bw.write("ABCDEF123$%,"+_data);
            bw.newLine();
            bw.flush();
            bw.close();

            Log.e("writeToFile", "writeToFile");
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
        turnGPSOn();

    }

    private void turnGPSOn() {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        sendBroadcast(intent);
    }

}




