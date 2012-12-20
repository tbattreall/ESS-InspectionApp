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

function onSuccessFetchStores(response){
    var $j = jQuery.noConflict();
    cordova.require("salesforce/util/logger").logToConsole("onSuccessSfdcContacts: received " + response.totalSize + " stores");
    
    $j("#div_stores_list").html("")
    var ul = $j('<ul data-role="listview" data-inset="true" data-theme="a" data-dividertheme="a"></ul>');
    $j("#div_stores_list").append(ul);
    
    ul.append($j('<li data-role="list-divider">Stores: ' + response.totalSize + '</li>'));
    $j.each(response.records, function(i, contact) {
           var newLi = $j("<li><a href='#'>" + (i+1) + " - " + contact.Name + "</a></li>");
           ul.append(newLi);
           });
    
    $j("#div_stores_list").trigger( "create" )
}

function onErrorSfdc(error) {
    cordova.require("salesforce/util/logger").logToConsole("onErrorSfdc: " + JSON.stringify(error));
    alert('Error!');
}
