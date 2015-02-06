package com.ndg.intel.fashionconcierge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

enum HRMSensorState {
    NOT_READY,
    READY,
    PROCESS,
    DONE
};

public class UserAuthActivity extends ActionBarActivity implements SensorEventListener {

    GraphView mGraphView = null;
    LineGraphSeries<DataPoint> mSeries = null;
    HRMSensorState mHrmState = HRMSensorState.NOT_READY;
    TextView mUserAuthState = null;
    TextView mUserAuthMsg = null;
    SensorManager mSensorManager = null;
    Sensor mHeartRateSensor = null;
    StoreAssociate mStoreAssociate = null;
    long timestamp = 0;
    ArrayList<String> mHRMProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auth);

        // Initialize TextViews
        mUserAuthState = (TextView) findViewById(R.id.user_auth_state);
        mUserAuthState.setText(R.string.user_auth_state_ready);
        mUserAuthMsg = (TextView) findViewById(R.id.user_auth_msg);
        mUserAuthMsg.setText(R.string.user_auth_ready_msg);

        // Extract store associate from Intent's extra
        mStoreAssociate = getStoreAssociate();

        // Configure GraphView
        configureGraphView();

        // Initialize HRM sensor
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(65562);
        mHRMProfile = new ArrayList<String>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_auth, menu);
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
    protected void onStart() {
        super.onStart();
        // Register HRM sensor listener
        mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
        clearHRMProfile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister HRM sensor listener
        clearHRMProfile();
        mSensorManager.unregisterListener(this);
        mHrmState = HRMSensorState.NOT_READY;
        timestamp = 0;
    }

    private StoreAssociate getStoreAssociate() {
        StoreAssociate associate = null;
        try {
            if (getIntent().getStringExtra("JSON") != null) {
                JSONObject obj = new JSONObject(getIntent().getStringExtra("JSON"));
                associate = new StoreAssociate(obj.getString("name"),
                        UUID.fromString(obj.getString("uuid")));
            }
        } catch (JSONException e) {

        }

        return associate;
    }

    private void configureGraphView() {
        ImageView userAuthImage = (ImageView) findViewById(R.id.user_auth_image);
        userAuthImage.setImageResource(R.drawable.heart);

        mGraphView = (GraphView) findViewById(R.id.graph);
//        mGraphView.getGridLabelRenderer().setHorizontalAxisTitle("time (sec)");
//        mGraphView.getGridLabelRenderer().setVerticalAxisTitle("bpm");

        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(10);
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(70);
        mGraphView.getViewport().setMaxY(100);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraphView);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        staticLabelsFormatter.setVerticalLabels(new String[]{"70", "80", "90", "100"});
        mGraphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        mSeries = new LineGraphSeries<DataPoint>();
        mGraphView.addSeries(mSeries);
    }

    private void delayedStopHRM() {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mHrmState = HRMSensorState.DONE;
            }
        };

        handler.postDelayed(r, 10000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i("UserAuthActivity", "sensor event accuracy: " + event.accuracy + " = " + event.values[0]);
        switch (mHrmState) {
            case READY:
                if (event.values[0] > 0) {
                    // The HRM sensor is ready to start processing
                    mUserAuthState.setText(R.string.user_auth_state_process);
                    mUserAuthMsg.setText(R.string.user_auth_state_process_msg);
                    mHrmState = HRMSensorState.PROCESS;
                    this.timestamp = System.currentTimeMillis();
                }
                break;
            case PROCESS:
                long i = (System.currentTimeMillis() - this.timestamp) / 1000;
                Log.i("HRM", "add point (" + i + ", " + event.values[0] + ")");
                String val = Double.toString(event.values[0]);
                mHRMProfile.add(val);
                Log.i("HRM", "str = " + val);
                mSeries.appendData(new DataPoint(i, event.values[0]), false, 500);
                // Finish measurements in 10 seconds
                delayedStopHRM();
                break;
            case DONE:
                // Unregister HRM sensor listener
                mSensorManager.unregisterListener(this);
                mUserAuthState.setText(R.string.user_auth_state_done);
                mUserAuthMsg.setText(R.string.user_auth_state_done_msg);

                saveHRMProfile();
                logHRMProfile();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("UserAuthActivity", "HRM accuracy changed: " + accuracy);
        mHrmState = HRMSensorState.READY;
        mUserAuthState.setText(R.string.user_auth_state_init);
        mUserAuthMsg.setText(R.string.user_auth_init_msg);
    }

    private ArrayList<String> getHRMSeries() {
        return mHRMProfile;
    }

    private void clearHRMProfile() {
        mHRMProfile.clear();
    }

    private void saveHRMProfile() {
        ArrayList<String> profile = getHRMSeries();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putString(getResources().getText(R.string.user_auth_bpm_profile).toString(),
                        serializeHRMProfile(profile));
        editor.commit();
    }

    private String serializeHRMProfile(ArrayList<String> hrmProfile) {
        StringBuilder sb = new StringBuilder();
        for (String s : hrmProfile)
        {
            sb.append(s);
            sb.append(",");
        }
        return sb.toString();
    }

    private ArrayList<String> deserializeHRMProfile(String hrmProfile) {
        ArrayList<String> array = new ArrayList<String>(Arrays.asList(hrmProfile.split(",")));
        return array;
    }

    private ArrayList<String> getHRMProfile() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String hrmProfile = sharedPref.getString(getResources().getText(R.string.user_auth_bpm_profile).toString(), new String());

        return deserializeHRMProfile(hrmProfile);
    }

    private void logHRMProfile() {
        ArrayList<String> hrmProfile = getHRMProfile();
        Iterator<String> iter = hrmProfile.iterator();
        while (iter.hasNext()) {
            Log.i("HRM profile: ", iter.next());
        }
    }

}
