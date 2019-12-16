"use strict";

var Utils = function Utils() {};

Utils.keysrt = function (key, desc) {
  return function (a, b) {
    return desc ? ~~(a[key] < b[key]) : ~~(a[key] > b[key]);
  };
};

Utils.caseInsensitiveSrt = function (a, b) {
  return a.toLowerCase().localeCompare(b.toLowerCase());
};

Utils.sortByDefault = function (a, b) {
  return a.originalIndex < b.originalIndex ? -1 : 1;
};

Utils.sortByCreationDate = function (a, b) {
  if (a.metadata == undefined || b.metadata == undefined) return a.originalIndex < b.originalIndex ? -1 : 1;
  var dateA = a.metadata.last_modification_date;

  if (a.metadata.creation_date != undefined && a.metadata.creation_date !== "") {
    dateA = a.metadata.creation_date;
  }

  var dateB = b.metadata.last_modification_date;

  if (b.metadata.creation_date != undefined && b.metadata.creation_date !== "") {
    dateB = b.metadata.creation_date;
  }

  return dateA < dateB ? -1 : 1;
};

Utils.cleanNoteName = function (name) {
  if (name.startsWith('note$')) name = name.substring('note$'.length, name.length);
  return FileUtils.stripExtensionFromName(name);
};

Utils.sortByCustomDate = function (a, b) {
  if (a.metadata == undefined || b.metadata == undefined) return a.originalIndex < b.originalIndex ? -1 : 1;
  var dateA = a.metadata.custom_date;

  if (dateA == undefined || dateA == "") {
    dateA = a.metadata.creation_date;
  }

  if (dateA == undefined || dateA == "") {
    dateA = a.metadata.last_modification_date;
  }

  var dateB = b.metadata.custom_date;

  if (dateB == undefined) {
    dateB = b.metadata.creation_date;
  }

  if (dateB == undefined) {
    dateB = b.metadata.last_modification_date;
  }

  return dateA < dateB ? -1 : 1;
};

Utils.sortByModificationDate = function (a, b) {
  if (a.metadata == undefined || b.metadata == undefined) return a.originalIndex < b.originalIndex ? -1 : 1;
  var dateA = a.metadata.creation_date;

  if (a.metadata.last_modification_date != undefined) {
    dateA = a.metadata.last_modification_date;
  }

  var dateB = b.metadata.creation_date;

  if (b.metadata.last_modification_date != undefined) {
    dateB = b.metadata.last_modification_date;
  }

  return dateA < dateB ? -1 : 1;
};

Utils.httpReg = /(?:(?:https?|ftp|file):\/\/|www\.|ftp\.)(?:\([-A-Z0-9+&@#\/%=~_|$?!:,.]*\)|[-A-Z0-9+&@#\/%=~_|$?!:,.])*(?:\([-A-Z0-9+&@#\/%=~_|$?!:,.]*\)|[A-Z0-9+&@#\/%=~_|$])/igm;

Utils.srt = function (desc) {
  return function (a, b) {
    return desc ? ~~(a < b) : ~~(a > b);
  };
};

Utils.applyCss = function (url, onloaded) {
  //content is for nextcloud..
  var head = document.getElementById("content") != undefined ? document.getElementById("content") : document.getElementsByTagName('head')[0];
  var link = document.createElement('link');
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = url;

  if (onloaded != undefined) {
    link.addEventListener('load', function () {
      onloaded(url);
    });
    link.addEventListener('error', function () {
      onloaded(url);
    });
  }

  head.appendChild(link);
};

Utils.removeCss = function (url) {
  $('link[href="' + url + '"]').attr('disabled', 'true');
  $('link[href="' + url + '"]').remove();
};

Utils.generateUID = function () {
  // I generate the UID from two parts here
  // to ensure the random number provide enough bits.
  var firstPart = Math.random() * 46656 | 0;
  var secondPart = Math.random() * 46656 | 0;
  firstPart = ("000" + firstPart.toString(36)).slice(-3);
  secondPart = ("000" + secondPart.toString(36)).slice(-3);
  return firstPart + secondPart;
};

Utils.getParameterByName = function (name, url) {
  if (!url) {
    url = window.location.href;
  }

  name = name.replace(/[\[\]]/g, "\\$&");
  var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
      results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, " ").replace(/%2F/g, "/"));
};