/**
 * Copyright 2020 Marya Doery no warantees expressed or implied.
 */
package com.fullstackoasis.bluetoothdetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This simple app is a Bluetooth detector.
 * https://developer.android.com/guide/topics/connectivity/bluetooth
 * This app uses classic Bluetooth and also can scan for LE Bluetooth (BLE)
 * See https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.LeScanCallback
 * for BLE Bluetooth scans. BluetoothAdapter.LeScanCallback is DEPRECATED, DO NOT USE. Instead,
 * use ScanCallback.
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getCanonicalName();
    private boolean bluetoothNotSupported = false;
    // Nothing special about 312, random.
    private static int REQUEST_ENABLE_BT = 312;
    private static int LOCATION_PERMISSION_REQUEST_CODE = 347;
    private static int BLE_SCAN_MILLISECONDS = 12000;
    private boolean bluetoothForbidden = false;
    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;
    private BluetoothDiscoveredReceiver bluetoothDiscoveredReceiver;
    private HashMap<String, String> discoveredDevices = new HashMap<String, String>();
    private HashMap<String, BLEBlob> discoveredBLEDevices = new HashMap<String, BLEBlob>();
    private EverythingScanCallback everythingScanCallback;
    private Handler handler = new Handler() {};

    /**********************************************************************************************
     * Lifecycle methods
     **********************************************************************************************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothNotSupported) {
            return;
        }
        // Do not have to unregister these receivers because they are unregistered in onPause,
        // which should always have been called before onDestroy
        // unregisterReceiver(bluetoothStateChangeReceiver);
        // unregisterReceiver(bluetoothDiscoveredReceiver);
        // cancel discovery, as well
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        everythingScanCallback = new EverythingScanCallback(this);
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(false);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLinearLayout();
                MainActivity.this.showPairedDevicesAsTextViews();
            }
        });
        Button b2 = findViewById(R.id.btnViewDiscoverDevices);
        b2.setEnabled(false);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear out any TextViews listed.
                discoveredBLEDevices.clear();
                discoveredDevices.clear();
                clearLinearLayout();
                // showDiscoveredDevicesAsTextViews(discoveredDevices);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                // Discover. If any found, they'll be shown in the list.
                bluetoothAdapter.startDiscovery();
            }
        });
        Button b3 = findViewById(R.id.btnDiscoverBLEDevices);
        b3.setEnabled(false);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear out any TextViews listed.
                discoveredBLEDevices.clear();
                discoveredDevices.clear();
                clearLinearLayout();
                // showDiscoveredBLEDevicesAsTextViews(discoveredBLEDevices);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                // Discover. If any found, they'll be shown in the list.
                if (permissionHandling()) {
                    Log.d(TAG, "Going to scan");
                    scanner.startScan(everythingScanCallback);
                    final Runnable r = new Runnable() {
                        public void run() {
                            Log.d(TAG, "Stopped scan");
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                            scanner.stopScan(everythingScanCallback);
                        }
                    };
                    handler.postDelayed(r, BLE_SCAN_MILLISECONDS);
                }
            }
        });
        bluetoothStateChangeReceiver = new BluetoothStateChangeReceiver(this);
        bluetoothDiscoveredReceiver = new BluetoothDiscoveredReceiver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothNotSupported) {
            Log.d(TAG, "onResume, Bluetooth not supported");
            return;
        }
        Log.d(TAG, "onResume Bluetooth IS supported");
        // Maybe Bluetooth is enabled/disabled currently, but user could change this.
        // So register to receive this change.
        registerBluetoothBroadcastReceiver();
        if (bluetoothForbidden) {
            handleBluetoothDisallowed();
            return;
        }
        getBluetoothAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothNotSupported) {
            return;
        }
        unregisterReceiver(bluetoothStateChangeReceiver);
        unregisterReceiver(bluetoothDiscoveredReceiver);
    }

    private void registerBluetoothBroadcastReceiver() {
        IntentFilter bluetoothChangedFilter =
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateChangeReceiver, bluetoothChangedFilter);
        Log.d(TAG, "reg with action found ");
        IntentFilter bluetoothDiscoveredFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDiscoveredReceiver, bluetoothDiscoveredFilter);
    }

    /**********************************************************************************************
     * Bluetooth handling methods
     **********************************************************************************************/

    /**
     * Check to see if Bluetooth is supported.
     */
    private void getBluetoothAdapter() {
        Log.d(TAG, "getBluetoothAdapter");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            handleBluetoothNotSupported();
            return;
        }
        bluetoothNotSupported = false;
        handleBluetoothIsSupported();
        enableBluetooth(bluetoothAdapter);
    }

    /**
     * Handle Bluetooth not being supported by showing appropriate message to user.
     */
    private void handleBluetoothNotSupported() {
        TextView tv = findViewById(R.id.bluetoothStatus);
        tv.setText(R.string.bluetooth_unavailable);
        enableButtons(false);
    }

    /**
     * Handle Bluetooth being supported by showing appropriate message to user.
     */
    private void handleBluetoothIsSupported() {
        TextView tv = findViewById(R.id.bluetoothStatus);
        tv.setText(R.string.bluetooth_available);
        enableButtons(true);
    }

    /**
     * Handle Bluetooth being disallowed by showing appropriate message to user.
     */
    private void handleBluetoothDisallowed() {
        TextView tv = findViewById(R.id.bluetoothStatus);
        tv.setText(R.string.bluetooth_disallowed);
        enableButtons(false);
    }

    /**
     * If Bluetooth is disabled, pop up a system window that requests it be enabled.
     * The app is not stopped.
     * See https://developer.android.com/guide/topics/connectivity/bluetooth#SettingUp
     * @param bluetoothAdapter used to check whether Bluetooth is enabled
     */
    private void enableBluetooth(BluetoothAdapter bluetoothAdapter) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Called when user responds to request to enable Bluetooth.
     * If resultCode was 0 (RESULT_CANCELED), then the user did not permit Bluetooth to be enabled.
     * If it was -1 (RESULT_OK), then the user permitted Bluetooth to be enabled.
     * @param requestCode expected to be 312
     * @param resultCode may be RESULT_OK or RESULT_CANCELED
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "The requestCode was " + requestCode);
        Log.d(TAG, "The resultCode was " + resultCode);
        Log.d(TAG, "The RESULT_OK was " + RESULT_OK);
        Log.d(TAG, "The RESULT_CANCELED was " + RESULT_CANCELED);
        // By setting this value, the app will not keep asking for permission to enable Bluetooth.
        bluetoothForbidden = resultCode == RESULT_CANCELED;
        enableButtons(bluetoothForbidden);
    }

    private void enableButtons(boolean bluetoothForbidden) {
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(bluetoothForbidden);
        Button b2 = findViewById(R.id.btnViewDiscoverDevices);
        b2.setEnabled(bluetoothForbidden);
        Button b3 = findViewById(R.id.btnDiscoverBLEDevices);
        b3.setEnabled(bluetoothForbidden);
    }

    protected void handleBluetoothChanged(int state) {
        if (state == BluetoothAdapter.STATE_OFF) {
            handleBluetoothDisallowed();
        } else {
            // Must be ON.
            handleBluetoothIsSupported();
        }
    }

    /**
     * Loop over paired devices.
     * https://developer.android.com/guide/topics/connectivity/bluetooth#QueryPairedDevices
     */
    private void showPairedDevicesAsTextViews() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        LinearLayout lLayout = findViewById(R.id.lLayout);
        // empty the LinearLayout, so we don't keep adding TextViews to it.
        /*
        for (int i = 0; i < lLayout.getChildCount(); i++){
            View v = lLayout.getChildAt(i);
            if (v instanceof TextView){
                lLayout.removeView(v);
            }
        }*/
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                TextView tv = new TextView(this);
                tv.setText(deviceName);
                lLayout.addView(tv);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText(R.string.tv_no_devices_found);
            lLayout.addView(tv);
        }
    }

    private void clearLinearLayout() {
        LinearLayout lLayout = findViewById(R.id.lLayout);
        lLayout.removeAllViews();
    }

    /**
     * Shows the "discovered" class Bluetooth devices in the scrollable list.
     * @param devices
     */
    private void showDiscoveredDevicesAsTextViews(Map<String, String> devices) {
        LinearLayout lLayout = findViewById(R.id.lLayout);
        // Note: layout should have been emptied already.
        if (devices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (String deviceHardwareAddress : devices.keySet()) {
                String deviceName = devices.get(deviceHardwareAddress);
                String str = getFormattedDiscoveryText(deviceName, deviceHardwareAddress);
                TextView tv = new TextView(this);
                tv.setText(str);
                lLayout.addView(tv);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText(R.string.tv_no_devices_discovered);
            lLayout.addView(tv);
        }
    }

    /**
     * Shows the "discovered" class Bluetooth devices in the scrollable list.
     * @param devices
     */
    private void showDiscoveredBLEDevicesAsTextViews(Map<String, BLEBlob> devices) {
        LinearLayout lLayout = findViewById(R.id.lLayout);
        // Note: layout should have been emptied already.
        if (devices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (String deviceHardwareAddress : devices.keySet()) {
                BLEBlob bleBlob = devices.get(deviceHardwareAddress);
                String deviceName = devices.get(deviceHardwareAddress).getDevice().getName();
                String str = getFormattedDiscoveryText(deviceName, deviceHardwareAddress);
                TextView tv = new TextView(this);
                tv.setText(str);
                lLayout.addView(tv);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText(R.string.tv_no_devices_discovered);
            lLayout.addView(tv);
        }
    }

    protected void handleBluetoothDiscovered(String deviceName, String deviceHardwareAddress) {
        discoveredDevices.put(deviceHardwareAddress, deviceName);
        showDiscoveredDevicesAsTextViews(discoveredDevices);
    }

    protected void handleBluetoothBLEDiscovered(String deviceHardwareAddress, BLEBlob blob) {
        if (discoveredBLEDevices.get(deviceHardwareAddress) == null) {
            discoveredBLEDevices.put(deviceHardwareAddress, blob);
            Log.d(TAG, "SHOWING: handleBluetoothBLEDiscovered " + deviceHardwareAddress);
            clearLinearLayout(); // Clear it out. discoveredBLEDevices caches the discovered devices
            showDiscoveredBLEDevicesAsTextViews(discoveredBLEDevices);
        } else {
            Log.d(TAG,
                    "handleBluetoothBLEDiscovered skipped already found " + deviceHardwareAddress);
        }
    }

    protected String getFormattedDiscoveryText(String deviceName, String deviceHardwareAddress) {
        return "< " + deviceHardwareAddress + " | " + deviceName + " >";
    }

    private boolean permissionHandling() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            Log.d(TAG, "going to return false");
            return false;
        }

    }

}