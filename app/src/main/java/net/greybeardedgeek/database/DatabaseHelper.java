package net.greybeardedgeek.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "navjump";

    public static final int VERSION_NUMBER = 1;

    static final String LOCATION_TABLE_CREATE =
            "create table " + LocationProvider.Locations.TABLE_NAME + " ("
            + LocationProvider.Locations.ID + " integer primary key autoincrement, "
            + LocationProvider.Locations.NAME + " text, "
            + LocationProvider.Locations.ADDRESS + " text, "
            + LocationProvider.Locations.LATITUDE + " text, "
            + LocationProvider.Locations.LONGITUDE + " text, "
            + LocationProvider.Locations.IS_FAVORITE + " favorite boolean, "
            + LocationProvider.Locations.LAST_USED + " last_used integer"
            + ")";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationProvider.Locations.TABLE_NAME);
    }
}
