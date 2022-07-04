package ru.lugadin.statusbar2;

import android.app.Service;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbManager;
import android.location.GpsStatus;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
//import com.neusoft.c3alfus.passthroughservice.PassthroughServiceApi;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OverlayShowingService extends Service {

    WindowManager wm;
    ViewGroup mTopView;
    WifiManager mWifiManager;
    //    BluetoothManager bluetoothManager;
    BroadcastReceiver _broadcastReceiver;
    BroadcastReceiver _broadcastReceiverUsb;
    StorageManager storageManager;


//    Calendar c = Calendar.getInstance();
//    c.set(2013, 8, 15, 12, 34, 56);
//    AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//    am.setTime(c.getTimeInMillis());


//    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("dd.MM.yyyy  HH:mm");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void _init() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        storageManager = (StorageManager) getApplicationContext().getSystemService(Context.STORAGE_SERVICE);


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }


    private void _broadcastReceiverInit() {
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {

                switch (intent.getAction()) {
                    case Intent.ACTION_TIME_TICK:
                        printTime();
                        break;
                    case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    case WifiManager.RSSI_CHANGED_ACTION:
                    case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                        printWifi();
                        break;
                    case "com.neusoft.statusbar.panel.expand":
                        mTopView.findViewById(R.id.views).setVisibility(View.GONE);
                        break;
                    case "com.neusoft.statusbar.panel.collapse":
                        mTopView.findViewById(R.id.views).setVisibility(View.VISIBLE);
                        break;
                    default:
                        String s = "";
                        for (String k : intent.getExtras().keySet()) {
                            s += k + ":" + intent.getExtras().get(k).toString();
                        }
                        Toast.makeText(getApplicationContext(), "DEF:" + s, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        _broadcastReceiverUsb = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                switch (intent.getAction()) {
                    case Intent.ACTION_MEDIA_MOUNTED:
                        processInsertAction(intent);
                        break;
                    case Intent.ACTION_MEDIA_EJECT:
                        processRemoveAction(intent);
                        break;
                    default:
                        String s = "";
                        for (String k : intent.getExtras().keySet()) {
                            s += k + ":" + intent.getExtras().get(k).toString();
                        }
                        Toast.makeText(getApplicationContext(), "DEF2:" + s, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction("com.neusoft.geely.bluetooth");
        intentFilter.addAction("com.neusoft.statusbar.panel.collapse");
        intentFilter.addAction("com.neusoft.statusbar.panel.expand");
        intentFilter.addAction("c3alfus.action");
        intentFilter.addAction("android.intent.action.POWER_OFF");
        intentFilter.addAction("android.intent.action.POWER_ON");
        intentFilter.addAction("ecarx.intent.action.bt.boot.completed");
        intentFilter.addAction("ecarx.intent.category.BT");
        intentFilter.addAction("ecarx.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("ecarx.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("ecarx.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("com.android.bluetooth.btservice.action.STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("*");
//        intentFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND);
//        intentFilter.addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        registerReceiver(_broadcastReceiver, intentFilter);

        IntentFilter intentFilterUsb = new IntentFilter();
        intentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilterUsb.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilterUsb.addAction(Intent.ACTION_MEDIA_EJECT);

        intentFilterUsb.addAction("bt.ecarxtestn.com.btphoneapp.phonemissed");
        intentFilterUsb.addAction("ecarx.intent.action.bt.boot.completed");
        intentFilterUsb.addAction("ecarx.intent.category.BT");
        intentFilterUsb.addAction("ecarx.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilterUsb.addAction("ecarx.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilterUsb.addAction("ecarx.a2dp.acquire");
        intentFilterUsb.addAction("CarControlReceiver");
        intentFilterUsb.addAction("SXVehicleConfigDataReceiver");
        intentFilterUsb.addAction("*");


        intentFilterUsb.addDataScheme("file");

        registerReceiver(_broadcastReceiverUsb, intentFilterUsb);
    }


    private boolean getStoragePath(String path) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            String line;
            BufferedReader br = new BufferedReader(inputStreamReader);
            while ((line = br.readLine()) != null) {
                if (line.contains(path)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void _createView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = (ViewGroup) inflater.inflate(R.layout.statusbar, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.CENTER;
        wm.addView(mTopView, params);
//        mTopView.findViewById(R.id.usb1).setVisibility(View.GONE);
//        mTopView.findViewById(R.id.usb2).setVisibility(View.GONE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _init();
        _broadcastReceiverInit();
        _createView();
//        printTime();
//        printWifi();
//        printBt();
//        checkPath();
        TextView myTextView = (TextView) mTopView.findViewById(R.id.time);

        final Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {startActivity(intent);
                return false;
            }
        });
    }

    public void printTime() {
        TextView myTextView = (TextView) mTopView.findViewById(R.id.time);
//        myTextView.setText(_sdfWatchTime.format(new Date()));
    }

    public void printBt() {

    }

    public void checkPath() {
        if (getStoragePath("udisk1")) {
//            mTopView.findViewById(R.id.usb1).setVisibility(View.VISIBLE);
        } else {
//            mTopView.findViewById(R.id.usb1).setVisibility(View.GONE);
        }

        if (getStoragePath("udisk2")) {
//            mTopView.findViewById(R.id.usb2).setVisibility(View.VISIBLE);
        } else {
//            mTopView.findViewById(R.id.usb2).setVisibility(View.GONE);
        }
    }

    private void processRemoveAction(Intent intent) {
//        String USBNumber = intent.getStringExtra("USBNumber");
//        if (USBNumber != null) {
//            if (USBNumber.equals("1")) {
//                mTopView.findViewById(R.id.usb1).setVisibility(View.GONE);
//            } else if (USBNumber.equals("2")) {
//                mTopView.findViewById(R.id.usb2).setVisibility(View.GONE);
//            }
//        }
    }

    private void processInsertAction(Intent intent) {
//        String USBNumber = intent.getStringExtra("USBNumber");
//        if (USBNumber == null) {
//            return;
//        }
//        if (USBNumber.equals("1")) {
//            mTopView.findViewById(R.id.usb1).setVisibility(View.VISIBLE);
//        } else if (USBNumber.equals("2")) {
//            mTopView.findViewById(R.id.usb2).setVisibility(View.VISIBLE);
//        }
    }

    public void printWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mTopView.findViewById(R.id.wifi).setBackground(null);
        } else {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            int iconWifi = R.drawable.wifi_off;
            if (wifiInfo.getNetworkId() != -1) {
                int level = mWifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                switch (level) {
                    case 0:
                        iconWifi = R.drawable.wifi_0;
                        break;
                    case 1:
                        iconWifi = R.drawable.wifi_1;
                        break;
                    case 2:
                        iconWifi = R.drawable.wifi_2;
                        break;
                    case 3:
                        iconWifi = R.drawable.wifi_3;
                        break;
                    case 4:
                        iconWifi = R.drawable.wifi_4;
                        break;
                }
            }
            mTopView.findViewById(R.id.wifi).setBackgroundResource(iconWifi);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_broadcastReceiver != null) {
            unregisterReceiver(_broadcastReceiver);
        }
    }
}
