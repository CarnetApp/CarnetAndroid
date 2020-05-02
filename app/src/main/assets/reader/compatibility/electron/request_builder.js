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

var ElectronRequestBuilder =
/*#__PURE__*/
function (_RequestBuilder) {
  _inherits(ElectronRequestBuilder, _RequestBuilder);

  function ElectronRequestBuilder() {
    var _this;

    _classCallCheck(this, ElectronRequestBuilder);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(ElectronRequestBuilder).call(this, "./"));

    var remote = require('electron').remote;

    _this.main = remote.require("./main.js");
    return _this;
  }

  _createClass(ElectronRequestBuilder, [{
    key: "get",
    value: function get(path, callback) {
      path = this.buildUrl(this.cleanPath(path));
      console.log("getting " + path);
      this.main.sendRequestToServer("GET", path, undefined, function (err, data) {
        if (data != undefined) {
          try {
            data = JSON.parse(data);
          } catch (e) {}
        }

        if (!RequestBuilder.sRequestBuilder.isCanceled(requestId)) callback(err, data);
      });
    }
  }, {
    key: "post",
    value: function post(path, data, callback) {
      path = this.buildUrl(this.cleanPath(path));
      this.main.sendRequestToServer("POST", path, data, function (err, data) {
        if (data != undefined) {
          try {
            data = JSON.parse(data);
          } catch (e) {}
        }

        if (!RequestBuilder.sRequestBuilder.isCanceled(requestId)) callback(err, data);
      });
    }
  }, {
    key: "postFiles",
    value: function postFiles(path, data, files, callback) {
      path = this.buildUrl(this.cleanPath(path));
      if (data == undefined) data = {};
      data.files = [];
      var request = this;
      var i = 0;

      function readNext() {
        if (i >= files.length) {
          request.main.sendRequestToServer("POST", path, data, function (err, data) {
            if (data != undefined) {
              try {
                data = JSON.parse(data);
              } catch (e) {}
            }

            if (!RequestBuilder.sRequestBuilder.isCanceled(requestId)) callback(err, data);
          });
          return;
        }

        var reader = new FileReader();
        reader.readAsDataURL(files[i]);

        reader.onload = function () {
          console.log(reader.result.replace(/^data:.*\/\w+;base64,/, ""));
          var f = {
            name: files[i].name,
            data: reader.result.replace(/^data:.*\/\w+;base64,/, "")
          };
          data.files.push(f);
          i++;
          readNext();
        };
      }

      readNext();
    }
  }, {
    key: "delete",
    value: function _delete(path, callback) {
      path = this.buildUrl(this.cleanPath(path));
      this.main.sendRequestToServer("DELETE", path, undefined, function (err, data) {
        if (data != undefined) {
          try {
            data = JSON.parse(data);
          } catch (e) {}
        }

        if (!RequestBuilder.sRequestBuilder.isCanceled(requestId)) callback(err, data);
      });
    }
  }, {
    key: "buildUrl",
    value: function buildUrl(path) {
      return "/" + path;
    }
  }]);

  return ElectronRequestBuilder;
}(RequestBuilder);