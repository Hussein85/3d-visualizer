$( document ).ready(function() {
  
  museumCookie = new Object();
  
  var keyValueStrings = $.cookie("museum").split("-")[1].split("&");
  $.each(keyValueStrings, function(i, keyValueString){
    var keyValue = keyValueString.split("=");
    museumCookie[keyValue[0]] = keyValue[1];
  }      
  ); 
  
});