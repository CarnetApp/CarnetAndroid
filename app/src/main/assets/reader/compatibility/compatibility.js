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

    if (this.isElectron) {
      RequestBuilder = ElectronRequestBuilder;
      console.log("set resquest builder");
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
      var langs = ["en", "fr", "de", "ru", "nl", "cs", "sk"];
      var toLoad = {};

      for (var _i = 0, _langs = langs; _i < _langs.length; _i++) {
        var lang = _langs[_i];
        toLoad[lang] = (!this.isElectron ? RequestBuilder.sRequestBuilder.api_url : "/") + 'settings/lang/json?lang=' + lang;
      }

      if (!this.isElectron) {
        $.i18n().load(toLoad).done(callback);
      } else {
        var size = Object.keys(toLoad).length;
        var i = 0;
        var total = {};

        var _loop = function _loop() {
          var key = _Object$keys[_i2];
          RequestBuilder.sRequestBuilder.get(toLoad[key], function (error, data) {
            i++;
            total[key] = data;

            if (i == size) {
              $.i18n().load(total).done(callback);
            }
          });
        };

        for (var _i2 = 0, _Object$keys = Object.keys(toLoad); _i2 < _Object$keys.length; _i2++) {
          _loop();
        }
      }
    }
  }]);

  return Compatibility;
}();