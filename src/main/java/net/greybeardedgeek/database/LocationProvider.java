package net.greybeardedgeek.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class LocationProvider extends ContentProvider {

    private static final String TAG = "LocationProvider";
    static final String AUTHORITY = "net.greybeardedgeek.database.LocationProvider";

    static final int ALL_LOCATIONS = 0;
    static final int FAVORITE_LOCATIONS = 1;
    static final int RECENT_LOCATIONS = 2;
    static final int SINGLE_LOCATION = 3;


    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "locations",  ALL_LOCATIONS);
        uriMatcher.addURI(AUTHORITY, "locations/favorite",  FAVORITE_LOCATIONS);
        uriMatcher.addURI(AUTHORITY, "locations/recent",  RECENT_LOCATIONS);
        uriMatcher.addURI(AUTHORITY, "locations/#",  SINGLE_LOCATION);
    }

    public static final class Locations {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/locations");
        public static final String TABLE_NAME = "locations";
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "long";
        public static final String IS_FAVORITE = "favorite";
        public static final String LAST_USED = "last_used";
    }

    private DatabaseHelper helper;

    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(getContext(),
                DatabaseHelper.DATABASE_NAME,
                null,
                DatabaseHelper.VERSION_NUMBER);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Locations.TABLE_NAME);

        String groupBy = null;
        String having = null;

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case ALL_LOCATIONS:
                return "vnd.android.cursor.dir/vnd.greybeardedgeek.provider.locations";
            case FAVORITE_LOCATIONS:
                return "vnd.android.cursor.item/vnd.greybeardedgeek.provider.locations.favorite";
            case RECENT_LOCATIONS:
                return "vnd.android.cursor.item/vnd.greybeardedgeek.provider.locations.recent";
            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String nullColumnHack = null;

        String tableName = null;
        Uri contentUri = null;
        ContentValues locationValues = new ContentValues();
        locationValues.put(Locations.NAME, values.getAsString(Locations.NAME));
        locationValues.put(Locations.ADDRESS, values.getAsString(Locations.ADDRESS));
        locationValues.put(Locations.LATITUDE, values.getAsString(Locations.LATITUDE));
        locationValues.put(Locations.LONGITUDE, values.getAsString(Locations.LONGITUDE));
        locationValues.put(Locations.IS_FAVORITE, values.getAsString(Locations.IS_FAVORITE));
        locationValues.put(Locations.LAST_USED, values.getAsString(Locations.LAST_USED));

        switch(uriMatcher.match(uri)) {
            case ALL_LOCATIONS:
                contentUri = Locations.CONTENT_URI;
                tableName = Locations.TABLE_NAME;
                break;

            default:
                break;
        }

        if (tableName != null) {
            long id = db.insertWithOnConflict(tableName, nullColumnHack, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id > -1) {
                Uri insertedId = ContentUris.withAppendedId(contentUri, id);
                getContext().getContentResolver().notifyChange(insertedId, null);
                return insertedId;
            }
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();

        String rowId = null;
        switch(uriMatcher.match(uri)) {

            case SINGLE_LOCATION:
                rowId = uri.getPathSegments().get(1);
                selection = Locations.ID + "=" + rowId + (!isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;

            default:
                break;
        }

        if (selection == null) {
            selection = "1";
        }

        int deleteCount = db.delete(Locations.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();

        String rowId = null;
        switch(uriMatcher.match(uri)) {
            case SINGLE_LOCATION:
                rowId = uri.getPathSegments().get(1);
                selection = Locations.ID + "=" + rowId + (!isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;

            default:
                break;
        }

        int updateCount = db.update(Locations.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    @Override
    public void shutdown() {
        helper.close();
    }

    private boolean isEmpty(String string) {
        return (string == null || string.isEmpty());
    }
}
