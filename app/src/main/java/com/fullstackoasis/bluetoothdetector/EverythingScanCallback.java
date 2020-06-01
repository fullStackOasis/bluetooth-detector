/**
 * Copyright 2020 Marya Doery no warantees expressed or implied.
 */
package com.fullstackoasis.bluetoothdetector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

/**
 * Used to do BLE Bluetooth scanning. I haven't tested the onScanFailed or onBatchScanResults
 * methods at all, since they were not being fired.
 */
public class EverythingScanCallback extends ScanCallback {
    private static String TAG = EverythingScanCallback.class.getCanonicalName();
    MainActivity mainActivity;
    public EverythingScanCallback(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void onBatchScanResults (List<ScanResult> results) {
        super.onBatchScanResults(results);
        Log.d(TAG, "onBatchScanResults");
    }

    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        BluetoothDevice device = result.getDevice();
        String name = device.getName();
        String hardwareAddress = device.getAddress();
        Log.d(TAG, "onScanResult callbackType was " + callbackType + " name = " + name + " " +
                "hardwareAddress = " + hardwareAddress);
        BLEBlob bleBlob = new BLEBlob(device,
                result.getRssi(), result.getScanRecord());
        mainActivity.handleBluetoothBLEDiscovered(hardwareAddress, bleBlob);
    }

    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.d(TAG, "onScanFailed, errorCode " + errorCode);
    }
}