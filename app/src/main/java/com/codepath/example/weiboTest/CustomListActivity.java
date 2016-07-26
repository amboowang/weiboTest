package com.codepath.example.weiboTest;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomListActivity extends Activity implements WeiboAuthListener {
    private static final String TAG = "WeiboTest";

    private static final String APP_KEY = "2193587819";
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "direct_messages_read";
    private AuthInfo authInfo;
    private SsoHandler sso;
    private Oauth2AccessToken m_accessToken;
    private ArrayList<User> users = new ArrayList<User>();
    private httpTask task;
    private InnerHandler handler;
    private DBHelper dbHelper;
    private CustomUsersAdapter adapter;
    //private SwipeRefreshLayout mRefreshlayout;
    private boolean mAuthSuccessed;

    private SwipyRefreshLayout mswipyRefreshLayout;
    private Future<JsonObject> loading;
    private ArrayAdapter<JsonObject> weiboAdapter;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // weibo authlister
    @Override
    public void onCancel() {
        Log.d(TAG, "Auth cancel");
    }

    @Override
    public void onComplete(Bundle arg0) {
        Log.d(TAG, "Auth onComplete");
        m_accessToken = Oauth2AccessToken.parseAccessToken(arg0);
        //maccessToken = accessToken;
        //CustomListActivity.m_accessToken = accessToken;

        if (m_accessToken.isSessionValid()) {
            //accessToken.getToken();
            Log.d(TAG, "Auth success, token" + m_accessToken);

            //get the public line
            //CustomListActivity.task.execute();
            //Message msg = handler.obtainMessage();
            //msg.what = 1;
            //msg.sendToTarget();
            mAuthSuccessed = true;
            //new httpTask().execute();
            //handler.post(new Runnable() {
            //    @Override
            //    public void run() {
            //        //new httpTask().execute();
            //    }
            //});
        } else {
            Log.d(TAG, "Auth failure");
        }
    }

    @Override
    public void onWeiboException(WeiboException arg0) {
        Log.d(TAG, "Auth Exception" + arg0.getMessage());
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CustomList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.codepath.example.weiboTest/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CustomList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.codepath.example.weiboTest/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // inner handler
    static class InnerHandler extends Handler {
        WeakReference<CustomListActivity> mListActivity;

        InnerHandler(CustomListActivity aListActivity) {
            mListActivity = new WeakReference<CustomListActivity>(aListActivity);
        }

        @Override
        public void handleMessage(Message inputMsg) {
            CustomListActivity theActivity = mListActivity.get();
            switch (inputMsg.what) {
                case 1:
                    Log.d(TAG, "Notify  List activity the auth success");
                    //task.execute();
                    if (theActivity.task != null) {
                        Log.d(TAG, "execute the asyn http task");
                    }

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list);

        mAuthSuccessed = false;

        //set up the handler
        handler = new InnerHandler(this);
        dbHelper = new DBHelper(this);
        //populateUsersList();
        //create the asnyc task
        //task = new httpTask();

        //create the atapter with JsonObject
        weiboAdapter = new ArrayAdapter<JsonObject>(this, 0){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //return super.getView(position, convertView, parent);

                if (convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.item_user, parent, false);
                }
                // Lookup view for data population
                TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
                TextView tvHome = (TextView) convertView.findViewById(R.id.tvHometown);
                ImageView thumbnail_pic = (ImageView) convertView.findViewById(R.id.tvImage);
                ImageView profile_pic = (ImageView) convertView.findViewById(R.id.ivUserIcon);

                JsonObject weiboItem = getItem(position);
                JsonObject user = weiboItem.getAsJsonObject("user");
                String name = user.get("screen_name").getAsString();
                String text = weiboItem.get("text").getAsString();
                String userIconImageUrl = user.get("profile_image_url").getAsString();

                Ion.with(profile_pic).load(userIconImageUrl);

                tvName.setText(name);
                tvHome.setText(text);

                if (weiboItem.has("bmiddle_pic")) {
                    String mPicUrl = weiboItem.get("bmiddle_pic").getAsString();
                    Ion.with(thumbnail_pic).load(mPicUrl);
                }


                return convertView;
            }
        };

        //SwipyRefreshLayoutDirection

        mswipyRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipyrefreshlayout);
        mswipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    Log.d(TAG, "Begin refresh top");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //new httpTask().execute();
                            ionLoad();
                        }
                    });
                } else {
                    Log.d(TAG, "Begin refresh bottom");
                }

            }
        });

        // Weibo auth
        Log.d(TAG, "Begin Weibo auth");
        authInfo = new AuthInfo(CustomListActivity.this, APP_KEY, REDIRECT_URL, SCOPE);
        sso = new SsoHandler(CustomListActivity.this, authInfo);

        Log.d(TAG, "set the listener");
        sso.authorize(CustomListActivity.this);

        //load from SQLite to arrayList
        dbHelper.load(users);

        //set the adapter of list view
        //adapter = new CustomUsersAdapter(this, users);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvUsers);
        //listView.setAdapter(adapter);
        listView.setAdapter(weiboAdapter);
        Log.d(TAG, "set adapter complete");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        //populateUsersList();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        //task.cancel(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (sso != null) {
            sso.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private class httpTask extends AsyncTask<Void, Void, ArrayList<User>> {
        //private ProgressDialog progressDialog;
        ArrayList<User> tItems = new ArrayList<User>();

        @Override
        protected void onPreExecute() {
            /*
            progressDialog = ProgressDialog.show(CustomListActivity.this, "", "Loading", true);
            progressDialog.setCancelable(true);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    //clean code
                }
            });
            */
        }

        @Override
        protected ArrayList<User> doInBackground(Void... arg0) {
            try {
                HttpClient hc = new DefaultHttpClient();

                //JsonObject m;


                HttpGet get = new HttpGet("https://api.weibo.com/2/statuses/public_timeline.json?access_token=" + m_accessToken.getToken());
                //HttpGet get = new HttpGet("http://search.twitter.com/search.json?q=android");
                //HttpGet get = new HttpGet("https://api.douban.com/v2/loc/list");
                Log.d(TAG, "Execute http get");
                HttpResponse rp = hc.execute(get);
                Log.d(TAG, "Execute http get complete code:" + rp.getStatusLine().getStatusCode());

                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.d(TAG, "HttpStatus OK");
                    String result = EntityUtils.toString(rp.getEntity());
                    Log.d(TAG, result);

                    JSONObject root = new JSONObject(result);
                    JSONArray sessions = root.getJSONArray("statuses");

                    for (int i = 0; i < sessions.length(); i++) {
                        JSONObject item = sessions.getJSONObject(i);
                        JSONObject user_info = item.getJSONObject("user");

                        Log.d("ListExample: session", Integer.toString(i));
                        User user = new User();
                        //user.name = item.getString("id");
                        user.name = user_info.getString("screen_name");
                        user.hometown = item.getString("text");

                        try {
                            user.profileImage = BitmapFactory.decodeStream((InputStream) new URL(user_info.getString("profile_image_url")).getContent());
                        } catch (Exception e) {
                            Log.d(TAG, "Load the thumbnail_pic exception");
                        }

                        if (item.has("bmiddle_pic")) {
                            try {
                                Log.d(TAG, "Load the bmiddle_pic");
                                user.bitmap = BitmapFactory.decodeStream((InputStream) new URL(item.getString("bmiddle_pic")).getContent());
                            } catch (Exception e) {
                                Log.d(TAG, "Load the bmiddle_pic exception");
                            }
                        }


                        tItems.add(user);
                        //adapter.add(user);

                        //save to the SQLite
                        //dbHelper.add(user);
                    }

                } else {
                    Log.d(TAG, "Loading weibo Failure");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading JSON", e);
            }
            return tItems;
        }

        @Override
        protected void onPostExecute(ArrayList<User> result) {
            //progressDialog.dismiss();
            Log.d(TAG, "Http task post execute");
            /*
            if (users.isEmpty()) {
                users = User.getUsers();
            }
            */
            mswipyRefreshLayout.setRefreshing(false);
            //populateUsersList();

            users.clear();
            users.addAll(result);
            //dbHelper.load(users);

            //adapter.clear();
            //adapter.addAll(users);

            adapter.notifyDataSetChanged();
            //adapter.notifyDataSetInvalidated();

        }

    }

    private class sqlAsynTask extends AsyncTask<ArrayList<User>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<User>... params) {

            dbHelper.open();

            for ( User item : params[0]) {
                dbHelper.add(item);
            }
            dbHelper.close();
            return null;
        }
    }

    private void ionLoad() {
        //don't attempt to load more if a loading is already in progress
        if (mAuthSuccessed == true) {

            if ((loading != null) && !loading.isDone() && !loading.isCancelled())
                return;

            //request the loads a URL as JsonArray
            Log.d(TAG, "Loading with Ion");
            String url = "https://api.weibo.com/2/statuses/public_timeline.json?access_token=" + m_accessToken.getToken();
            //Log.d(TAG,Ion.with(this).load(url).asJsonObject().toString());

            loading = Ion.with(this).load(url).setLogging(TAG, Log.DEBUG).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    //Log.d(TAG, result.toString());

                    if (e != null) {
                        Log.d(TAG, "Ion loading Error");
                        return;
                    }

                    if (result != null) {
                        Log.d(TAG, "result:"+result.toString());
                        //weiboAdapter.clear();

                        JsonArray resultArray = result.get("statuses").getAsJsonArray();

                        for (int i = 0; i < resultArray.size(); i++) {
                            weiboAdapter.insert(resultArray.get(i).getAsJsonObject(), 0);
                        }

                        mswipyRefreshLayout.setRefreshing(false);
                        weiboAdapter.notifyDataSetChanged();
                    }

                }
            });

        }
    }
}
