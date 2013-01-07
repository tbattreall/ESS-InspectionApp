function regLinkClickHandlers() {
    var $j = jQuery.noConflict();
    var logToConsole = cordova.require("salesforce/util/logger").logToConsole;          
    $j('#link_logout').click(function() {
             logToConsole("link_logout clicked");
             var sfOAuthPlugin = cordova.require("salesforce/plugin/oauth");
             sfOAuthPlugin.logout();
             });
    
}

function fetchBuildings(){
    var store_number = $j('#temp_data').data('store');
    forcetkClient.query("SELECT Name, Roof_Type__c,of_Floors__c, Construction_Type__c FROM Building__c WHERE Name LIKE '"+store_number+"%'", onSuccessFetchBuildings, logError);
}

function onSuccessFetchBuildings(response){
    var $j = jQuery.noConflict();
    cordova.require("salesforce/util/logger").logToConsole("onSuccessFetchBuildings: received " + response.totalSize + " buildings");
    
    refreshListUI(response.records);
    
    cordova.require("salesforce/util/logger").logToConsole("onSuccessFetchBuildings: finish loading " + response.totalSize + " buildings");
}

function refreshListUI(records){
    var list = "";
    $j.each(records, function(i, item) {
        var building_data = {
                number: 'Which is this?',
                name: item.Name,
                b_type: item.Construction_Type__c,
                r_type: item.Roof_Type__c,
                condition: 'Which is this?',
                floors: item.of_Floors__c
              };
         $j('#ul_buildings').append(ich.buildings_list_entry(building_data));
        });
    $j('#ul_buildings').listview("refresh");
}

/**
* Error Received
**/
function logError(error) {
    SFHybridApp.logToConsole("Error: " + JSON.stringify(error,null,'<br>'));
}

$j(document).bind("pageinit", function(event, data) {
    ich.grabTemplates();
//    var store_data = {
//            number: $j('#temp_data').data('store'),
//            name: 'name'
//          };
//    $j("#buildings_header").replaceWith(ich.title_stores(store_data).html());
//    $j('#buildings_header').trigger('create');
    regLinkClickHandlers();
    fetchBuildings();
});