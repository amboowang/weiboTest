package com.codepath.example.customadapterdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import javax.xml.transform.Result;

public class CustomListActivity extends Activity implements WeiboAuthListener {
    private static final String APP_KEY = "2193587819";
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String SCOPE = "direct_messages_read";
    private AuthInfo authInfo;
    private SsoHandler sso;
    private Oauth2AccessToken m_accessToken;
    private ArrayList<User> users = new ArrayList<User>();
    public static httpTask task;
    private Handler handler;
    // weibo authlister
    @Override
    public void onCancel() {
        Log.d("ListExample", "Auth cancel");
    }

    @Override
    public void onComplete(Bundle arg0) {
        Log.d("ListExample", "Auth onComplete");
        m_accessToken = Oauth2AccessToken.parseAccessToken(arg0);
        //maccessToken = accessToken;
        //CustomListActivity.m_accessToken = accessToken;

        if(m_accessToken.isSessionValid()) {
            //accessToken.getToken();
            Log.d("ListExample", "Auth success, token"+m_accessToken);

            //get the public line

            //CustomListActivity.task.execute();
        }
        else{
            Log.d("ListExample", "Auth failure");
        }
    }

    @Override
    public void onWeiboException(WeiboException arg0) {
        Log.d("ListExample", "Auth Exception"+arg0.getMessage());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ListExample", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list);

        //set up the handler
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMsg) {
                switch (inputMsg.what){
                    //
                }
            }
        };


        //populateUsersList();
        //create the asnyc task
        task = new httpTask();

        // Weibo auth
        Log.d("ListExample", "Begin Weibo auth");
        authInfo = new AuthInfo(CustomListActivity.this, APP_KEY, REDIRECT_URL, SCOPE);
        sso = new SsoHandler(CustomListActivity.this, authInfo);

        Log.d("ListExample", "set the listener");
        sso.authorize(CustomListActivity.this);

    }
    @Override
    protected void onResume()
    {
        Log.d("ListExample", "onResume");
        super.onResume();
        //weibo auth
        //WeiboAuthListener
        //SsoHandler sso = new SsoHandler();
        //sso.authorize();
        //new httpTask().execute();
        task = new httpTask();
        task.execute();
    }
    @Override
    protected void onPause() {
        Log.d("ListExample", "onPause");
        super.onPause();
        task.cancel(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ListExample", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (sso != null)
        {
            sso.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private void populateUsersList() {
        // Construct the data source
        //ArrayList<User> arrayOfUsers = User.getUsers();
        // Create the adapter to convert the array to views
        //CustomUsersAdapter adapter = new CustomUsersAdapter(this, arrayOfUsers);
        Log.d("ListExample", "new usersAdapter");
        CustomUsersAdapter adapter = new CustomUsersAdapter(this, users);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvUsers);
        Log.d("ListExample", "set adapter");
        listView.setAdapter(adapter);
        Log.d("ListExample", "set adapter complete");
    }

    public class httpTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CustomListActivity.this, "", "Loading", true);
            progressDialog.setCancelable(true);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    //clean code
                }
            });
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                HttpClient hc = new DefaultHttpClient();

                HttpGet get = new HttpGet("https://api.weibo.com/2/statuses/public_timeline.json?access_token="+m_accessToken.getToken());
                //HttpGet get = new HttpGet("http://search.twitter.com/search.json?q=android");
                //HttpGet get = new HttpGet("https://api.douban.com/v2/loc/list");
                Log.d("ListExample", "Execute http get");
                HttpResponse rp = hc.execute(get);
                Log.d("ListExample", "Execute http get complete code:"+rp.getStatusLine().getStatusCode());

                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.d("ListExample", "HttpStatus OK");
                    String result = EntityUtils.toString(rp.getEntity());
                    Log.d("ListExample", result);

                    JSONObject root = new JSONObject(result);
                    JSONArray sessions = root.getJSONArray("statuses");

                    for (int i = 0; i < sessions.length(); i++) {
                        JSONObject item = sessions.getJSONObject(i);

                        Log.d("ListExample: session", Integer.toString(i));
                        User user = new User();
                        user.name = item.getString("id");
                        user.hometown = item.getString("text");
                        users.add(user);
                    }

                } else {
                    Log.d("ListExample", "Loading weibo Failure");
                }

            } catch (Exception e) {
                Log.e("ListExample", "Error loading JSON", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            Log.d("ListExample", "Http task post execute");
            /*
            if (users.isEmpty()) {
                users = User.getUsers();
            }
            */
            populateUsersList();

        }

    }

	
}
