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

Utils.applyCss = function (url) {
  var head = document.getElementsByTagName('head')[0];
  var link = document.createElement('link');
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = url;
  head.appendChild(link);
};

Utils.removeCss = function (url) {
  $('link[href="' + url + '"]').attr('disabled', 'true');
  $('link[href="' + url + '"]').remove();
};