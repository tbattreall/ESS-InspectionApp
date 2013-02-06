package com.vertiba.fastrack.db;

import android.provider.BaseColumns;

public class DBConstants {

	// This class cannot be instantiated
	private DBConstants() {}

	/**
	 * The database that the provider uses as its underlying data store
	 */
	public static final String DATABASE_NAME = "FasTrack.db";
	
	public static final int PRIORITY_HIGH = 100;
	public static final int PRIORITY_MEDIUM = 50;
	public static final int PRIORITY_LOW = 10;
	
	enum State {Update, New, Sync}
	
	/**
	 * Event table contract
	 */
	public static final class Building implements BaseColumns {

		/**
		 * The columns we are interested in from the database
		 */
		public static final String[] PROJECTION = new String[] {
			Building._ID
		};
		
		// This class cannot be instantiated
		private Building() {}

		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "Building__c";


		/*
		 * Column definitions
		 */
		public static final String COLUMN_ID = "Id";
		public static final String COLUMN_NAME = "Name";
		public static final String COLUMN_ROOF_TYPE = "Roof_Type__c";
		public static final String COLUMN_OF_FLOORS = "of_Floors__c";
		public static final String COLUMN_CONSTRUCTION_TYPE = "Construction_Type__c";
		public static final String COLUMN_CONDITION = "Condition__c";
		
		/**
		 * Column used to determine the state of the row (Update, New, Sync)
		 */
		public static final String COLUMN_STATE = "state";
		
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
		
	}

	//TODO Add the rest of the Tables definitions (try to use the same name for the table and for the fields than in salesforce to simplify the sync process)
	
}