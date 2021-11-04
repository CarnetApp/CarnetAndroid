"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

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
    return db.getFlatenDB(function (err, flaten, pin, metadata) {
      if (err) return callback(true);
      console.log(JSON.stringify(flaten));
      var files = [];

      var _iterator = _createForOfIteratorHelper(pin),
          _step;

      try {
        for (_iterator.s(); !(_step = _iterator.n()).done;) {
          var filePath = _step.value;
          var filename = filePath;
          filePath = filePath;
          file = new File(filePath, true, filename);
          file.isPinned = true;
          files.push(file);
        }
      } catch (err) {
        _iterator.e(err);
      } finally {
        _iterator.f();
      }

      var _iterator2 = _createForOfIteratorHelper(flaten),
          _step2;

      try {
        for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
          var _filePath = _step2.value;
          if (pin.indexOf(_filePath) != -1) continue;
          var filename = _filePath;
          _filePath = _filePath;
          file = new File(_filePath, true, filename);
          files.push(file);
        }
      } catch (err) {
        _iterator2.e(err);
      } finally {
        _iterator2.f();
      }

      callback(false, files, true, metadata);
    });
  } else if (this.path.startsWith("keyword://")) {
    console.log("getting keyword");
    var keywordsDBManager = new KeywordsDBManager();
    var filebrowser = this;
    return keywordsDBManager.getFlatenDB(function (error, data) {
      var files = [];
      console.log("keyword " + filebrowser.path.substring("keyword://".length));

      var _iterator3 = _createForOfIteratorHelper(data[filebrowser.path.substring("keyword://".length)]),
          _step3;

      try {
        for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
          var filePath = _step3.value;
          var filename = filePath;
          console.log("file " + filePath);
          filePath = filePath;
          file = new File(filePath, true, filename);
          files.push(file);
        }
      } catch (err) {
        _iterator3.e(err);
      } finally {
        _iterator3.f();
      }

      callback(false, files, true);
    });
  } else {
    var fbrowser = this;
    return RequestBuilder.sRequestBuilder.get(this.path.startsWith("search://") ? "/notes/getSearchCache" : "/browser/list?path=" + encodeURIComponent(this.path), function (error, data) {
      if (error) {
        callback(error);
        return;
      }

      var files = [];
      var dirs_in = [];
      var files_in = [];
      var endOfSearch = !fbrowser.path.startsWith("search://");

      var _iterator4 = _createForOfIteratorHelper(data['files']),
          _step4;

      try {
        for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
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
        _iterator4.e(err);
      } finally {
        _iterator4.f();
      }

      files = files.concat(dirs_in);
      files = files.concat(files_in);
      callback(false, files, endOfSearch, data['metadata']);
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