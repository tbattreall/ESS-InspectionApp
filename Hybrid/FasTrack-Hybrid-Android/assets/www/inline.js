function regLinkClickHandlers() {
    var $j = jQuery.noConflict();
    var logToConsole = cordova.require("salesforce/util/logger").logToConsole;          
    $j('#link_logout').click(function() {
             logToConsole("link_logout clicked");
             var sfOAuthPlugin = cordova.require("salesforce/plugin/oauth");
             sfOAuthPlugin.logout();
             });
    
    $j('#cached_stores').click(function() {
        logToConsole("cached_stores clicked");
        $j('#search_stores_div').hide('fast', function() {
        	 $j('#cached_stores').css("ui-btn-active ui-state-persist");
             $j('#search_stores').css("");
        	$j('#cached_stores_div').show('30');
         });
    });
    $j('#search_stores').click(function() {
        logToConsole("search_stores clicked");
        $j('#cached_stores_div').hide('fast', function() {
        	$j('#cached_stores').css("");
            $j('#search_stores').css("ui-btn-active ui-state-persist");
        	$j('#search_stores_div').show('30',function() {
        		$j('#stores_scroll_div').iscrollview("refresh");
        	});
         });
    });
    $j('#search_stores_input').bind("change", function(event, ui) {
        fetchStores();
    });
}

var STORES_SOUP_NAME = "ct__storesSoup";

function regOfflineSoups() {

    // Registering soup 1 for storing stores
    var indexesStores = [ {
        path : "Name",
        type : "string"
    }, {
        path : "Site_Number__c",
        type : "string"
    }, {
        path : "Site_Address_Line1__c",
        type : "string"
    } ];
    
//    navigator.smartstore.removeSoup(STORES_SOUP_NAME, function(){
//        SFHybridApp.logToConsole("Soup Removed: "+record);
//        }, logError);

    navigator.smartstore.registerSoup(STORES_SOUP_NAME, indexesStores,
            function() {
                SFHybridApp.logToConsole(STORES_SOUP_NAME + " Soup Registered")
            }, logError);

}

function fetchStores(){
    navigator.smartstore.soupExists(STORES_SOUP_NAME, onStoreSoupExist, logError);
}

function onStoreSoupExist(exist){
    if(exist){
        updateStoreList();
    } else {
        forcetkClient.query("SELECT Site_Number__c,Name,geo_latitude__c,geo_longitude__c,Direct_Dial_Phone__c,Site_Address_Line1__c,Site_City__c,Site_State_Province__c,Site_Zip_Code__c FROM Site__c WHERE Site_Number__c<>null and (geo_latitude__c>0 or geo_longitude__c>0) LIMIT 200", onSuccessFetchStores, onErrorSfdc);
        regOfflineSoups();
    }
}

var map = null;

function loadPosition(){
	var geoMap = document.querySelector('#geo-map');
	map = new google.maps.Map(geoMap, {
	          zoom: 3,
	          center: new google.maps.LatLng(37.4419, -94.1419), // United States
	          mapTypeId: google.maps.MapTypeId.ROADMAP
	        });
	map.getDiv().style.border =  '1px solid #ccc';
	var height = $(window).height();
	map.getDiv().style.height = height - 60 + "px";

	navigator.geolocation.getCurrentPosition(showCurrentPosition);
}


var currentCoordinate = null;
/**
 * Shows the position in the map
 */
function showCurrentPosition(position) {
	currentCoordinate = position.coords;
	fetchStores();
	goToLocation(position.coords.latitude, position.coords.longitude);
}

function distanceToCurrentLocation(lat1,lon1){
	if(currentCoordinate){
		var unit = "K";// K, M or N
		var lat2 = currentCoordinate.latitude;
		var lon2 = currentCoordinate.longitude;
		var radlat1 = Math.PI * lat1/180
		var radlat2 = Math.PI * lat2/180
		var radlon1 = Math.PI * lon1/180
		var radlon2 = Math.PI * lon2/180
		var theta = lon1-lon2
		var radtheta = Math.PI * theta/180
		var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
		dist = Math.acos(dist)
		dist = dist * 180/Math.PI
		dist = dist * 60 * 1.1515
		if (unit=="K") { dist = dist * 1.609344 }
		if (unit=="N") { dist = dist * 0.8684 }
		return dist
	}
	return 0;
}

function goToLocation(latitude, longitude){
  var latLng = new google.maps.LatLng(latitude, longitude);
  map.setCenter(latLng);
  map.setZoom(12);
}

function addStoreInMap(store){
    var latLng = new google.maps.LatLng(store.geo_latitude__c, store.geo_longitude__c);
    var marker = new google.maps.Marker({
      position: latLng,
      title: store.Site_Number__c,
      map: map
    });
    
    var info_data = {
            name: store.Name,
            number: store.Site_Number__c
          };

    // Here's all the magic.
    var info = ich.mapInfoWindow(info_data);
    info = $j('#temp_data').append(info).trigger("create").html();
    $j('#temp_data').empty();
    var infowindow = new google.maps.InfoWindow({
        content: info
    });
    
    google.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map,marker);
    });
}

function addCachedStore(number){
    if(number=="null") { alert("No number"); return; }
    if(document.getElementById("li_store_"+number)!=null) return;
    var newLi = $j('<li id="li_store_'+ number +'"><a href="#">' + "Store " + number + '</a><a href="#popupRemoveStoreDialog" data-position-to="window" data-rel="popup" data-icon="delete" onclick=\'$j("#temp_data").data("store","'+number+'")\'></a></li>');
    $j("#ul_cached_stores").append(newLi);
    $j("#ul_cached_stores").listview('refresh');
    $j('#stores_scroll_div').iscrollview("refresh");
}

function removeCachedStore(){
    var number = $j('#temp_data').data('store');
    $j("#li_store_"+number).remove();
}

function onSuccessFetchStores(response){
    var $j = jQuery.noConflict();
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: received " + response.totalSize + " stores");
    
    navigator.smartstore.upsertSoupEntries(STORES_SOUP_NAME,response.records, updateStoreList, logError);
    
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: finish loading " + response.totalSize + " stores");
}

function refreshListUI(cursor){
    var curPageEntries = cursor.currentPageOrderedEntries;
    navigator.smartstore.closeCursor(cursor);
    var list = "";
    curPageEntries.sort(function(a,b){
		return distanceToCurrentLocation(a.geo_latitude__c,a.geo_longitude__c)-distanceToCurrentLocation(b.geo_latitude__c,b.geo_longitude__c);
	});
    $j.each(curPageEntries, function(i, item) {
        addStoreInMap(item);
        list += '<li><a href="#" onclick="goToLocation(\''+item.geo_latitude__c+'\',\''+item.geo_longitude__c+'\')">' + item.Site_Number__c +' '+ item.Name + '</a><a href="#" data-icon="add" onclick="addCachedStore(\''+item.Site_Number__c+'\')">Cache</a></li>';
        });
    $j('#ul_searched_stores').empty().append( list ).listview("refresh");
}

function updateStoreList(){
    //get the search string from the search view
    var search = $j('#search_stores_input').val();
    var querySpec = navigator.smartstore.buildLikeQuerySpec("Name", "%"+search+"%", null, 20);
    
    navigator.smartstore.querySoup(STORES_SOUP_NAME, querySpec, refreshListUI, logError);
}

/**
* Error Received
**/
function logError(error) {
    SFHybridApp.logToConsole("Error: " + JSON.stringify(error,null,'<br>'));
}

function onErrorSfdc(error) {
    cordova.require("salesforce/util/logger").logToConsole("onErrorSfdc: " + JSON.stringify(error));
    alert('Error!');
}
