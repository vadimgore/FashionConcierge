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

import org.apache.commons.math3.analysis.function.Min;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


enum HRMSensorState {
    NOT_READY,
    READY,
    PROCESS,
    SUCCESS,
    FAIL
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Handle presses on the action bar items
        switch (id) {
            case R.id.action_save_hrm_profile:
                saveHRMProfile(true);
                return true;
            case R.id.action_settings:
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register HRM sensor listener
        mSensorManager.registerListener(this, this.mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister HRM sensor listener
        clearAll();
        mSensorManager.unregisterListener(this);
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
 //       mGraphView.getGridLabelRenderer().setHorizontalAxisTitle("time (sec)");
 //       mGraphView.getGridLabelRenderer().setVerticalAxisTitle("bpm");

        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(10);
/*
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(60);
        mGraphView.getViewport().setMaxY(120);
*/
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mGraphView);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        //staticLabelsFormatter.setVerticalLabels(new String[]{"70", "80", "90", "100"});
        mGraphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        mSeries = new LineGraphSeries<DataPoint>();
        mGraphView.addSeries(mSeries);
    }

    private void delayedStopHRM() {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                saveHRMProfile(false);
                //logHRMProfile();
                matchUserProfile();
            }
        };

        handler.postDelayed(r, 11000);
    }

    private void delayedRestartHRM() {
        Handler handler = new Handler();
        final SensorEventListener listener = this;
        final Runnable r = new Runnable() {
            public void run() {
                clearAll();
                mSensorManager.registerListener(listener, mHeartRateSensor,
                                                SensorManager.SENSOR_DELAY_FASTEST);
            }
        };

        handler.postDelayed(r, 5000);
    }

    private void matchUserProfile() {
/*
        double savedHRMProfile[] = new double[] {74.28806, 74.80511, 76.07594, 77.69099,
                                                 79.06474, 80.16972, 80.84673, 81.23549,
                                                 81.20487, 80.73627, 80.39274, 80.1198,
                                                 80.16982};
        double currentHRMProfile[] = new double[] {77.5737, 77.39081, 78.73197, 79.89495,
                                                   80.743576, 80.90538, 80.761986, 80.37992,
                                                   79.92841, 79.84282, 79.797615, 79.88893,
                                                   80.02692};
*/
        // Obtain stored and current HRM profile
        List<String> savedHRMProfile = getHRMProfile();
        List<String> currentHRMProfile = getHRMSeries();

        // Convert to double arrays
        int szArray = Math.min(savedHRMProfile.size(), currentHRMProfile.size());
        // If less than 5 probe points the correlation cannot be established
        if (szArray < 5) {
            mHrmState = HRMSensorState.FAIL;
            return;
        }

        double dSavedProfile[] = new double[szArray];
        double dCurrentProfile[] = new double[szArray];

        for (int i=0; i < szArray; i++) {
            dSavedProfile[i] = Double.parseDouble(savedHRMProfile.get(i));
            dCurrentProfile[i] = Double.parseDouble(currentHRMProfile.get(i));
        }

        double confidence = corr(dSavedProfile, dCurrentProfile);
        displayConfidence(confidence);
        if (confidence < 0.5) {
            mHrmState = HRMSensorState.FAIL;
            return;
        }

        mHrmState = HRMSensorState.SUCCESS;
    }

    /**
     * Execute the cross correlation between signal x1 and x2 and calculate the time delay.
     */
    public double corr(double[] x1, double[] x2)
    {
        //use http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.7_Covariance_and_correlation
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        double corr = pearsonsCorrelation.correlation(x1, x2);

        return corr;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("UserAuthActivity", "sensor event accuracy: " + event.accuracy + " = " + event.values[0]);
        switch (mHrmState) {
            case NOT_READY:
                mHrmState = HRMSensorState.READY;
                break;
            case READY:
                if (event.values[0] > 0) {
                    // The HRM sensor is ready to start processing
                    mUserAuthState.setText(R.string.user_auth_state_process);
                    mUserAuthMsg.setText(R.string.user_auth_state_process_msg);
                    mHrmState = HRMSensorState.PROCESS;
                    this.timestamp = System.currentTimeMillis();
                    delayedStopHRM();
                }
                break;
            case PROCESS:
                double i = (double)(System.currentTimeMillis() - this.timestamp) / 1000;
                Log.i("HRM", "add point (" + i + ", " + event.values[0] + ")");
                String val = Double.toString(event.values[0]);
                mHRMProfile.add(val);
                Log.i("HRM", "str = " + val);
                mSeries.appendData(new DataPoint(i, event.values[0]), false, 500);
                // Finish measurements in 10 seconds
                break;
            case SUCCESS:
                // Unregister HRM sensor listener
                mSensorManager.unregisterListener(this);
                mUserAuthState.setText(R.string.user_auth_state_success);
                mUserAuthMsg.setText(R.string.user_auth_state_success_msg);
                break;
            case FAIL:
                mSensorManager.unregisterListener(this);
                mUserAuthState.setText(R.string.user_auth_state_fail);
                mUserAuthMsg.setText(R.string.user_auth_state_fail_msg);
                delayedRestartHRM();
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

    private void saveHRMProfile(boolean overwrite) {
        // Check if HRMProfile already exists
        if (!overwrite && getHRMProfile() != null) return;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        ArrayList<String> profile = getHRMSeries();
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

        if (!hrmProfile.isEmpty()) {
            return deserializeHRMProfile(hrmProfile);
        }

        return null;
    }

    private void logHRMProfile() {
        ArrayList<String> hrmProfile = getHRMProfile();
        Iterator<String> iter = hrmProfile.iterator();
        while (iter.hasNext()) {
            Log.i("HRM profile: ", iter.next());
        }
    }

    private void displayConfidence(double confidence) {
        TextView txtConf = (TextView) findViewById(R.id.user_auth_confidence);
        txtConf.setText("Authentication Confidence Level: " + Math.round(confidence * 100) + "%");
    }

    private void clearAll() {

        // Clear GraphView
        mHrmState = HRMSensorState.NOT_READY;

        // Clear HRM Profile
        clearHRMProfile();
        mSeries.resetData(new DataPoint[]{});
        //mGraphView.removeAllSeries();

        // Reset text views
        mUserAuthState.setText(R.string.user_auth_state_ready);
        mUserAuthMsg.setText(R.string.user_auth_ready_msg);
        TextView txtConf = (TextView) findViewById(R.id.user_auth_confidence);
        txtConf.setText("");

        // Reset timestamp
        timestamp = 0;
    }
}
