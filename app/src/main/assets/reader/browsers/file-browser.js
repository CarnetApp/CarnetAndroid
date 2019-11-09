"use strict";

var FileBrowser = function FileBrowser(path) {
  this.path = path;
};

FileBrowser.prototype.createFolder = function (name, callback) {
  fs.mkdir(pathTool.join(this.path, name), function (e) {
    callback();
  });
};

FileBrowser.prototype.list = function (callback) {
  if (this.path == "recentdb://") {
    console.log("getting recent");
    var db = RecentDBManager.getInstance();
    db.getFlatenDB(function (err, flaten, pin, metadata) {
      console.log(JSON.stringify(flaten));
      var files = [];
      var _iteratorNormalCompletion = true;
      var _didIteratorError = false;
      var _iteratorError = undefined;

      try {
        for (var _iterator = pin[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
          var filePath = _step.value;
          var filename = filePath;
          filePath = filePath;
          file = new File(filePath, true, filename);
          file.isPinned = true;
          files.push(file);
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

      var _iteratorNormalCompletion2 = true;
      var _didIteratorError2 = false;
      var _iteratorError2 = undefined;

      try {
        for (var _iterator2 = flaten[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
          var _filePath = _step2.value;
          if (pin.indexOf(_filePath) != -1) continue;
          var filename = _filePath;
          _filePath = _filePath;
          file = new File(_filePath, true, filename);
          files.push(file);
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

      callback(files, true, metadata);
    });
  } else if (this.path.startsWith("keyword://")) {
    console.log("getting keyword");
    var keywordsDBManager = new KeywordsDBManager();
    var filebrowser = this;
    keywordsDBManager.getFlatenDB(function (error, data) {
      var files = [];
      console.log("keyword " + filebrowser.path.substring("keyword://".length));
      var _iteratorNormalCompletion3 = true;
      var _didIteratorError3 = false;
      var _iteratorError3 = undefined;

      try {
        for (var _iterator3 = data[filebrowser.path.substring("keyword://".length)][Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
          var filePath = _step3.value;
          var filename = filePath;
          console.log("file " + filePath);
          filePath = filePath;
          file = new File(filePath, true, filename);
          files.push(file);
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

      callback(files, true);
    });
  } else {
    var fbrowser = this;
    RequestBuilder.sRequestBuilder.get(this.path.startsWith("search://") ? "/notes/getSearchCache" : "/browser/list?path=" + encodeURIComponent(this.path), function (error, data) {
      if (error) {
        callback(error);
        return;
      }

      var files = [];
      var dirs_in = [];
      var files_in = [];
      var endOfSearch = !fbrowser.path.startsWith("search://");
      var _iteratorNormalCompletion4 = true;
      var _didIteratorError4 = false;
      var _iteratorError4 = undefined;

      try {
        for (var _iterator4 = data['files'][Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
          var node = _step4.value;
          console.log(node);

          if (node == "end_of_search") {
            endOfSearch = true;
            continue;
          }

          if (node.path == "quickdoc") continue;
          file = new File(node.path, !node.isDir, node.name);
          if (!node.isDir) files_in.push(file);else {
            dirs_in.push(file);
          }
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

      files = files.concat(dirs_in);
      files = files.concat(files_in);
      callback(files, endOfSearch, data['metadata']);
    });
  }
};

var File = function File(path, isFile, name, extension) {
  this.path = path;
  this.isFile = isFile;
  this.name = name;
  this.extension = extension;
};

File.prototype.getName = function () {
  return getFilenameFromPath(this.path);
};

function getFilenameFromPath(path) {
  return path.replace(/^.*[\\\/]/, '');
}

function stripExtensionFromName(name) {
  return name.replace(/\.[^/.]+$/, "");
}