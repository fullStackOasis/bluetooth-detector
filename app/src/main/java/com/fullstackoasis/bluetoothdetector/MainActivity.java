package com.fullstackoasis.bluetoothdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Set;

/**
 * This simple app is a Bluetooth detector.
 * https://developer.android.com/guide/topics/connectivity/bluetooth
 * This app will use classic Bluetooth.
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getCanonicalName();
    private boolean bluetoothNotSupported = false;
    // Nothing special about 312, random.
    private static int REQUEST_ENABLE_BT = 312;
    private boolean bluetoothForbidden = false;
    private BluetoothReceiver bluetoothReceiver;

    /**********************************************************************************************
     * Lifecycle methods
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(false);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.showPairedDevicesAsTextViews();
            }
        });
        bluetoothReceiver = new BluetoothReceiver(this);
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
        unregisterReceiver(bluetoothReceiver);
    }

    private void registerBluetoothBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private void unregisterBluetoothBroadcastReceiver() {
        unregisterReceiver(bluetoothReceiver);
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
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(false);
    }

    /**
     * Handle Bluetooth being supported by showing appropriate message to user.
     */
    private void handleBluetoothIsSupported() {
        TextView tv = findViewById(R.id.bluetoothStatus);
        tv.setText(R.string.bluetooth_available);
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(true);
    }

    /**
     * Handle Bluetooth being disallowed by showing appropriate message to user.
     */
    private void handleBluetoothDisallowed() {
        TextView tv = findViewById(R.id.bluetoothStatus);
        tv.setText(R.string.bluetooth_disallowed);
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(false);
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
        Button b = findViewById(R.id.btnViewPairedDevices);
        b.setEnabled(bluetoothForbidden);
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
        for (int i = 0; i < lLayout.getChildCount(); i++){
            View v = lLayout.getChildAt(i);
            if (v instanceof TextView){
                lLayout.removeView(v);
            }
        }
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
}
