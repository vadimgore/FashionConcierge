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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
                matchUserProfile();
            }
        };

        handler.postDelayed(r, 10000);
    }

    private void matchUserProfile() {

        double savedHRMProfile[] = new double[] {74.28806, 74.80511, 76.07594, 77.69099,
                                                 79.06474, 80.16972, 80.84673, 81.23549,
                                                 81.20487, 80.73627, 80.39274, 80.1198,
                                                 80.16982};
        double currentHRMProfile[] = new double[] {77.5737, 77.39081, 78.73197, 79.89495,
                                                   80.743576, 80.90538, 80.761986, 80.37992,
                                                   79.92841, 79.84282, 79.797615, 79.88893,
                                                   80.02692};

        if (corr(savedHRMProfile, currentHRMProfile) > 0.5) {
            mHrmState = HRMSensorState.SUCCESS;
        }
        else {
            mHrmState = HRMSensorState.FAIL;
        }
    }

    /**
     * Execute the cross correlation between signal x1 and x2 and calculate the time delay.
     */
    public double corr(double[] x1, double[] x2)
    {
        // define the size of the resulting correlation array
        int corrSize = 2*x1.length;

        // create correlation array
        double out[] = new double[corrSize];

        // shift variable
        int shift = x1.length;

        double val;
        int maxIndex = 0;
        int delay = 0;
        double maxVal = 0;

        // we have push the signal from the left to the right
        for(int i=0; i < corrSize; i++)
        {
            val = 0;

            // multiply sample by sample and sum up
            for(int k=0; k < x1.length; k++)
            {
                // x2 has reached his end - abort
                if((k+shift) > (x2.length -1))
                {
                    break;
                }

                // x2 has not started yet - continue
                if((k+shift) < 0)
                {
                    continue;
                }

                // multiply sample with sample and sum up
                val += x1[k] * x2[k+shift];

                //System.out.print("x1["+k+"] * x2["+(k+tmp_tau)+"] + ");
            }

            //System.out.println();
            // save the sample
            out[i] = val;
            shift--;

            // save highest correlation index
            if(out[i] > maxVal)
            {
                maxVal = out[i];
                maxIndex = i;
            }
        }

        // set the delay
        delay = maxIndex - x1.length;

        return maxVal;
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
            case SUCCESS:
                // Unregister HRM sensor listener
                mSensorManager.unregisterListener(this);
                mUserAuthState.setText(R.string.user_auth_state_success);
                mUserAuthMsg.setText(R.string.user_auth_state_success_msg);
                //saveHRMProfile();
                //logHRMProfile();
                break;
            case FAIL:
                mUserAuthState.setText(R.string.user_auth_state_fail);
                mUserAuthMsg.setText(R.string.user_auth_state_fail_msg);
                //saveHRMProfile();
                //logHRMProfile();
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
