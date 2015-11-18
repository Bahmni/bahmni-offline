package com.ionicframework.bahmni;

/**
 * Created by TWI on 16/11/15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;


/**
 * Created by TWI on 13/11/15.
 */
public class WebAppInterface {
    Context mContext;
    private FeedReaderDbHelper mDbHelper;


    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
        mDbHelper = new FeedReaderDbHelper(c);
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void insertData(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int id=1;

// Create a new map of values, where column names are the keys
        id++;
        for(int i = 0 ; i < 100; i++){

            ContentValues values = new ContentValues();
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID, id);
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "yayvdkxehwvkhjvekwfvwqejhfkwehdvfjwhvljhfvljhfvlwerhvfkwjehvfkjwhevfljhwevlfjhvwelfjhvwelfjhvwehfvwehflewfvhev" + id);

            db.insert( FeedReaderContract.FeedEntry.TABLE_NAME,
                    null,
                    values);
        }
        System.out.println("yaaaaay");
    }

    @JavascriptInterface
    public void getData(){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE
        };
        Cursor c = db.rawQuery("select * from " + FeedReaderContract.FeedEntry.TABLE_NAME, new String[]{});
        c.moveToFirst();
        long itemId = c.getLong(
                c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID)
        );

    }
}
