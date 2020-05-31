package com.fullstackoasis.bluetoothdetector;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * If a user enables or disables Bluetooth, let MainActivity know about it.
 */
public class BluetoothStateChangeReceiver extends BroadcastReceiver {
    private static String TAG = BluetoothStateChangeReceiver.class.getCanonicalName();
    private MainActivity activity;

    BluetoothStateChangeReceiver(MainActivity activity) {
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            /* Check for Bluetooth state being changed */
            int state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
            int prevState = extras.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
            /**
             * Potential states are:
             * BluetoothAdapter.STATE_CONNECTED 2
             * BluetoothAdapter.STATE_CONNECTING 1
             * BluetoothAdapter.STATE_DISCONNECTED 0
             * BluetoothAdapter.STATE_DISCONNECTING 3
             * BluetoothAdapter.STATE_OFF 10
             * BluetoothAdapter.STATE_ON 12
             * But, prevState can be 11! I don't know what that means
             */
            /*
            Log.d(TAG, "state " + state);
            Log.d(TAG, "prevState " + prevState);
            Log.d(TAG, "state connected " + BluetoothAdapter.STATE_CONNECTED);
            Log.d(TAG, "state connecting " + BluetoothAdapter.STATE_CONNECTING);
            Log.d(TAG, "state STATE_DISCONNECTED " + BluetoothAdapter.STATE_DISCONNECTED);
            Log.d(TAG, "state STATE_DISCONNECTING " + BluetoothAdapter.STATE_DISCONNECTING);
            Log.d(TAG, "state STATE_OFF " + BluetoothAdapter.STATE_OFF);
            Log.d(TAG, "state on " + BluetoothAdapter.STATE_ON);
             */
            if (state == BluetoothAdapter.STATE_OFF && prevState != BluetoothAdapter.STATE_OFF) {
                // Do not know if state can go from STATE_OFF to STATE_OFF...
                notifyActivity(state);
            } else if (state == BluetoothAdapter.STATE_ON && prevState != BluetoothAdapter.STATE_ON) {
                // Do not know if state can go from STATE_ON to STATE_ON...
                notifyActivity(state);
            }
            // Otherwise, nothing to do.
        }
    }

    public void notifyActivity(int state) {
        Log.d(TAG, "notify " + state);
        activity.handleBluetoothChanged(state);
    }

}
