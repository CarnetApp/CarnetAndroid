function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

var SettingsCompatibility =
/*#__PURE__*/
function (_Compatibility) {
  "use strict";

  _inherits(SettingsCompatibility, _Compatibility);

  function SettingsCompatibility() {
    var _this;

    _classCallCheck(this, SettingsCompatibility);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(SettingsCompatibility).call(this));

    var compatibility = _assertThisInitialized(_this);

    $(document).ready(function () {
      if (compatibility.isElectron) {
        var SettingsHelper = require("./settings/settings_helper").SettingsHelper;

        var settingsHelper = new SettingsHelper();
        document.getElementById("window-frame-switch").checked = settingsHelper.displayFrame();

        document.getElementById("window-frame-switch").onchange = function () {
          settingsHelper.setDisplayFrame(document.getElementById("window-frame-switch").checked);

          var remote = require('electron').remote;

          remote.app.relaunch();
          remote.app.exit(0);
        };

        document.getElementById("export").parentElement.style.display = "none";

        document.getElementById("disconnect").onclick = function () {
          settingsHelper.setRemoteWebdavAddr(undefined);
          settingsHelper.setRemoteWebdavUsername(undefined);
          settingsHelper.setRemoteWebdavPassword(undefined);
          settingsHelper.setRemoteWebdavPath(undefined);
          window.location.reload(true);
        };

        document.getElementById("connect").onclick = function () {
          var _require = require('electron'),
              remote = _require.remote;

          var BrowserWindow = remote.BrowserWindow;
          var win = new BrowserWindow({
            width: 500,
            height: 500,
            frame: true
          });

          var url = require('url');

          var path = require('path');

          win.loadURL(url.format({
            pathname: path.join(__dirname, 'settings/webdav_dialog.html'),
            protocol: 'file:',
            slashes: true
          }));
          win.setMenu(null);
        };

        if (settingsHelper.getRemoteWebdavAddr() == undefined) {
          document.getElementById("disconnect").parentElement.style.display = "none";
          document.getElementById("connect").parentElement.style.display = "block";
        } else {
          document.getElementById("connect").parentElement.style.display = "none";
          document.getElementById("disconnect").parentElement.style.display = "block";
        }
      } else {
        document.getElementById("window-frame-switch").parentElement.style.display = "none";
        document.getElementById("connect").parentElement.style.display = "none";
        document.getElementById("disconnect").parentElement.style.display = "none";
      }
    });
    return _this;
  }

  return SettingsCompatibility;
}(Compatibility);

var compatibility = new SettingsCompatibility();