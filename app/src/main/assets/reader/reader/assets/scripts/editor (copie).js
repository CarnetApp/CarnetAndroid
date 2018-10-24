"use strict";

var holdState = 1;
var font = 16;
var oDoc, sDefTxt, oEditor, oFloating;

function alertTest(string) {
  app.editImage(string);
}

function initDoc() {
  oEditor = document.getElementById("editor");
  $("#editor").webkitimageresize().webkittableresize().webkittdresize();
  oDoc = document.getElementById("text");
  oFloating = document.getElementById("floating");
  oDoc.addEventListener("input", function () {
    app.onTextChange(oEditor.innerHTML);
  }, false);
}

function loadText(txt) {
  oEditor.innerHTML = txt;
  oDoc = document.getElementById("text");
  oFloating = document.getElementById("floating");
  oDoc.addEventListener("input", function () {
    app.onTextChange(oEditor.innerHTML);
  }, false);
  sDefTxt = oDoc.innerHTML;
  /*simple initialization*/

  $("#editor").webkitimageresize().webkittableresize().webkittdresize();
}

function formatDoc(sCmd, sValue) {
  oEditor.focus();

  if (validateMode()) {
    document.execCommand(sCmd, false, sValue);
    oEditor.focus();
  }
}

function insertFloatingElement(string) {
  oFloating.innerHTML = oFloating.innerHTML + string;
}

function validateMode() {
  return true;
}

function setDocMode(bToSource) {
  var oContent;

  if (bToSource) {
    oContent = document.createTextNode(oDoc.innerHTML);
    oDoc.innerHTML = "";
    var oPre = document.createElement("pre");
    oDoc.contentEditable = false;
    oPre.id = "sourceText";
    oPre.contentEditable = true;
    oPre.appendChild(oContent);
    oDoc.appendChild(oPre);
  } else {
    if (document.all) {
      oDoc.innerHTML = oDoc.innerText;
    } else {
      oContent = document.createRange();
      oContent.selectNodeContents(oDoc.firstChild);
      oDoc.innerHTML = oContent.toString();
    }

    oDoc.contentEditable = true;
  }

  oDoc.focus();
}

function printDoc() {
  if (!validateMode()) {
    return;
  }

  var oPrntWin = window.open("", "_blank", "width=450,height=470,left=400,top=100,menubar=yes,toolbar=no,location=no,scrollbars=yes");
  oPrntWin.document.open();
  oPrntWin.document.write("<!doctype html><html><head><title>Print<\/title><\/head><body onload=\"print();\">" + oDoc.innerHTML + "<\/body><\/html>");
  oPrntWin.document.close();
}

document.getElementsByClassName = function (className, elmt) {
  var selection = new Array();
  var regex = new RegExp("\\b" + className + "\\b"); // le second argument, facultatif

  if (!elmt) elmt = document;else if (typeof elmt == "string") elmt = document.getElementById(elmt); // on sélectionne les éléments ayant la bonne classe

  var elmts = elmt.getElementsByTagName("*"); //document.getElementById('notes').innerHTML=elmts.length;

  for (var i = 0; i < elmts.length; i++) {
    if (regex.test(elmts[i].className)) selection.push(elmts[i]);
  }

  return selection;
};

function column(c) {
  texts = document.getElementsByClassName('text');
  document.getElementById('text').style.columnCount = c;
  document.getElementById('text').style.MozColumnCount = c;
  document.getElementById('text').style.WebkitColumnCount = c;
}

function hold() {
  holdState = 1;
  document.getElementById('buttonHide').innerHTML = "<img src='images/go-first.png' /><br />hide sidebar</a> <br />";
  document.getElementById('buttonHide').onclick = hide;
  document.getElementById('text').style.marginLeft = "90px";
  document.getElementById('leftBar').style.display = "inline";
  document.getElementById('leftBar').style.position = "fixed";
  document.getElementById('leftAppearBar').style.display = "none";
  return false;
}

function hide() {
  holdState = 0;
  document.getElementById('buttonHide').innerHTML = "<img src='images/go-last.png' /><br />hold sidebar</a> <br />";
  document.getElementById('buttonHide').onclick = hold;
  document.getElementById('leftBar').style.display = "none";
  document.getElementById('leftBar').style.position = "fixed";
  document.getElementById('text').style.marginLeft = "0";
  document.getElementById('leftAppearBar').style.display = "inline";
  return false;
}

function appear() {
  if (holdState == 0) document.getElementById('leftBar').style.display = "inline";
}

function disappear() {
  if (holdState == 0) document.getElementById('leftBar').style.display = "none";
}

function next() {
  $('#text').animate({
    scrollLeft: $('#text').scrollLeft() + $('#text').width() + 20
  }, 100);
}

function previous() {
  // document.getElementById('text').scrollIntoView(true);
  $('#text').animate({
    scrollLeft: $('#text').scrollLeft() - $('#text').width()
  }, 100);
}

function scrolling(sc) {
  if (sc == 0) {
    document.getElementById('text').style.overflowY = "auto";
    document.getElementById('text').style.minHeight = "100%";
  }
}

document.onkeydown = function (evt) {
  evt = evt || window.event;

  switch (evt.keyCode) {
    case 37:
      previous();
      break;

    case 39:
      next();
      break;
  }
};

function reloadImage(imgId, path) {
  document.getElementById(imgId).src = path + "?rand_number=" + Math.random();
}

function deleteImage(imgId) {
  alert("" + imgId);
  element = document.getElementById(imgId);
  element.parentNode.removeChild(element);
}

function notify(text) {
  document.getElementById('notification').style.display = "inline";
  document.getElementById('notification').innerHTML = text;
  setTimeout(function () {
    document.getElementById('notification').style.display = "none";
  }, 5000);
}