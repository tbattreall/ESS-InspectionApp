function regLinkClickHandlers() {
    var $j = jQuery.noConflict();
    var logToConsole = cordova.require("salesforce/util/logger").logToConsole;          
    $j('#link_logout').click(function() {
             logToConsole("link_logout clicked");
             var sfOAuthPlugin = cordova.require("salesforce/plugin/oauth");
             sfOAuthPlugin.logout();
             });
}

function fetchStores(){
    forcetkClient.query("SELECT Site_Number__c,Name,geo_latitude__c,geo_longitude__c,Direct_Dial_Phone__c,Site_Address_Line1__c,Site_City__c,Site_State_Province__c,Site_Zip_Code__c FROM Site__c", onSuccessFetchStores, onErrorSfdc); 
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
	map.getDiv().style.height = "10em";

	navigator.geolocation.getCurrentPosition(showPosition);
}

/**
 * Shows the position in the map
 */
function showPosition(position) {
	goToLocation(position.coords.latitude, position.coords.longitude);
}

function goToLocation(latitude, longitude){
  var latLng = new google.maps.LatLng(latitude, longitude);
  var marker = new google.maps.Marker({
    position: latLng,
    map: map
  });
  map.setCenter(latLng);
  map.setZoom(12);
}

function addCachedStore(number){
    if(number==null) number = "No number";
    var newLi = $j("<li><a href='#'>" + "Store " + number + "</a></li>");
    $j("#ul_cached_stores").append(newLi);
    $j("#ul_cached_stores").listview('refresh');
}

function onSuccessFetchStores(response){
    var $j = jQuery.noConflict();
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: received " + response.totalSize + " stores");
    
    var tableData = {};
	tableData.aaData = response.records;
	tableData.aoColumns = [{
		"mData" : "Site_Number__c"
	}, {
		"mData" : "Name"
	}, {
		"mData" : "Name",
		"bSearchable": false,
		"bSortable": false,
		"mRender" : function(data, type, row) {
			return '<a href="#" onclick="goToLocation(\''+row.geo_latitude__c+'\',\''+row.geo_longitude__c+'\')">Locate</a> <a href="#" onclick="addCachedStore(\''+row.Site_Number__c+'\')">Cache</a>';
		}
	}];
	//tableData.sScrollY = "10em";
	tableData.sScrollX = "100%";

	$j('#stores_table').dataTable(tableData);
}

function onErrorSfdc(error) {
    cordova.require("salesforce/util/logger").logToConsole("onErrorSfdc: " + JSON.stringify(error));
    alert('Error!');
}
