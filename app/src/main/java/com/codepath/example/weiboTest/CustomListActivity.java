package com.codepath.example.weiboTest;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.ListView;

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
    private SwipeRefreshLayout mRefreshlayout;
    private boolean mAuthSuccessed;
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

        if(m_accessToken.isSessionValid()) {
            //accessToken.getToken();
            Log.d(TAG, "Auth success, token"+m_accessToken);

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
        }
        else{
            Log.d(TAG, "Auth failure");
        }
    }

    @Override
    public void onWeiboException(WeiboException arg0) {
        Log.d(TAG, "Auth Exception"+arg0.getMessage());
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
            switch (inputMsg.what){
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
        mRefreshlayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        mRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Begin refresh");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new httpTask().execute();
                    }
                });
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
        adapter = new CustomUsersAdapter(this, users);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvUsers);
        listView.setAdapter(adapter);
        Log.d(TAG, "set adapter complete");

    }
    @Override
    protected void onResume()
    {
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

        if (sso != null)
        {
            sso.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    public class httpTask extends AsyncTask<Void, Void, ArrayList<User>> {
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

                HttpGet get = new HttpGet("https://api.weibo.com/2/statuses/public_timeline.json?access_token="+m_accessToken.getToken());
                //HttpGet get = new HttpGet("http://search.twitter.com/search.json?q=android");
                //HttpGet get = new HttpGet("https://api.douban.com/v2/loc/list");
                Log.d(TAG, "Execute http get");
                HttpResponse rp = hc.execute(get);
                Log.d(TAG, "Execute http get complete code:"+rp.getStatusLine().getStatusCode());

                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.d(TAG, "HttpStatus OK");
                    String result = EntityUtils.toString(rp.getEntity());
                    Log.d(TAG, result);

                    JSONObject root = new JSONObject(result);
                    JSONArray sessions = root.getJSONArray("statuses");

                    for (int i = 0; i < sessions.length(); i++) {
                        JSONObject item = sessions.getJSONObject(i);

                        Log.d("ListExample: session", Integer.toString(i));
                        User user = new User();
                        user.name = item.getString("id");
                        user.hometown = item.getString("text");

                        if (item.has("bmiddle_pic"))
                        {
                            try {
                                Log.d(TAG, "Load the bmiddle_pic");
                                user.bitmap = BitmapFactory.decodeStream((InputStream)new URL(item.getString("bmiddle_pic")).getContent());
                            }
                            catch (Exception e){
                                Log.d(TAG, "Load the bmiddle_pic exception");
                            }
                        }

                        tItems.add(user);
                        //adapter.add(user);

                        //save to the SQLite
                        dbHelper.add(user);
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
            mRefreshlayout.setRefreshing(false);
            //populateUsersList();

            users.clear();
            //users.addAll(result);
            dbHelper.load(users);

            //adapter.clear();
            //adapter.addAll(users);

            adapter.notifyDataSetChanged();
            //adapter.notifyDataSetInvalidated();

        }

    }

	
}
