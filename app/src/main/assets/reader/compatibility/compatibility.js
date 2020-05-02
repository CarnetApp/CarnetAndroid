"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var Compatibility =
/*#__PURE__*/
function () {
  function Compatibility() {
    _classCallCheck(this, Compatibility);

    this.isElectron = typeof require === "function";
    this.isAndroid = (typeof app === "undefined" ? "undefined" : _typeof(app)) === "object";
    this.isGtk = false;
    console.log("is electron ?" + this.isElectron);
    console.log("is isAndroid ?" + this.isAndroid);

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
  }]);

  return Compatibility;
}();