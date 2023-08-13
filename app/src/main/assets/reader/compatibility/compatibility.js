"use strict";

function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, _toPropertyKey(descriptor.key), descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _toPropertyKey(arg) { var key = _toPrimitive(arg, "string"); return _typeof(key) === "symbol" ? key : String(key); }
function _toPrimitive(input, hint) { if (_typeof(input) !== "object" || input === null) return input; var prim = input[Symbol.toPrimitive]; if (prim !== undefined) { var res = prim.call(input, hint || "default"); if (_typeof(res) !== "object") return res; throw new TypeError("@@toPrimitive must return a primitive value."); } return (hint === "string" ? String : Number)(input); }
var compatRequire = undefined;
var Compatibility = /*#__PURE__*/function () {
  function Compatibility() {
    _classCallCheck(this, Compatibility);
    this.isElectron = typeof require === "function" || typeof parent.require === "function";
    console.log("this.isElectron  " + this.isElectron);
    if (this.isElectron) {
      if (typeof require !== "function") {
        compatRequire = parent.require;
      } else compatRequire = require;
    }
    this.isAndroid = (typeof app === "undefined" ? "undefined" : _typeof(app)) === "object";
    this.isGtk = false;
    console.log("isAndroid" + this.isAndroid);
    if (this.isElectron) {
      RequestBuilder = ElectronRequestBuilder;
      console.log("set resquest builder");
    }
    if (this.isAndroid) {
      $(document).on('ajaxSend', function (elm, xhr, settings) {
        if (settings.crossDomain === false) {
          xhr.setRequestHeader('requesttoken', app.getRequestToken());
          xhr.setRequestHeader('OCS-APIREQUEST', 'true');
        }
      });
    }
  }
  _createClass(Compatibility, [{
    key: "addRequestToken",
    value: function addRequestToken(url) {
      if (this.isAndroid) {
        if (url.indexOf("?") > -1) url += "&";else url += "?";
        url += "requesttoken=" + app.getRequestToken();
      }
      return url;
    }
  }, {
    key: "openUrl",
    value: function openUrl(url) {
      if (compatibility.isElectron) {
        var _require = require('electron'),
          shell = _require.shell;
        shell.openExternal(url);
      } else if (compatibility.isAndroid) {
        app.openUrl(url);
      } else {
        var win = window.open(url, '_blank');
        win.focus();
      }
    }
  }, {
    key: "loadLang",
    value: function loadLang(callback) {
      RequestBuilder.sRequestBuilder.get('settings/lang/json?lang=tot', function (error, data) {
        $.i18n().load(data).done(callback);
      });
    }
  }, {
    key: "getStore",
    value: function getStore() {
      if (this.isElectron) {
        return ElectronStore;
      } else return NextcloudStore;
    }
  }, {
    key: "openElectronSyncDialog",
    value: function openElectronSyncDialog() {
      var remote = require('@electron/remote');
      var BrowserWindow = remote.BrowserWindow;
      var win = new BrowserWindow({
        width: 500,
        height: 500,
        frame: true,
        webPreferences: {
          nodeIntegration: true,
          webviewTag: true,
          enableRemoteModule: true,
          contextIsolation: false
        }
      });
      var url = require('url');
      var path = require('path');
      win.loadURL(url.format({
        pathname: path.join(__dirname, 'settings/webdav_dialog.html'),
        protocol: 'file:',
        slashes: true
      }));
      win.setMenu(null);
      var main = remote.require("./main");
      main.enableEditorWebContent(win.webContents.id);
    }
  }, {
    key: "sendNextLargeDownload",
    value: function sendNextLargeDownload() {
      if (this.largeDownload == undefined) {
        if (currentFrame != undefined) {
          currentFrame.contentWindow.compatibility.sendNextLargeDownload();
          return;
        }
        return;
      }
      if (this.largeDownload.length <= 0) {
        app.onLargeDownloadEnd();
        this.largeDownload = undefined;
      } else {
        var nextSize = this.largeDownload.length > 200000 ? 200000 : this.largeDownload.length;
        var next = this.largeDownload.substring(0, nextSize);
        this.largeDownload = this.largeDownload.substring(nextSize);
        app.onNextLargeDownload(next);
      }
    }
  }]);
  return Compatibility;
}();