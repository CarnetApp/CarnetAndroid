"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

var BrowserCompatibility =
/*#__PURE__*/
function (_Compatibility) {
  "use strict";

  _inherits(BrowserCompatibility, _Compatibility);

  function BrowserCompatibility() {
    var _this;

    _classCallCheck(this, BrowserCompatibility);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(BrowserCompatibility).call(this));

    var compatibility = _assertThisInitialized(_this);

    $(document).ready(function () {
      if (compatibility.isGtk) {
        document.getElementsByClassName('mdl-layout__header')[0].style.display = "none";
        document.getElementById('grid-button-container').style.display = "none";
      } else if (!compatibility.isElectron) {
        var right = document.getElementById("right-bar");
        right.removeChild(document.getElementById("minus-button"));
        right.removeChild(document.getElementById("close-button"));
        document.getElementById("settings-button").href = "./settings";

        document.getElementById("size-button").onclick = function () {
          if (!isFullScreen()) {
            var docElm = document.getElementById("content");

            if (docElm.requestFullscreen) {
              docElm.requestFullscreen();
            } else if (docElm.mozRequestFullScreen) {
              docElm.mozRequestFullScreen();
            } else if (docElm.webkitRequestFullScreen) {
              docElm.webkitRequestFullScreen();
            }
          } else {
            if (document.exitFullscreen) {
              document.exitFullscreen();
            } else if (document.mozCancelFullScreen) {
              document.mozCancelFullScreen();
            } else if (document.webkitCancelFullScreen) {
              document.webkitCancelFullScreen();
            }
          }
        };
      } else {
        var _require = require('electron'),
            remote = _require.remote,
            ipcRenderer = _require.ipcRenderer;

        var syncButton = document.getElementById("sync-button");
        syncButton.style.display = "inline";
        ipcRenderer.on('sync-start', function (event, arg) {
          syncButton.classList.add("rotation");
          syncButton.disabled = true;
        });
        ipcRenderer.on('sync-stop', function (event, arg) {
          syncButton.classList.remove("rotation");
          syncButton.disabled = false;
        });

        var main = remote.require("./main.js");

        if (main.isSyncing()) {
          syncButton.classList.add("rotation");
          syncButton.disabled = true;
        }

        syncButton.onclick = function () {
          if (!main.startSync()) {
            var _require2 = require('electron'),
                remote = _require2.remote;

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
          }
        };

        var SettingsHelper = require("./settings/settings_helper").SettingsHelper;

        var settingsHelper = new SettingsHelper();

        if (settingsHelper.displayFrame()) {
          document.getElementById("minus-button").style.display = "none";
          document.getElementById("size-button").style.display = "none";
          document.getElementById("close-button").style.display = "none";
        } else {
          document.getElementById("minus-button").onclick = function () {
            remote.BrowserWindow.getFocusedWindow().minimize();
          };

          document.getElementById("size-button").onclick = function () {
            if (remote.BrowserWindow.getFocusedWindow().isMaximized()) remote.BrowserWindow.getFocusedWindow().unmaximize();else remote.BrowserWindow.getFocusedWindow().maximize();
          };

          document.getElementById("close-button").onclick = function () {
            remote.app.exit(0);
          };
        }

        registerWriterEvent("exit", function () {
          main.syncOneNote(currentNotePath);
        });
        document.getElementById("settings-button").href = "settings.html";
        setTimeout(function () {
          RequestBuilder.sRequestBuilder.get("/settings/current_version", function (error, version) {
            if (!error) {
              console.log("current version " + version);
              $.ajax({
                url: "https://qn.phie.ovh/binaries/desktop/current_version",
                type: "GET",
                success: function success(newVersion) {
                  console.log("new version " + newVersion);

                  if (parseInt(version.replace(/\./g, "")) < parseInt(newVersion.replace(/\./g, ""))) {
                    displaySnack({
                      message: "New version available",
                      timeout: 10000,
                      actionHandler: function actionHandler() {
                        var _require3 = require('electron'),
                            shell = _require3.shell;

                        shell.openExternal("https://qn.phie.ovh/binaries/desktop/");
                      },
                      actionText: 'Download'
                    });
                  }
                },
                fail: function fail() {},
                error: function error(e) {}
              });
            }
          });
        }, 5000);
      }
    });
    return _this;
  }

  _createClass(BrowserCompatibility, [{
    key: "onFirstrunEnds",
    value: function onFirstrunEnds() {
      if (this.isElectron) {
        var _require4 = require('electron'),
            remote = _require4.remote;

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
      }
    }
  }, {
    key: "getEditorUrl",
    value: function getEditorUrl() {
      if (this.isElectron) return "";else if (!this.isAndroid) return "writer";
    }
  }, {
    key: "getStore",
    value: function getStore() {
      if (this.isElectron) {
        return ElectronStore;
      } else return NextcloudStore;
    }
  }, {
    key: "getMasonry",
    value: function getMasonry() {
      if (this.isElectron) {
        return require('masonry-layout');
      } else return Masonry;
    }
  }]);

  return BrowserCompatibility;
}(Compatibility);

var compatibility = new BrowserCompatibility();
var Store = compatibility.getStore();