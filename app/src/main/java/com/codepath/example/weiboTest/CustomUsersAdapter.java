package com.codepath.example.weiboTest;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomUsersAdapter extends ArrayAdapter<User> {
    //private ArrayList<User> items;
    private static final String TAG = "WeiboTest";

    public CustomUsersAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        //items = users;
     }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "CustomUsersAdapter: getView"+position);
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
        ImageView profile_pic = (ImageView) convertView.findViewById(R.id.ivUserIcon);

        // Populate the data into the template view using the data object
        tvName.setText(user.name);
        tvHome.setText(user.hometown);
        if (user.bitmap != null) {
            Log.d(TAG, "CustomUsersAdapter: set the bitmap");
            thumbnail_pic.setImageBitmap(user.bitmap);
            thumbnail_pic.setVisibility(View.VISIBLE);
        }
        else {
            thumbnail_pic.setVisibility(View.GONE);
        }

        if (user.profileImage != null) {
            Log.d(TAG, "CustomUsersAdapter: set the profile bitmap");
            //profile_pic.setImageBitmap(user.profileImage);
            //profile_pic.setImageDrawable();

            //set the image to circle
            RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory.create(getContext().getResources(), user.profileImage);
            rd.setCornerRadius(Math.min(rd.getMinimumWidth(),rd.getMinimumHeight())/2.0f);
            rd.setAntiAlias(true);
            rd.setAlpha(200);
            profile_pic.setImageDrawable(rd);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
