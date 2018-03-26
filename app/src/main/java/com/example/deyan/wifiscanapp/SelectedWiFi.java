package com.example.deyan.wifiscanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SelectedWiFi extends AppCompatActivity {

    TextView selectedWifi;
    Button gotInfo;
    TextView displayResults;
    String wifiName;
    String bssiName;
    int count = 0;
    ArrayList<List<ScanResult>> scanResultsList;
    double mean, temp, standardDeviation;

    WifiManager wifiManager;
    IntentFilter intentFilter;
    Intent intent;
    WiFiScanReceiver wifiReceiver;
    List<ScanResult> scanResults;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_wi_fi);

        selectedWifi = findViewById(R.id.selectedWifi);
        gotInfo = findViewById(R.id.got_wifi);
        displayResults = findViewById(R.id.results);

        intent = getIntent();
        Bundle extras = intent.getExtras();
        String SR = extras.getString("scanResults");
        String FT = extras.getString("FT");
        selectedWifi.setText(SR + "\n" + FT);

        wifiName = SR.split("\n")[0].split(": ")[1];
        bssiName = SR.split("\n")[1].split(": ")[1];

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiReceiver = new WiFiScanReceiver();


        gotInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDisplayScanResults();

            }
        });

    }

    public void getDisplayScanResults() {

        scanResultsList = new ArrayList<List<ScanResult>>();

        displayResults.append("RSSI:" + "\n");


        while (count < 5) {

            SystemClock.sleep(2000);
            onStart();
            wifiManager.startScan();
            scanResults = wifiManager.getScanResults();
            scanResultsList.add(scanResults);
            count += 1;
            onStop();

        }

        for (int i = 0; i < scanResultsList.size(); i++) {
            for (int j = 0; j < scanResultsList.get(i).size(); j++) {
                if (wifiName.equalsIgnoreCase(scanResultsList.get(i).get(j).SSID)) {
                    displayResults.append("Scan " + (i + 1) +":      " +
                            Integer.toString(scanResultsList.get(i).get(j).level) + " dBm" + "\n");
                    mean += scanResultsList.get(i).get(j).level;
                }
            }
        }

        mean = mean/5;

        for(int i = 0; i < scanResultsList.size(); i++){
            for(int j = 0; j < scanResultsList.get(i).size(); j++){
                if(wifiName.equalsIgnoreCase(scanResultsList.get(i).get(j).SSID)){
                    temp += Math.pow((scanResultsList.get(i).get(j).level - mean),2);
                }
            }
        }
        standardDeviation = Math.sqrt(temp/5);

        displayResults.append("\n" + "Mean: " + mean + " dBm" + "\n" + "Standard Deviation: " + String.format("%.5f",standardDeviation));


        try{
            File myFile = new File("/sdcard/ScanResults.txt");
            myFile.createNewFile();
            FileOutputStream file = new FileOutputStream(myFile);
            OutputStreamWriter myWriter = new OutputStreamWriter(file);
            myWriter.append("SSID: " + wifiName + "\n" + "BSSID: " + bssiName + "\n");
            myWriter.append(displayResults.getText());
            myWriter.close();
            file.close();
        }catch(IOException e){
            Log.e("Error","Could not create file" + e.toString() );
        }
    }


        @Override
        protected void onStop(){
        super.onStop();
        }

        @Override
        protected void onStart(){
        super.onStart();
        registerReceiver(wifiReceiver,intentFilter);
    }

        private class WiFiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context context,Intent intent) {
            scanResults = wifiManager.getScanResults();
        }
    }

    @Override
    public void onBackPressed(){
        unregisterReceiver(wifiReceiver);
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}





