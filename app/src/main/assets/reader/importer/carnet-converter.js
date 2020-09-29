"use strict";

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var CarnetConverter =
/*#__PURE__*/
function () {
  function CarnetConverter(importer) {
    _classCallCheck(this, CarnetConverter);

    this.importer = importer;
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
      var fileName = FileUtils.getFilename(notePath);
      var converter = this;
      var dest = FileUtils.getParentFolderFromPath(notePath); //detect if root path(for example in nextcloud we don't want to keep QuickNote root folder)

      if (this.importer.archiveName.indexOf("no_root") < 0) {
        if (this.removeRoot == undefined) {
          //check if all notes have same root
          if (this.pathList.length > 0) {
            var sameRoot = true;
            var root = undefined;
            var _iteratorNormalCompletion = true;
            var _didIteratorError = false;
            var _iteratorError = undefined;

            try {
              for (var _iterator = this.pathList[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
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
  }, {
    key: "getListOfNotesFromZip",
    value: function getListOfNotesFromZip(zip, callback) {
      var list = [];
      zip.forEach(function (relativePath, zipEntry) {
        console.log("note " + relativePath);

        if (relativePath.endsWith(".sqd")) {
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