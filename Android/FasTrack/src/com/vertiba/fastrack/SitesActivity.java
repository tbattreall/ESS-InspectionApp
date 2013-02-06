package com.vertiba.fastrack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.store.SmartStore;
import com.salesforce.androidsdk.store.SmartStore.IndexSpec;
import com.salesforce.androidsdk.store.SmartStore.Order;
import com.salesforce.androidsdk.store.SmartStore.QuerySpec;
import com.salesforce.androidsdk.store.SmartStore.Type;
import com.salesforce.androidsdk.ui.NativeMainActivity;
import com.vertiba.fastrack.Constants.Preferences;
import com.vertiba.fastrack.salesforce.SalesforceManager;
import com.vertiba.fastrack.salesforce.SalesforceManager.RequestAndStoreCallback;
import com.vertiba.fastrack.util.PreferencesManager;

public class SitesActivity extends NativeMainActivity{

    private ArrayAdapter<SiteModel> listAdapter;
    private ArrayAdapter<String> favoriteAdapter;
    private GoogleMap mMap;
	private SmartStore smartStore;
	
	private static final String TAG = "Sites";
	private static final String STORES_SOUP_NAME = "Sites";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ForceApp.APP.getLoginServerManager().useSandbox();
		setContentView(R.layout.main_screen);
		smartStore = FasTrackMobileApp.getAppInstance().getSmartStore();
	}
	
	@Override 
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		// Create list adapter
		favoriteAdapter = new FavoriteAdapter(this, R.layout.favorite_site_item);
		ListView favoriteList = (ListView) findViewById(R.id.lvFavoriteStores);
		favoriteList.setAdapter(favoriteAdapter);
		listAdapter = new SiteAdapter(this, R.layout.site_item, new ArrayList<SiteModel>());
		
		ListView listView = (ListView) findViewById(R.id.lvStores);
		listView.setAdapter(listAdapter);
		OnItemClickListener onItemClickListener = new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) {
			    SiteModel site = ((ArrayAdapter<SiteModel>)parent.getAdapter()).getItem(position);
			    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(site.position, 12));
			    Log.d("MainActivity", "Going to location: "+ site.number +" with location: "+ site.position.toString());
			  }
			};
		listView.setOnItemClickListener(onItemClickListener); 
		super.onResume();
	}
	
	@Override
	protected LoginOptions getLoginOptions() {
    	LoginOptions loginOptions = new LoginOptions(
    			null, // login host is chosen by user through the server picker 
    			ForceApp.APP.getPasscodeHash(),
    			getString(R.string.oauth_callback_url),
    			getString(R.string.oauth_client_id),
    			new String[] {"api"});
    	return loginOptions;
	}
	
	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        SalesforceManager.setClient(client); 

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
		
		try {
			setUpMapIfNeeded();
			if(!smartStore.hasSoup(STORES_SOUP_NAME)){
				fetchStores();
			} else {
				QuerySpec query = QuerySpec.buildAllQuerySpec(Order.ascending, Integer.MAX_VALUE);
				try {
					JSONArray querySoup = smartStore.querySoup(STORES_SOUP_NAME, query, 0);
					populateStoreList(querySoup.getJSONObject(0));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			populateFavoriteList();
		} catch (UnsupportedEncodingException e) {
			Log.e("MainActivity","Error loading stores",e);
		}
	}
	
	/**
	 * 
	 * @throws UnsupportedEncodingException 
	 */
	public void fetchStores() throws UnsupportedEncodingException {
        SalesforceManager.sendRequestAndStoreResponse(this, "SELECT Id,Site_Number__c,Name,geo_latitude__c,geo_longitude__c,Direct_Dial_Phone__c,Site_Address_Line1__c,Site_City__c,Site_State_Province__c,Site_Zip_Code__c FROM Site__c"
        		, STORES_SOUP_NAME, new IndexSpec[]{new IndexSpec("Site_Number__c",Type.string)}, new RequestAndStoreCallback() {
					
					@Override
					public void onComplete(JSONObject asJSONObject) {
						try {
							populateStoreList(asJSONObject);
						} catch (JSONException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				});
	}
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
        	FragmentManager fragmentManager = this.getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.map);
        	mMap = ((MapFragment) fragment).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
	
	private void setUpMap() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		if (lastKnownLocation != null) {
			Log.i("MainActivity", "Location: " + lastKnownLocation.toString());

			SiteModel.currentPosition = new LatLng(
					lastKnownLocation.getLatitude(),
					lastKnownLocation.getLongitude());

			mMap.addMarker(new MarkerOptions()
					.position(SiteModel.currentPosition)
					.title("Your Location")
					.snippet("This is your location")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					SiteModel.currentPosition, 12));
		}
	}
	
	private void populateFavoriteList() {
		SharedPreferences preferences = PreferencesManager.getSharedPreferences(this);
		Set<String> set = preferences.getStringSet(Preferences.CACHED_STORES, new HashSet<String>());
		favoriteAdapter.clear();
		for (String number : set) {
			favoriteAdapter.add(number);
		}
	}
	
	private void populateStoreList(JSONObject object) throws JSONException{
		listAdapter.clear();
		JSONArray records = object.getJSONArray("records");
		for (int i = 0; i < records.length(); i++) {
			JSONObject jsonObject = records.getJSONObject(i);
			SiteModel site = new SiteModel(jsonObject); 
			if(!site.number.isEmpty() && !site.number.equals("null") && 
					(site.position.latitude!=0 || site.position.longitude!=0)){ //just adding complete sites
			listAdapter.add(site);
				if(mMap!=null){
					mMap.addMarker(new MarkerOptions()
						.position(site.position)
						.title(site.number)
						.snippet(site.name)
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				}
			}
		}
		listAdapter.sort(new Comparator<SiteModel>() {
			@Override
			public int compare(SiteModel left, SiteModel right) {
				return left.compareTo(right);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the options menu from XML
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.sites, menu);

	    // Get the SearchView and set the searchable configuration
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    menu.findItem(R.id.menu_logout).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				ForceApp.APP.logout(SitesActivity.this);
				return true;
			}
		});
	    menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				try {
					fetchStores();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return true;
			}
		});
	    
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				listAdapter.getFilter().filter(arg0);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				listAdapter.getFilter().filter(arg0);
				return true;
			}
		});

	    return true;
	}
	
	private class FavoriteAdapter extends ArrayAdapter<String> {

		public FavoriteAdapter(SitesActivity sitesActivity, int siteItem) {
			super(sitesActivity, siteItem);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.favorite_site_item, null);
			final String item = getItem(position);
			((TextView)view.findViewById(R.id.tvNumber)).setText("Store "+item);
			view.findViewById(R.id.ivDelete).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					SharedPreferences preferences = PreferencesManager.getSharedPreferences(SitesActivity.this);
					Set<String> set = preferences.getStringSet(Preferences.CACHED_STORES, new HashSet<String>());
					if (set.contains(item)) {
						Set<String> newSet = new HashSet<String>(set);
						newSet.remove(item);
						PreferencesManager.setPreference(Preferences.CACHED_STORES, newSet, SitesActivity.this);
					}
					populateFavoriteList();
				}
			});
			return view;
		}
	}
	
	private class SiteAdapter extends ArrayAdapter<SiteModel> {

		public SiteAdapter(SitesActivity sitesActivity, int siteItem,
				ArrayList<SiteModel> arrayList) {
			super(sitesActivity, siteItem, arrayList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.site_item, null);
			final SiteModel item = getItem(position);
			((TextView)view.findViewById(R.id.tvName)).setText(item.name);
			((TextView)view.findViewById(R.id.tvAddress)).setText(item.address+", "+item.city+", "+item.state+" "+item.zipCode);
			((TextView)view.findViewById(R.id.tvNumber)).setText(item.number);
			((TextView)view.findViewById(R.id.tvPhone)).setText(item.phone);
			view.findViewById(R.id.ivAdd).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					addCacheStore(item);
					populateFavoriteList();
				}
			});
			return view;
		}
	}
	
	private void addCacheStore(SiteModel item){
		SharedPreferences preferences = PreferencesManager.getSharedPreferences(SitesActivity.this);
		Set<String> set = preferences.getStringSet(Preferences.CACHED_STORES, new HashSet<String>());
		if (!set.contains(item.number)) {
			Set<String> newSet = new HashSet<String>(set);
			newSet.add(item.number);
			PreferencesManager.setPreference(Preferences.CACHED_STORES, newSet, SitesActivity.this);
			//TODO Cache all the necessary info from salesforce for this particular store
		}
	}
	
	private static class SiteModel implements Comparable<SiteModel>{
		static LatLng currentPosition;
		String id;
		String number;
		String name;
		LatLng position;
		double distance;
		String address;
		String city;
		String state;
		String phone;
		String zipCode;
		
		public SiteModel(JSONObject siteJson) throws JSONException {
			id = siteJson.getString("Id");
			number = siteJson.getString("Site_Number__c");
			name = siteJson.getString("Name");
			phone = siteJson.getString("Direct_Dial_Phone__c");
			address = siteJson.getString("Site_Address_Line1__c");
			city = siteJson.getString("Site_City__c");
			state = siteJson.getString("Site_State_Province__c");
			zipCode = siteJson.getString("Site_Zip_Code__c");
			position = new LatLng(siteJson.getDouble("geo_latitude__c"), siteJson.getDouble("geo_longitude__c"));
			if (currentPosition != null) {
				 distance = calculateDistance(position.latitude,position.longitude,currentPosition.latitude,currentPosition.longitude);
			 }
		}
		@Override
		public String toString(){
			return number + " " + name + address + " " + city + " " + state + " " + zipCode;
		}
		@Override
		public int compareTo(SiteModel another) {
			return new Double(this.distance).compareTo(another.distance);
		}
		
		private double calculateDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
		    float pk = (float) (180/3.14169);

		    float a1 = (float) (lat_a / pk);
		    float a2 = (float) (lng_a / pk);
		    float b1 = (float) (lat_b / pk);
		    float b2 = (float) (lng_b / pk);

		    float t1 = FloatMath.cos(a1)*FloatMath.cos(a2)*FloatMath.cos(b1)*FloatMath.cos(b2);
		    float t2 = FloatMath.cos(a1)*FloatMath.sin(a2)*FloatMath.cos(b1)*FloatMath.sin(b2);
		    float t3 = FloatMath.sin(a1)*FloatMath.sin(b1);
		    double tt = Math.acos(t1 + t2 + t3);

		    return 6366000*tt;
		}
		
	}
}
