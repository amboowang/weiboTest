package com.codepath.example.weiboTest;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomUsersAdapter extends ArrayAdapter<User> {
    //private ArrayList<User> items;

    public CustomUsersAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        //items = users;
     }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);    
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
           convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHometown);
        ImageView thumbnail_pic = (ImageView) convertView.findViewById(R.id.tvImage);
        // Populate the data into the template view using the data object
        tvName.setText(user.name);
        tvHome.setText(user.hometown);
        if (user.bitmap != null) {
            thumbnail_pic.setImageBitmap(user.bitmap);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}