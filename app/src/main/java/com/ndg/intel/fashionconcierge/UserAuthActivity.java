package com.ndg.intel.fashionconcierge;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


public class UserAuthActivity extends ActionBarActivity {

    GraphView graph = null;
    LineGraphSeries<DataPoint> series = null;
    int i = 0;
    int j = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auth);

        ImageView userAuthImage = (ImageView) findViewById(R.id.user_auth_image);
        userAuthImage.setImageResource(R.drawable.heart);

        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 0)
        });

        graph.addSeries(series);

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                i++;
                j = 0 + (int)(Math.random()*100);
                series.appendData(new DataPoint(i,j), false, 100);
                graph.addSeries(series);
                handler.postDelayed(this, 100);
            }
        };

        handler.postDelayed(r, 100);


        //final StoreAssociate associate = getStoreAssociate();

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

    private StoreAssociate getStoreAssociate() {
        StoreAssociate associate = null;
        try {
            JSONObject obj = new JSONObject(getIntent().getStringExtra("JSON"));
            associate = new StoreAssociate(obj.getString("name"),
                    UUID.fromString(obj.getString("uuid")));
        } catch (JSONException e) {

        }
        return associate;
    }
}
