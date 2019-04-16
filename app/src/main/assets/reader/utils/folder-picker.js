"use strict";

var fs = require("fs");

var getParentFolderFromPath = require('path').dirname;

var FolderPicker = function FolderPicker() {
  this.elem = document.getElementById("table-container");
  this.progressView = document.getElementById("progress-view");

  var _require = require('electron'),
      ipcRenderer = _require.ipcRenderer;

  var picker = this;
  var _iteratorNormalCompletion = true;
  var _didIteratorError = false;
  var _iteratorError = undefined;

  try {
    for (var _iterator = document.getElementsByClassName("import-button")[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
      var elem = _step.value;

      elem.onclick = function () {
        ipcRenderer.sendToHost('pathSelected', picker.path);
      };
    }
  } catch (err) {
    _didIteratorError = true;
    _iteratorError = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion && _iterator["return"] != null) {
        _iterator["return"]();
      }
    } finally {
      if (_didIteratorError) {
        throw _iteratorError;
      }
    }
  }
};

FolderPicker.prototype.listPath = function (path) {
  console.log("list " + path);
  this.path = path;
  this.elem.innerHTML = "";
  $(this.progressView).show();
  var _iteratorNormalCompletion2 = true;
  var _didIteratorError2 = false;
  var _iteratorError2 = undefined;

  try {
    for (var _iterator2 = document.getElementsByClassName("import-button")[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
      var elem = _step2.value;
      $(elem).hide();
    }
  } catch (err) {
    _didIteratorError2 = true;
    _iteratorError2 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion2 && _iterator2["return"] != null) {
        _iterator2["return"]();
      }
    } finally {
      if (_didIteratorError2) {
        throw _iteratorError2;
      }
    }
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
    var _iteratorNormalCompletion3 = true;
    var _didIteratorError3 = false;
    var _iteratorError3 = undefined;

    try {
      for (var _iterator3 = dir[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
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
      _didIteratorError3 = true;
      _iteratorError3 = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion3 && _iterator3["return"] != null) {
          _iterator3["return"]();
        }
      } finally {
        if (_didIteratorError3) {
          throw _iteratorError3;
        }
      }
    }

    $(folderPicker.progressView).hide();
    var _iteratorNormalCompletion4 = true;
    var _didIteratorError4 = false;
    var _iteratorError4 = undefined;

    try {
      for (var _iterator4 = document.getElementsByClassName("import-button")[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
        var elem = _step4.value;
        $(elem).show();
      }
    } catch (err) {
      _didIteratorError4 = true;
      _iteratorError4 = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion4 && _iterator4["return"] != null) {
          _iterator4["return"]();
        }
      } finally {
        if (_didIteratorError4) {
          throw _iteratorError4;
        }
      }
    }
  });
};