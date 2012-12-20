package com.vertiba.fastrack;


/**
 * Class to hold all of the constants used in the integration
 * 
 * @author jorgecapra
 * 
 */
public class Constants {

	public static class Preferences {
		public final static String PREFS_NAME = "FASTRACK_MOBILE";
		public final static String CACHED_STORES = "CACHED_STORES";
	}
	
	/**
	 * Keys used to parse the server JSON responses
	 * 
	 * @author jorgecapra
	 * 
	 */
	public static class JsonKeys {
		public final static String LOCATION = "location";
	}
	
	/**
	 * Attribute names used in the API calls
	 * 
	 * @author jorgecapra
	 * 
	 */
	public static class Attributes {
		public final static String TOKEN = "token";
		public final static String IDS = "ids";
	}
	
	public static boolean DEBUG = true;
}
