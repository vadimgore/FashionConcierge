package com.ndg.intel.fashionconcierge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.blesh.sdk.classes.Blesh;
import com.blesh.sdk.classes.BleshInstance;
import com.blesh.sdk.models.BleshTemplateResult;
import com.intel.heartratecore.api.HeartRateCore;
import com.intel.heartratecore.api.HeartRateCoreListener;
import com.intel.heartratecore.api.HeartRateCoreStatus;

import java.util.EnumSet;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    HeartRateCore hrCore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBluetooth();

        // Define a callback reference to be used by the Blesh service
        // in order to push the user's action results to your application
        BleshTemplateResult result = new BleshTemplateResult() {
            @Override
            public void bleshTemplateResultCallback(String actionType, String actionValue) {
                if (!actionType.isEmpty() && !actionValue.isEmpty()) {
                    Log.i(TAG, "bleshTemplateResultCallback: action type:" + actionType + " value: " + actionValue);
                    // Check for the action type and value you want to use
                    // You may wish to load a web
                    String msg = "bleshTemplateResultCallback: action type:" + actionType + " value: " + actionValue;
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
                else {
                    Log.i(TAG, "bleshTemplateResultCallback: empty action type or value");
                    Toast.makeText(getApplicationContext(), "bleshTemplateResultCallback: empty action type or value", Toast.LENGTH_LONG).show();
                }
            }
        };

        // Register Blesh callback
        BleshInstance.sharedInstance().setTemplateResult(result);

        // Start Blesh service
        startBlesh();
/*
        // Initialize HeartRateCore SDK
        HeartRateCoreListener hrListener = new HeartRateCoreListener() {
            @Override
            public void onStatusChanged(@NonNull EnumSet<HeartRateCoreStatus> heartRateCoreStatuses) {
                Log.i(TAG, "HeartRate: onStatusChanged: status=" + heartRateCoreStatuses);
                Toast.makeText(getApplicationContext(), "HeartRate: onStatusChanged: status=" + heartRateCoreStatuses, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onHeartRateChanged(int i) {
                Log.i(TAG, "HeartRate: onHeartRateChanged called heartRate=" + i);
                Toast.makeText(getApplicationContext(), "HeartRate: onHeartRateChanged called heartRate=" + i, Toast.LENGTH_LONG).show();
            }
        };

        hrCore = HeartRateCore.getInstance(getBaseContext(), hrListener);
        hrCore.start();
*/
        // Start ListView activity
        //Intent intent = new Intent(this, UserPromptActivity.class);
        //startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        super.onResume();
        // Make sure Bluetooch is enabled
        enableBluetooth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hrCore.stop();
        hrCore.destroy();
    }

    /**
     * Ensures Bluetooth is available on the beacon and it is enabled. If not,
     * displays a dialog requesting user permission to enable Bluetooth.
     */
    private void enableBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void startBlesh() {
        Log.i(TAG, "startBlesh");

        // Customer specific initialization parameters
        Intent blesh = new Intent(this, Blesh.class);
        blesh.putExtra("APIUser", "intel");
        blesh.putExtra("APIKey", "RjKqGrNxEs");
        blesh.putExtra("integrationType", "M");
        blesh.putExtra("integrationId", "5033171751");
        blesh.putExtra("pushToken", "");
        blesh.putExtra("optionalKey", "5033171751");

        /*
         * optionalKey ve integrationId should be String
         * optionalKey is not mandatory while integrationId is
         */

        // Start the Blesh service using the bundle you have just created
        startService(blesh);
    }
}
