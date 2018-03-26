package com.example.deyan.wifiscanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    // Elements definition
    Button scanButton;
    ListView displayWifi;
    EditText keywords;
    ArrayList<String> results;
    String passData;
    String FT;

    // Wifi elements
    WifiManager wifiManager;
    IntentFilter intentFilter;
    WiFiScanReceiver wifiReceiver;
    List<ScanResult> scanResults;
    ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        displayWifi =  findViewById(R.id.display_wifi);
        keywords =  findViewById(R.id.keyword);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        intentFilter  = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiReceiver = new WiFiScanReceiver();


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                results = new ArrayList<String>();
                results.add("Scanning...");
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,results);
                displayWifi.setAdapter(adapter);
                //scan wifi hotspot
                wifiManager.startScan();

            }
        });

        displayWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent getInfo = new Intent(MainActivity.this, SelectedWiFi.class);

                Bundle extras = new Bundle();
                passData = (String) displayWifi.getItemAtPosition(position);
                extras.putString("scanResults", passData);
                extras.putString("FT",FT);
                getInfo.putExtras(extras);
                startActivityForResult(getInfo,1);
            }
        });
    }


    private class WiFiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            scanResults = wifiManager.getScanResults();
            results.clear();

            if(!keywords.getText().toString().isEmpty()) {

                for (int i = 0; i < scanResults.size(); i++) {

                    String SSID = scanResults.get(i).SSID;
                    String BSSID = scanResults.get(i).BSSID;
                    String RSSI = Integer.toString(scanResults.get(i).level);
                    String frequency = Integer.toString(scanResults.get(i).frequency);
                    String timestamp = Long.toString(scanResults.get(i).timestamp);

                    if (SSID.toLowerCase().contains(keywords.getText().toString().toLowerCase())) {

                        results.add("SSID: " + SSID + "\n" + "BSSID: " + BSSID + "\n" + "RSSI: " + RSSI);
                        FT = "Frequency: " + frequency + "\n" + "Timestamp: " + timestamp;

                    }

                }
            }

            else {

                results.clear();
                for (int i = 0; i < scanResults.size(); i++) {

                    String SSID = scanResults.get(i).SSID;
                    String BSSID = scanResults.get(i).BSSID;
                    String RSSI = Integer.toString(scanResults.get(i).level);
                    String frequency = Integer.toString(scanResults.get(i).frequency);
                    String timestamp = Long.toString(scanResults.get(i).timestamp);

                    results.add("SSID: " + SSID + "\n" + "BSSID: " + BSSID + "\n" + "RSSI: " + RSSI);
                    FT = "Frequency: " + frequency + "\n" + "Timestamp: " + timestamp;
                }
            }

            adapter.notifyDataSetChanged();
            wifiManager.startScan();
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onStart(){
        super.onStart();
        registerReceiver(wifiReceiver,intentFilter);
    }

}





