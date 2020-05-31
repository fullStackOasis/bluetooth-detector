package com.fullstackoasis.bluetoothdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * This simple app is a Bluetooth detector.
 * https://developer.android.com/guide/topics/connectivity/bluetooth
 * This app will use classic Bluetooth.
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getCanonicalName();
    private boolean bluetoothNotSupported = true;
    private static int REQUEST_ENABLE_BT = 312;
    private boolean bluetoothForbidden = false;

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
        if (bluetoothForbidden) {
            handleBluetoothDisallowed();
            // User has forbidden bluetooth, so do not try to override this.
            return;
        }
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
        enableBluetooth(bluetoothAdapter);
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

    /**
     * Handle Bluetooth being disallowed by showing appropriate message to user.
     */
    private void handleBluetoothDisallowed() {
        TextView tv = findViewById(R.id.bluetooth_status);
        tv.setText(R.string.bluetooth_disallowed);
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
    }

}
