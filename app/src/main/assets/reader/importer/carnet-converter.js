"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

var CarnetConverter = /*#__PURE__*/function () {
  function CarnetConverter(_importer) {
    _classCallCheck(this, CarnetConverter);

    _defineProperty(this, "importFilesToZip", function (notePath, currentZip, destZip, files, callback) {
      if (files == undefined || files.length <= 0) {
        callback();
        return;
      }

      var importer = this;
      var file = files.pop();
      console.log("importing " + file);
      currentZip.files[notePath + file].async("base64").then(function (data) {
        destZip.file(file, data, {
          base64: true
        });
        importer.importFilesToZip(notePath, currentZip, destZip, files, callback);
      });
    });

    this.importer = _importer;
    this.removeRoot = undefined;
  }

  _createClass(CarnetConverter, [{
    key: "getDestPath",
    value: function getDestPath() {
      return "/";
    }
  }, {
    key: "convertNoteToSQD",
    value: function convertNoteToSQD(currentZip, notePath, destFolder, callback) {
      console.log("convertNoteToSQD " + notePath);
      var fileName = notePath.endsWith("/") ? FileUtils.getFilename(notePath.substring(0, notePath.length - 1)) : FileUtils.getFilename(notePath);
      var converter = this;
      var dest = FileUtils.getParentFolderFromPath(notePath); //detect if root path(for example in nextcloud we don't want to keep QuickNote root folder)

      if (this.importer.archiveName.indexOf("no_root") < 0) {
        if (this.removeRoot == undefined) {
          //check if all notes have same root
          if (this.pathList.length > 0) {
            var sameRoot = true;
            var root = undefined;

            var _iterator = _createForOfIteratorHelper(this.pathList),
                _step;

            try {
              for (_iterator.s(); !(_step = _iterator.n()).done;) {
                var path = _step.value;
                var splitPath = path.split("/");
                var thisRoot = undefined;

                if (splitPath.length > 0) {
                  thisRoot = splitPath[0];

                  if (thisRoot == "" || thisRoot == undefined) {
                    thisRoot = undefined;
                    if (splitPath.length > 1) thisRoot = splitPath[1];
                  }
                }

                if (root == undefined) root = thisRoot;
                console.log("current root " + root);

                if (root != thisRoot) {
                  sameRoot = false;
                  break;
                }
              }
            } catch (err) {
              _iterator.e(err);
            } finally {
              _iterator.f();
            }

            if (sameRoot && root != undefined) {
              this.removeRoot = root;
            }
          } else this.removeRoot = false;
        }

        if (this.removeRoot) {
          var toRemove = this.removeRoot.length;

          if (dest.indexOf("/") === 0) {
            toRemove++;
          }

          dest = dest.substring(toRemove);
        }
      }

      if (notePath.endsWith("/")) {
        // is folder note, need to create a directory
        var filesToImportToZip = [];
        var zip = new JSZip();
        currentZip.folder(notePath).forEach(function (relativePath, zipEntry) {
          console.log(relativePath);
          filesToImportToZip.push(relativePath);
        });
        converter.importFilesToZip(notePath, currentZip, zip, filesToImportToZip, function () {
          zip.generateAsync({
            type: "blob"
          }).then(function (noteBlob) {
            currentZip.files[notePath + "metadata.json"].async('string').then(function (metadata) {
              callback(noteBlob, metadata, fileName, metadata.isPinned, dest);
            });
          });
        });
      } else {
        currentZip.files[notePath].async('blob').then(function (noteBlob) {
          console.log("blob loaded " + noteBlob);
          noteBlob.arrayBuffer().then(function (buffer) {
            console.log("buffer loaded " + buffer);
            JSZip.loadAsync(buffer).then(function (noteZip) {
              console.log("noteZip loaded " + noteZip);
              if (noteZip.files["metadata.json"] != undefined) noteZip.files["metadata.json"].async('string').then(function (metadata) {
                console.log("metadata loaded ");
                callback(noteBlob, metadata, fileName, metadata.isPinned, dest);
              });else callback(undefined);
            }, function (e) {
              console.log("error " + e);
              callback(undefined);
            });
          });
        });
      }
    }
  }, {
    key: "getListOfNotesFromZip",
    value: function getListOfNotesFromZip(zip, callback) {
      var list = [];
      zip.forEach(function (relativePath, zipEntry) {
        console.log("note " + relativePath);

        if (relativePath.endsWith(".sqd") || relativePath.endsWith(".sqd/")) {
          list.push(relativePath);
        }
      });
      this.pathList = list;
      callback(list);
    }
  }, {
    key: "hasRecentDB",
    value: function hasRecentDB() {
      return true;
    }
  }]);

  return CarnetConverter;
}();