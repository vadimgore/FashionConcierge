package com.ndg.intel.fashionconcierge;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by vgore on 1/26/2015.
 */
public class CustomListViewAdapter extends ArrayAdapter<StoreAssociate>{
    private final Context context;
    private final StoreAssociate[] associates;

    public CustomListViewAdapter(Context context, StoreAssociate[] associates) {
        super(context, R.layout.activity_user_prompt, associates);
        this.context = context;
        this.associates = associates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listView = inflater.inflate(R.layout.activity_user_prompt, parent, false);
        TextView nameView = (TextView) listView.findViewById(R.id.name);
        TextView titleView = (TextView) listView.findViewById(R.id.title);
        ImageView pictureView = (ImageView) listView.findViewById(R.id.icon);
        ImageView mainLangView = (ImageView) listView.findViewById(R.id.mainLang);
        ImageView secLangView = (ImageView) listView.findViewById(R.id.secLang);

        nameView.setText(associates[position].name);
        titleView.setText(associates[position].title);
        pictureView.setImageResource(associates[position].imageId);
        mainLangView.setImageResource(associates[position].mainLangId);
        secLangView.setImageResource(associates[position].secLangId);

        return listView;
    }
}
