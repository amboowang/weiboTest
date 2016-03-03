package com.codepath.example.weiboTest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by DVNG74 on 3/2/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "WeiboTest";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weiboTest.db";
    public static final String TABLE_WEIBO = "weiboList";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "_NAME";
    public static final String COLUMN_TEXT = "_TEXT";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_WEIBO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TEXT + " TEXT" + ")";

        //You could define id as an auto increment column:
        //create table entries (id integer primary key autoincrement, data)
        //As MichaelDorner notes, the SQLite documentation says that an integer primary key does the same thing and is slightly faster. A column of that type is an alias for rowid, which behaves like an autoincrement column.
        //create table entries (id integer primary key, data)
        //http://stackoverflow.com/questions/9342249/how-to-insert-a-unique-id-into-each-sqlite-row
        Log.d(TAG, "DBHelper: onCreate");
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String UPDATE_TABLE = "FROP TABLE IF EXIST " + TABLE_WEIBO;
        onCreate(sqLiteDatabase);
    }

    public void add(User item) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, item.name);
        values.put(COLUMN_TEXT, item.hometown);

        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(TAG, "DBHelper: add");
        db.insert(TABLE_WEIBO, null, values);
        db.close();
    }

    // load the rows from SQLite to the arrayList
    public void load(ArrayList<User> arrayList) {
        String QUERY = "Select * FROM "+TABLE_WEIBO;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d(TAG, "DBHelper: load");
                do {
                    User user = new User();

                    user.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    user.hometown = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT));
                    arrayList.add(user);
                }while(cursor.moveToNext());
            }
        }
    }
}
