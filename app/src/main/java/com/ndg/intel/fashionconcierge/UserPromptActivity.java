package com.ndg.intel.fashionconcierge;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class UserPromptActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StoreAssociate[] associates = {
                new StoreAssociate("Leo","An outgoing and friendly Sales Ninja with great taste in fashion... ", R.drawable.leo,
                        R.drawable.english, R.drawable.italian),
                new StoreAssociate("Akino","A true SELLebrity with tons of experience in foot-ware...", R.drawable.akino,
                        R.drawable.english, R.drawable.japanese)
        };

        ArrayAdapter<StoreAssociate> adapter = new CustomListViewAdapter(this, associates);
        setListAdapter(adapter);
        //setContentView(R.layout.activity_my_list);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        StoreAssociate associate = (StoreAssociate) getListAdapter().getItem(position);
        String name = (String) associate.name;
        Toast.makeText(this, name + " selected", Toast.LENGTH_LONG).show();
    }
}
