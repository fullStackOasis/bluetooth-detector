/**
 * Copyright 2020 Marya Doery no warantees expressed or implied.
 */
package com.fullstackoasis.bluetoothdetector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

// Not yet used, but may be eventually.
public class BLEBlob {
    private BluetoothDevice device;
    private int rssi;
    private ScanRecord scanRecord;
    protected BLEBlob(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public ScanRecord getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(ScanRecord scanRecord) {
        this.scanRecord = scanRecord;
    }
}
