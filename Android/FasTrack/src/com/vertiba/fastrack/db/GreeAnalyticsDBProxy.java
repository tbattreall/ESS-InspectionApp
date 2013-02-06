package com.vertiba.fastrack.db;

import android.app.Activity;
import android.content.ContentValues;

import com.vertiba.fastrack.db.DBConstants.Building;
import com.vertiba.fastrack.db.DBConstants.State;

public class GreeAnalyticsDBProxy {
	
	public static void initDatabase(Activity activity){
		GreeAnalyticsDBProvider.initInstance(activity);
	}
	
	public static void addBuilding(String Id, String condition, String constructionType, String name, String ofFloors) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(Building.COLUMN_CONDITION, condition);
		initialValues.put(Building.COLUMN_CONSTRUCTION_TYPE, constructionType);
		initialValues.put(Building.COLUMN_NAME, name);
		initialValues.put(Building.COLUMN_OF_FLOORS, ofFloors);
		initialValues.put(Building.COLUMN_ID, Id);
		initialValues.put(Building.COLUMN_ROOF_TYPE, Id);
		initialValues.put(Building.COLUMN_STATE, State.New.toString());
		GreeAnalyticsDBProvider.getInstance().insert(initialValues, Building.TABLE_NAME );
	}

}
