package com.vertiba.fastrack.db;

import com.vertiba.fastrack.db.DBConstants.Building;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class GreeAnalyticsDBProvider {
	
	private static final String TAG = "DBProvider";
	
	/**
	 * The database version
	 */
	private static final int DATABASE_VERSION = 1;

	// Handle to a new DatabaseHelper.
	private DatabaseHelper mOpenHelper;

	protected GreeAnalyticsDBProvider(Context context) {
		// Creates a new helper object.
		mOpenHelper = new DatabaseHelper(context);
	}
	
	private static GreeAnalyticsDBProvider instance;
	
	public static void initInstance(Context context) {
		if (instance == null) {
			instance = new GreeAnalyticsDBProvider(context);
		}
	}
	
	public static GreeAnalyticsDBProvider getInstance(){
		return instance;
	}
	
	/**
	 * This class helps open, create, and upgrade the database file. 
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {

			// calls the super constructor, requesting the default cursor factory.
			super(context, DBConstants.DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Creates the underlying database with table name and column names taken from the
		 * Buffer class.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + Building.TABLE_NAME + " ("
					+ Building._ID + " INTEGER PRIMARY KEY,"
					+ Building.COLUMN_CONDITION + " TEXT,"
					+ Building.COLUMN_CONSTRUCTION_TYPE + " TEXT,"
					+ Building.COLUMN_NAME + " TEXT,"
					+ Building.COLUMN_OF_FLOORS + " INTEGER,"
					+ Building.COLUMN_ROOF_TYPE + " TEXT,"
					+ Building.COLUMN_STATE + " TEXT"
					+ ");");
			//TODO add the rest of the tables needed for offline support
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// Logs that the database is being upgraded
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + Building.TABLE_NAME);
            onCreate(db);

		}
		
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// Logs that the database is being downgraded
			Log.w(TAG, "Downgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + Building.TABLE_NAME);
	        onCreate(db);

		}
	}

	/**
	 * Deletes records from the database. deletes the one record specified by
	 * the ID. Otherwise, it deletes a a set of records. If rows were deleted,
	 * then listeners are notified of the change.
	 * 
	 * @return If a "where" clause is used, the number of rows affected is
	 *         returned, otherwise 0 is returned.
	 * @throws IllegalArgumentException
	 *             if the incoming URI pattern is invalid.
	 */
	public int delete(String id, String where, String[] whereArgs, String tableName) {

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String finalWhere;

		int count;

		if (id == null) {

			// If matches the general pattern for Buffer, does a delete
			// based on the incoming "where" columns and arguments.
			count = db.delete(tableName, where, whereArgs);
		} else {
			/*
			 * Starts a final WHERE clause by restricting it to the desired
			 * weight ID.
			 */
			finalWhere = BaseColumns._ID + " = " + id;

			// If there were additional selection criteria, append them to the
			// final
			// WHERE clause
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}

			count = db.delete(tableName, finalWhere, whereArgs);
		}

		// Returns the number of rows deleted.
		return count;
	}

	public long insert(ContentValues initialValues, String tableName) {

		ContentValues values;

		// If the incoming values map is not null, uses it for the new values.
		if (initialValues != null) {
			values = new ContentValues(initialValues);

		} else {
			values = new ContentValues();
		}

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		// Performs the insert and returns the ID of the new weight.
		long rowId = db.insert(
				tableName,
				null, 
				values
		);

		// If the insert succeeded, the row ID exists.
		if (rowId > 0) {
			return rowId;
		}

		// If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
		throw new SQLException("Failed to insert row");

	}

	protected SQLiteQueryBuilder createQueryBuilder(){
		return new SQLiteQueryBuilder();
	}
	/**
	 * Queries the database and returns a cursor containing the results.
	 *
	 * @return A cursor containing the results of the query. The cursor exists but is empty if
	 * the query returns no results or an exception occurs.
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	public Cursor query(String id, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String tableName) {

		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = createQueryBuilder();
		qb.setTables(tableName);
		
		/**
		 * Choose the projection and adjust the "where" clause based on the id.
		 */
		if (id != null) {
			qb.appendWhere(
					BaseColumns._ID +    // the name of the ID column
					"='" +
					id+"'");
		}

		String orderBy;
		// If no sort order is specified, uses the default
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = null;
		} else {
			// otherwise, uses the incoming sort order
			orderBy = sortOrder;
		}

		// Opens the database object in "read" mode, since no writes need to be done.
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		return c;
	}


	public int update(String id, ContentValues values, String selection,
			String[] selectionArgs, String tableName) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		if (id == null) {
			count = db.update(tableName, values, selection,
					selectionArgs);
		} else {
			count = db.update(tableName, values, BaseColumns._ID
					+ "="
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
		}

		return count;
	}
	
	//TEST PORPUSE ONLY
	public static void setInstance(GreeAnalyticsDBProvider newInstance){
		GreeAnalyticsDBProvider.instance = newInstance;
	}
	public static void resetInstance(){
		GreeAnalyticsDBProvider.instance = null;
	}
	public void setDataBaseHelper(DatabaseHelper dbHelper){
		this.mOpenHelper = dbHelper;
	}
}
