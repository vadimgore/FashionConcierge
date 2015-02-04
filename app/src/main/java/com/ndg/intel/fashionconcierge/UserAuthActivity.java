package com.ndg.intel.fashionconcierge;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


public class UserAuthActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auth);

        ImageView userAuthImage = (ImageView) findViewById(R.id.user_auth_image);
        userAuthImage.setImageResource(R.drawable.heart);

        final StoreAssociate associate = getStoreAssociate();

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
