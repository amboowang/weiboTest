package com.codepath.example.weiboTest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
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
    public static final String COLUMN_BMIDDLE_PIC = "_BMIDDLE_IMAGE";
    public static final String COLUMN_PROFILE_PIC = "_PROFILE_IMAGE";

    private SQLiteDatabase mSqlDB = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_WEIBO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TEXT + " TEXT,"
                + COLUMN_PROFILE_PIC + " BLOB,"
                + COLUMN_BMIDDLE_PIC + " BLOB"+")";

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
        Log.d(TAG, "DBHelper: onUpgrade");
        String UPDATE_TABLE = "FROP TABLE IF EXIST " + TABLE_WEIBO;
        onCreate(sqLiteDatabase);
    }

    public void  open() {
        if (mSqlDB == null) {
            mSqlDB = this.getWritableDatabase();
        }
    }

    public void close() {
        if (mSqlDB != null) {
            mSqlDB.close();
        }
    }

    public void add(User item) {
        Log.d(TAG, "DBHelper: add");
        //check if the entry is already existed in SQLite
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, item.name);
        values.put(COLUMN_TEXT, item.hometown);

        if (item.profileImage != null) {
            Log.d(TAG, "DBHelper: add the profile image");
            values.put(COLUMN_PROFILE_PIC, getBytes(item.profileImage));
        }
        if (item.bitmap != null) {
            Log.d(TAG, "DBHelper: add the image");
            values.put(COLUMN_BMIDDLE_PIC, getBytes(item.bitmap));
        }

        //SQLiteDatabase db = this.getWritableDatabase();
        if (mSqlDB != null) {
            mSqlDB.insert(TABLE_WEIBO, null, values);
        }
        //db.close();
    }

    // load the rows from SQLite to the arrayList
    public void load(ArrayList<User> arrayList) {

        if (mSqlDB != null) {
            String QUERY = "Select * FROM " + TABLE_WEIBO;

            //SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = mSqlDB.rawQuery(QUERY, null);

            if (cursor != null) {
                if (cursor.moveToLast()) {
                    Log.d(TAG, "DBHelper: load");
                    int count = 0;
                    do {
                        User user = new User();

                        user.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                        user.hometown = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT));

                        if (!cursor.isNull(cursor.getColumnIndex(COLUMN_PROFILE_PIC))) {
                            Log.d(TAG, "DBHelper: load the profile image");
                            user.profileImage = getImage(cursor.getBlob(cursor.getColumnIndex(COLUMN_PROFILE_PIC)));
                        }

                        if (!cursor.isNull(cursor.getColumnIndex(COLUMN_BMIDDLE_PIC))) {
                            Log.d(TAG, "DBHelper: load the image");
                            user.bitmap = getImage(cursor.getBlob(cursor.getColumnIndex(COLUMN_BMIDDLE_PIC)));
                        }

                        arrayList.add(user);
                        count++;
                    } while ((cursor.moveToPrevious() && (count <= 20)));
                }
            }
            //db.close();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
