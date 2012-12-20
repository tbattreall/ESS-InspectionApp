package com.vertiba.fastrack.util;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.vertiba.fastrack.Constants.Preferences;

/**
 * Wrapper around the preferences
 *
 */
public class PreferencesManager {

	/**
	 * Sets a long preference
	 * @param key preference key
	 * @param value preference value
	 */
	public static void setPreference(String key, long value,Context context) {
		// Save user preferences.
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putLong(key, value);
		editor.commit();
	}
	
	/**
	 * Sets a boolean preference
	 * @param key preference key
	 * @param value preference value
	 */
	public static void setPreference(String key, boolean value,Context context) {
		// Save user preferences.
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	/**
	 * Sets an int preference
	 * @param key preference key
	 * @param value preference value
	 */
	public static void setPreference(String key, int value,Context context) {
		// Save user preferences.
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * Sets a String preference
	 * @param key preference key
	 * @param value preference value
	 */
	public static void setPreference(String key, String value,Context context) {
		// Save user preferences.
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Sets a String Set preference
	 * @param key preference key
	 * @param value preference value
	 */
	public static void setPreference(String key, Set<String> value,Context context) {
		// Save user preferences.
		SharedPreferences.Editor editor = getSharedPreferencesEditor(context);
		editor.putStringSet(key, value);
		editor.commit();
	}
	
	/**
	 * Gets the application {@link Editor}
	 * @return {@link Editor}
	 */
	public static SharedPreferences.Editor getSharedPreferencesEditor(Context context) {
		return context.getSharedPreferences(Preferences.PREFS_NAME, 0)
				.edit();
	}
	
	/**
	 * Gets the application {@link SharedPreferences}
	 * @return {@link SharedPreferences}
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(Preferences.PREFS_NAME, 0);
	}
}
