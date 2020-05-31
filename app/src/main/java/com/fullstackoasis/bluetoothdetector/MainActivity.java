package com.fullstackoasis.bluetoothdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * This simple app is a Bluetooth detector.
 * https://developer.android.com/guide/topics/connectivity/bluetooth
 * This app will use classic Bluetooth.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
