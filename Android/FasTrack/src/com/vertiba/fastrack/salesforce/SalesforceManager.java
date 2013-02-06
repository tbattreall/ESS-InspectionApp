package com.vertiba.fastrack.salesforce;

import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

import android.app.Activity;
import android.widget.Toast;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.store.SmartStore;
import com.salesforce.androidsdk.store.SmartStore.IndexSpec;
import com.vertiba.fastrack.FasTrackMobileApp;
import com.vertiba.fastrack.R;

public class SalesforceManager {

	private static RestClient client;
	
	public static void setClient(RestClient client){
		SalesforceManager.client = client;
	}
	
	public interface RequestAndStoreCallback {
		public void onComplete(JSONObject asJSONObject);
	}
	
	public static void sendRequestAndStoreResponse(final Activity activity,
			String soql, final String soupName, final IndexSpec[] indexes, final RequestAndStoreCallback callback)
			throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(activity.getString(R.string.api_version), soql);
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				try {
					SmartStore smartStore = FasTrackMobileApp.getAppInstance().getSmartStore();
					JSONObject asJSONObject = result.asJSONObject();
					smartStore.registerSoup(soupName, indexes );
					smartStore.upsert(soupName, asJSONObject);
					if(callback != null){
						callback.onComplete(asJSONObject);
					}
				} catch (Exception e) {
					onError(e);
				}
			}
			
			@Override
			public void onError(Exception exception) {
                Toast.makeText(activity,
                		activity.getString(ForceApp.APP.getSalesforceR().stringGenericError(), exception.toString()),
                               Toast.LENGTH_LONG).show();
			}
		});
	}
	
	
}
