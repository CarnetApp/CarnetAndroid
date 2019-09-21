"use strict";

{
  var token = document.getElementById("token").innerHTML;
  $(document).on('ajaxSend', function (elm, xhr, settings) {
    if (settings.crossDomain === false) {
      xhr.setRequestHeader('requesttoken', token);
      xhr.setRequestHeader('OCS-APIREQUEST', 'true');
    }
  });
}