package com.fullstackoasis.bluetoothdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This simple app is a Bluetooth detector.
 * https://developer.android.com/guide/topics/connectivity/bluetooth
 * This app will use classic Bluetooth.
 */
public class MainActivity extends AppCompatActivity {
    private boolean bluetoothNotSupported = true;

    /**********************************************************************************************
     * Lifecycle methods
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBluetoothAdapter();
    }

    /**********************************************************************************************
     * Bluetooth handling methods
     **********************************************************************************************/

    /**
     * Check to see if Bluetooth is supported.
     */
    private void getBluetoothAdapter() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            handleBluetoothNotSupported();
            return;
        }
        bluetoothNotSupported = false;
        handleBluetoothIsSupported();
    }

    /**
     * Handle Bluetooth not being supported by showing appropriate message to user.
     */
    private void handleBluetoothNotSupported() {
        TextView tv = findViewById(R.id.bluetooth_status);
        tv.setText(R.string.bluetooth_unavailable);
    }

    /**
     * Handle Bluetooth being supported by showing appropriate message to user.
     */
    private void handleBluetoothIsSupported() {
        TextView tv = findViewById(R.id.bluetooth_status);
        tv.setText(R.string.bluetooth_available);
    }

}
