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
        	$j('#search_stores_div').show('30');
         });
    });
}

function fetchStores(){
    forcetkClient.query("SELECT Site_Number__c,Name,geo_latitude__c,geo_longitude__c,Direct_Dial_Phone__c,Site_Address_Line1__c,Site_City__c,Site_State_Province__c,Site_Zip_Code__c FROM Site__c WHERE Site_Number__c<>null and (geo_latitude__c>0 or geo_longitude__c>0) LIMIT 200", onSuccessFetchStores, onErrorSfdc); 
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
	map.getDiv().style.height = "15em";

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
    
    //Instantiate an info window.        
    var html = '<div>'+
    '<p>'+store.Site_Number__c+' '+store.Name +
    '</p></div>'+
    '<div>'+
    '<a class="ui-btn ui-shadow ui-btn-corner-all ui-mini ui-btn-inline ui-btn-up-c" data-theme="c" data-wrapperels="span" data-iconshadow="true" data-shadow="true" data-corners="true" data-role="button" data-inline="true" data-mini="true" onclick="addCachedStore(\''+store.Site_Number__c+'\')"><span class="ui-btn-inner ui-btn-corner-all"><span class="ui-btn-text">Add to Cache</span></span></a>'+
    '</div>';
    
    var infowindow = new google.maps.InfoWindow({
        content: html
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
}

function removeCachedStore(){
    var number = $j('#temp_data').data('store');
    $j("#li_store_"+number).remove();
}

function onSuccessFetchStores(response){
    var $j = jQuery.noConflict();
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: received " + response.totalSize + " stores");
    
    /*var tableData = {};
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
		    addStoreInMap(row);
			return '<a href="#" onclick="goToLocation(\''+row.geo_latitude__c+'\',\''+row.geo_longitude__c+'\')">Locate</a> <a href="#" onclick="addCachedStore(\''+row.Site_Number__c+'\')">Cache</a>';
		}
	}];
	//tableData.sScrollY = "10em";
	tableData.sScrollX = "100%";
	
	$j('#stores_table').dataTable(tableData);*/
	
	var list = "";
    $j.each( response.records, function( i, item ) {
    	addStoreInMap(item);
    	list += '<li><a href="#" onclick="goToLocation(\''+item.geo_latitude__c+'\',\''+item.geo_longitude__c+'\')">' + item.Site_Number__c +' '+ item.Name + '</a><a href="#" onclick="addCachedStore(\''+item.Site_Number__c+'\')">Cache</a></li>';
        });
    $j('#ul_searched_stores').empty().append( list ).listview("refresh");
    //for quick.pagination plugin
    //$j("#ul_searched_stores").quickPagination({pageSize:"5"});
    $j("#search_stores_div").pajinate({
		nav_label_first : '<<',
		nav_label_last : '>>',
		show_first_last: false,
		nav_label_prev : 'Prev',
		nav_label_next : 'Next',
		num_page_links_to_display : 4,
		items_per_page : 4,
		item_container_id : '#ul_searched_stores',
		nav_panel_id : '#search_page_navigation'
	});
    
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: finish loading " + response.totalSize + " stores");
}

function onErrorSfdc(error) {
    cordova.require("salesforce/util/logger").logToConsole("onErrorSfdc: " + JSON.stringify(error));
    alert('Error!');
}
