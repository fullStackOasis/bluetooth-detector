/**
 * Copyright 2020 Marya Doery no warantees expressed or implied.
 */
package com.fullstackoasis.bluetoothdetector;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Handles Bluetooth discovery
 */
public class BluetoothDiscoveredReceiver extends BroadcastReceiver {
    private static String TAG = BluetoothDiscoveredReceiver.class.getCanonicalName();
    private MainActivity activity;
    BluetoothDiscoveredReceiver(MainActivity activity) {
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive ");
        String action = intent.getAction();
        Log.d(TAG, "onReceive " + action);
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            notifyActivity(deviceName, deviceHardwareAddress);
        }
    }

    public void notifyActivity(String deviceName, String deviceHardwareAddress) {
        activity.handleBluetoothDiscovered(deviceName, deviceHardwareAddress);
    }
}
