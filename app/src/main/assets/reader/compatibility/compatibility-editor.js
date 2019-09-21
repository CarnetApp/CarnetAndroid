"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

var rootpath = "";

String.prototype.endsWith = function (suffix) {
  return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function (suffix) {
  return this.indexOf(suffix) === 0;
};

var CompatibilityEditor =
/*#__PURE__*/
function (_Compatibility) {
  _inherits(CompatibilityEditor, _Compatibility);

  function CompatibilityEditor() {
    var _this;

    _classCallCheck(this, CompatibilityEditor);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(CompatibilityEditor).call(this));

    if (_this.isElectron) {
      module.paths.push(rootpath + 'node_modules');

      require('electron').ipcRenderer.on('loadnote', function (event, path) {
        loadPath(path);
      });
    } else {
      var exports = function exports() {};
    }

    return _this;
  }

  _createClass(CompatibilityEditor, [{
    key: "print",
    value: function print(printTitle, printMod, printCreation, note) {
      var dateC = new Date(note.metadata.creation_date);
      var dateM = new Date(note.metadata.last_modification_date);
      var tmpDiv = document.createElement('div');
      if (printTitle) tmpDiv.innerHTML += "<h3>" + FileUtils.stripExtensionFromName(FileUtils.getFilename(note.path)) + "<h3>";
      if (printCreation) tmpDiv.innerHTML += "<span> Created: " + dateC.toLocaleDateString() + " " + dateC.toLocaleTimeString() + "</span><br />";
      if (printMod) tmpDiv.innerHTML += "<span> Modified: " + dateM.toLocaleDateString() + " " + dateM.toLocaleTimeString() + "</span><br />";
      if (printMod || printCreation) tmpDiv.innerHTML += "<br />";
      tmpDiv.innerHTML += writer.oDoc.innerHTML;

      if (this.isAndroid) {
        app.print(tmpDiv.innerHTML);
      } else {
        var ifr = document.createElement('iframe');
        ifr.style = 'height: 0px; width: 0px; position: absolute';
        document.body.appendChild(ifr);
        $(tmpDiv).clone().appendTo(ifr.contentDocument.body);
        ifr.contentWindow.print();
        ifr.parentElement.removeChild(ifr);
      }
    }
  }, {
    key: "exit",
    value: function exit() {
      if (this.isGtk) {
        window.parent.document.title = "msgtopython:::exit";
        parent.postMessage("exit", "*");
      } else if (this.isElectron) {
        var _require = require('electron'),
            ipcRenderer = _require.ipcRenderer;

        ipcRenderer.sendToHost('exit', "");
      } else if (this.isAndroid) app.postMessage("exit", "*");else if (window.self !== window.top) //in iframe
        parent.postMessage("exit", "*");
    }
  }, {
    key: "getRecorder",
    value: function getRecorder(options) {
      if (this.isAndroid) return new AndroidRecorder(options);
      return new Recorder(options);
    }
  }, {
    key: "onNoteLoaded",
    value: function onNoteLoaded() {
      if (this.isGtk) {
        document.getElementsByClassName('mdl-layout__header')[0].style.display = "none";
        window.parent.document.title = "msgtopython:::noteloaded";
      }

      if (this.isElectron) {
        var _require2 = require('electron'),
            ipcRenderer = _require2.ipcRenderer;

        ipcRenderer.sendToHost('loaded', "");
      } else if (this.isAndroid) {
        app.hideProgress();
      } else {
        parent.postMessage("loaded", "*");
      }
    }
  }]);

  return CompatibilityEditor;
}(Compatibility);

var compatibility = new CompatibilityEditor();
var isElectron = compatibility.isElectron;