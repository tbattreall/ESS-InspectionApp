/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.vertiba.fastrack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.NativeMainActivity;

/**
 * Main activity
 */
public class MainActivity extends NativeMainActivity {

    private RestClient client;
    private ArrayAdapter<SiteModel> listAdapter;
    private GoogleMap mMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ForceApp.APP.getLoginServerManager().useSandbox();
		// Setup view
		setContentView(R.layout.main);
	}
	
	@Override 
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		// Create list adapter
		listAdapter = new ArrayAdapter<SiteModel>(this, android.R.layout.simple_list_item_1, new ArrayList<SiteModel>());
		ListView listView = (ListView) findViewById(R.id.lvStores);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) {
			    SiteModel site = ((ArrayAdapter<SiteModel>)parent.getAdapter()).getItem(position);
			    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(site.position, 12));
			    Log.d("MainActivity", "Going to location: "+ site.number +" with location: "+ site.position.toString());
			  }
			}); 
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
        this.client = client; 

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
		
		try {
			setUpMapIfNeeded();
			fetchStores();
		} catch (UnsupportedEncodingException e) {
			Log.e("MainActivity","Error loading stores",e);
		}
	}

	/**
	 * Called when "Logout" button is clicked. 
	 * 
	 * @param v
	 */
	public void onLogoutClick(View v) {
		ForceApp.APP.logout(this);
	}
	
	/**
	 * Called when "Clear" button is clicked. 
	 * 
	 * @param v
	 */
	public void onClearClick(View v) {
		listAdapter.clear();
	}	

	/**
	 * 
	 * @throws UnsupportedEncodingException 
	 */
	public void fetchStores() throws UnsupportedEncodingException {
		sendRequest("SELECT Site_Number__c,Name,geo_latitude__c,geo_longitude__c FROM Site__c");
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

		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Log.i("MainActivity","Location: "+lastKnownLocation.toString());
		
		SiteModel.currentPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
		
		mMap.addMarker(new MarkerOptions()
                .position(SiteModel.currentPosition)
                .title("Your Location")
                .snippet("This is your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
	   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SiteModel.currentPosition, 12));	
	}

	private void sendRequest(String soql) throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(getString(R.string.api_version), soql);
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				try {
					listAdapter.clear();
					
					JSONArray records = result.asJSONObject().getJSONArray("records");
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
				} catch (Exception e) {
					onError(e);
				}
			}
			
			@Override
			public void onError(Exception exception) {
                Toast.makeText(MainActivity.this,
                               MainActivity.this.getString(ForceApp.APP.getSalesforceR().stringGenericError(), exception.toString()),
                               Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private static class SiteModel implements Comparable<SiteModel>{
		static LatLng currentPosition;
		String number;
		String name;
		LatLng position;
		double distance;
		public SiteModel(JSONObject siteJson) throws JSONException{
			 number = siteJson.getString("Site_Number__c");
			 name = siteJson.getString("Name");
			 position = new LatLng(siteJson.getDouble("geo_latitude__c"), siteJson.getDouble("geo_longitude__c"));
			 distance = calculateDistance(position.latitude,position.longitude,currentPosition.latitude,currentPosition.longitude);
		}
		@Override
		public String toString(){
			return number + " - " +name;
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
