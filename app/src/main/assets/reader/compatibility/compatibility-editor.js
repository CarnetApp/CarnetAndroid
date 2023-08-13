"use strict";

function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, _toPropertyKey(descriptor.key), descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _toPropertyKey(arg) { var key = _toPrimitive(arg, "string"); return _typeof(key) === "symbol" ? key : String(key); }
function _toPrimitive(input, hint) { if (_typeof(input) !== "object" || input === null) return input; var prim = input[Symbol.toPrimitive]; if (prim !== undefined) { var res = prim.call(input, hint || "default"); if (_typeof(res) !== "object") return res; throw new TypeError("@@toPrimitive must return a primitive value."); } return (hint === "string" ? String : Number)(input); }
function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); Object.defineProperty(subClass, "prototype", { writable: false }); if (superClass) _setPrototypeOf(subClass, superClass); }
function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }
function _createSuper(Derived) { var hasNativeReflectConstruct = _isNativeReflectConstruct(); return function _createSuperInternal() { var Super = _getPrototypeOf(Derived), result; if (hasNativeReflectConstruct) { var NewTarget = _getPrototypeOf(this).constructor; result = Reflect.construct(Super, arguments, NewTarget); } else { result = Super.apply(this, arguments); } return _possibleConstructorReturn(this, result); }; }
function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } else if (call !== void 0) { throw new TypeError("Derived constructors may only return object or undefined"); } return _assertThisInitialized(self); }
function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }
function _isNativeReflectConstruct() { if (typeof Reflect === "undefined" || !Reflect.construct) return false; if (Reflect.construct.sham) return false; if (typeof Proxy === "function") return true; try { Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function () {})); return true; } catch (e) { return false; } }
function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf.bind() : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }
var rootpath = "";
String.prototype.endsWith = function (suffix) {
  return this.indexOf(suffix, this.length - suffix.length) !== -1;
};
String.prototype.startsWith = function (suffix) {
  return this.indexOf(suffix) === 0;
};
var CompatibilityEditor = /*#__PURE__*/function (_Compatibility) {
  _inherits(CompatibilityEditor, _Compatibility);
  var _super = _createSuper(CompatibilityEditor);
  function CompatibilityEditor() {
    var _this;
    _classCallCheck(this, CompatibilityEditor);
    _this = _super.call(this);
    if (_this.isElectron) {
      module.paths.push(rootpath + 'node_modules');
      var ipcRenderer = require('electron').ipcRenderer;
      ipcRenderer.on('loadnote', function (event, path) {
        loadPath(path);
      });
      ipcRenderer.on('action', function (event, action) {
        writer.handleAction(action);
      });
    } else {
      var exports = function exports() {};
    }
    return _this;
  }
  _createClass(CompatibilityEditor, [{
    key: "exit",
    value: function exit() {
      if (this.isGtk) {
        window.parent.document.title = "msgtopython:::exit";
        parent.postMessage("exit", "*");
      } else if (this.isElectron) {
        var _require = require('electron'),
          _ipcRenderer = _require.ipcRenderer;
        _ipcRenderer.sendToHost('exit', "");
      } else if (this.isAndroid) app.postMessage("exit", "*");else if (window.self !== window.top)
        //in iframe
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
          _ipcRenderer2 = _require2.ipcRenderer;
        _ipcRenderer2.sendToHost('loaded', "");
      } else if (this.isAndroid) {
        app.hideProgress();
      } else {
        parent.postMessage("loaded", "*");
      }
    }
  }]);
  return CompatibilityEditor;
}(Compatibility);
var isElectron = typeof require === "function" || typeof parent.require === "function";
if (!isElectron) {
  $(document).ready(function () {
    init();
  });
} else {
  console.log("isDefinitivetyelectron");
  var ipcRenderer = require('electron').ipcRenderer;
  ipcRenderer.on('remote_ready', function (event, path) {
    init();
  });
}