"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

var fs = require("fs");

var getParentFolderFromPath = require('path').dirname;

var FolderPicker = function FolderPicker() {
  this.elem = document.getElementById("table-container");
  this.progressView = document.getElementById("progress-view");

  var _require = require('electron'),
      ipcRenderer = _require.ipcRenderer;

  var picker = this;

  var _iterator = _createForOfIteratorHelper(document.getElementsByClassName("import-button")),
      _step;

  try {
    for (_iterator.s(); !(_step = _iterator.n()).done;) {
      var elem = _step.value;

      elem.onclick = function () {
        ipcRenderer.sendToHost('pathSelected', picker.path);
      };
    }
  } catch (err) {
    _iterator.e(err);
  } finally {
    _iterator.f();
  }
};

FolderPicker.prototype.listPath = function (path) {
  console.log("list " + path);
  this.path = path;
  this.elem.innerHTML = "";
  $(this.progressView).show();

  var _iterator2 = _createForOfIteratorHelper(document.getElementsByClassName("import-button")),
      _step2;

  try {
    for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
      var elem = _step2.value;
      $(elem).hide();
    }
  } catch (err) {
    _iterator2.e(err);
  } finally {
    _iterator2.f();
  }

  var table = document.createElement("table");
  table.classList.add("mdl-data-table");
  table.classList.add("mdl-js-data-table");
  table.classList.add("mdl-shadow--2dp");
  var tbody = document.createElement("tbody");
  table.appendChild(tbody);
  this.elem.appendChild(table);
  folderPicker = this;
  var tr = document.createElement("tr");
  tbody.appendChild(tr);
  var td = document.createElement("td");
  td.classList.add("mdl-data-table__cell--non-numeric");
  tr.appendChild(td);
  td.innerHTML = "Parent folder";
  td.path = getParentFolderFromPath(path);

  td.onclick = function (elem) {
    console.log(this.path);
    folderPicker.listPath(this.path);
  };

  fs.readdir(path, function (err, dir) {
    console.log(err);

    var _iterator3 = _createForOfIteratorHelper(dir),
        _step3;

    try {
      for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
        var filename = _step3.value;
        if (filename.startsWith(".")) continue;
        var filePath = path + "/" + filename;
        var stat = fs.statSync(filePath);
        if (stat.isFile()) continue;
        var tr = document.createElement("tr");
        tbody.appendChild(tr);
        var td = document.createElement("td");
        td.classList.add("mdl-data-table__cell--non-numeric");
        tr.appendChild(td);
        var img = document.createElement("img");
        td.appendChild(img);
        img.src = "../img/directory.png";
        img.classList.add("icon");
        var span = document.createElement("span");
        td.appendChild(span);
        span.innerHTML = filename;
        td.path = filePath;

        td.onclick = function (elem) {
          console.log(this.path);
          folderPicker.listPath(this.path);
        };
      }
    } catch (err) {
      _iterator3.e(err);
    } finally {
      _iterator3.f();
    }

    $(folderPicker.progressView).hide();

    var _iterator4 = _createForOfIteratorHelper(document.getElementsByClassName("import-button")),
        _step4;

    try {
      for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
        var elem = _step4.value;
        $(elem).show();
      }
    } catch (err) {
      _iterator4.e(err);
    } finally {
      _iterator4.f();
    }
  });
};