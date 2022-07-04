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
        intentFilter.addAction("com.neusoft.c3alfus.commservice.action.REVERSE_ON");
        intentFilter.addAction("com.vrmms.intent.CARCONTROL");
        intentFilter.addAction("com.neusoft.ACTION_WARNING_PAGE_HAS_DISMISS");
        intentFilter.addAction("com.neusoft.avm.press");
        registerReceiver(_broadcastReceiver, intentFilter);

        IntentFilter intentFilterUsb = new IntentFilter();
        intentFilterUsb.addAction("bt.ecarxtestn.com.btphoneapp.phonemissed");
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
        printTime();
//        printWifi();
//        printBt();
//        checkPath();
        TextView myTextView = (TextView) mTopView.findViewById(R.id.time);

        final Intent intent = new Intent();
        intent.setAction("com.vrmms.intent.CARCONTROL");
        intent.putExtra("operate","window_open");
        intent.putExtra("car-window",1);

        final Intent intent2 = new Intent();
        intent2.setAction("com.neusoft.c3alfus.commservice.action.REVERSE_ON");


        Intent intent9 = new Intent("android.intent.action.VIEW");
        intent9.setClassName( "com.neusoft.cardvr", "com.neusoft.cardvr.view.MainActivity");
        intent9.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent9);
//        public void toSettings(View view) {
//            Intent intent = new Intent();
//            intent.setClassName("com.android.settings", "com.android.settings.Settings");
//            startActivity(intent);
//        }
//
      final Intent intent3 = new Intent();
        intent3.setAction("com.neusoft.avm.press");

      final Intent intent4 = new Intent();
        intent4.setAction("com.neusoft.ACTION_CANDIAGNOSIS");
        intent4.putExtra("command",13);

  final Intent intent5 = new Intent();
        intent5.setAction("com.neusoft.ACTION_SHIELD_HARDKEY");
        intent5.putExtra("require",0);


        myTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendBroadcast(intent);
                sendBroadcast(intent2);
                sendBroadcast(intent3);
                sendBroadcast(intent4);
                sendBroadcast(intent5);
                return false;
            }
        });
    }

    public void printTime() {
        TextView myTextView = (TextView) mTopView.findViewById(R.id.time);
        myTextView.setText("12345");
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
