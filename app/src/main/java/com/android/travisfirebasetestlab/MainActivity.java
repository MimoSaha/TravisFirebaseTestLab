package com.android.travisfirebasetestlab;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.travisfirebasetestlab.R;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private Button start;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        status = findViewById(R.id.status);

        boolean permission = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
        } /*else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTION_MANAGE_WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }*/

        if (!permission) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 101);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS},101);
            }
        }

        Log.v("MIMO_SAHA:", "Permission: " + permission);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                hotspotConfigure();
            }
        });
    }

    private void hotspotConfigure() {
        wifiManager.setWifiEnabled(false);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        String networkName = "hotspot_w3";

        wifiConfiguration.SSID = networkName;

        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        wifiConfiguration.preSharedKey = "hotspotw3mimo";

        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setWifiApMethod.invoke(wifiManager, wifiConfiguration, true);

            Method stateMethod = wifiManager.getClass().getMethod("getWifiApState");
            stateMethod.setAccessible(true);

            boolean startPoint = true;

            while (startPoint) {
                if ((Integer) stateMethod.invoke(wifiManager, (Object[]) null) == 13) {
                    startPoint = false;
                    status.setText("success");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
