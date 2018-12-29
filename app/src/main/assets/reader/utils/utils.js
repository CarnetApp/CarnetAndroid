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