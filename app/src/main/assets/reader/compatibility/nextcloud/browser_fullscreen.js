{
  console.log("hidden");
  var right2 = document.getElementById("right-bar");
  var settings = document.getElementById("settings");
  settings.parentNode.removeChild(settings);
  right2.appendChild(settings);
  var header = document.getElementById("header");
  header.parentNode.removeChild(header);
  var nc_content = document.getElementById("content-wrapper");
  if (nc_content == null) nc_content = document.getElementById("content");
  nc_content.style.paddingTop = "0px";
  var ex = document.getElementById("expanddiv");
  ex.style.top = "50px";
  ex.style.position = "absolute";
  ex.style.right = "0px";
  ex.style.background = "white";
  console.log("hidden");
}