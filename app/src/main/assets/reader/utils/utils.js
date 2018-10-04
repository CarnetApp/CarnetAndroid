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