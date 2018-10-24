"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Compatibility = function Compatibility() {
  _classCallCheck(this, Compatibility);

  this.isElectron = typeof require === "function";
  this.isAndroid = (typeof app === "undefined" ? "undefined" : _typeof(app)) === "object";
  console.log("is electron ?" + this.isElectron);

  if (this.isElectron) {
    RequestBuilder = ElectronRequestBuilder;
    console.log("set resquest builder");
  }
};