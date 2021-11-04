"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var SearchEngine = /*#__PURE__*/function () {
  function SearchEngine() {
    _classCallCheck(this, SearchEngine);
  }

  _createClass(SearchEngine, [{
    key: "oldSearchInNotes",
    value: function oldSearchInNotes(searching) {
      resetGrid(false);
      notes = [];
      document.getElementById("note-loading-view").style.display = "inline";
      RequestBuilder.sRequestBuilder.get("/notes/search?path=." + "&query=" + encodeURIComponent(searching), function (error, data) {
        if (!error) {
          console.log("listing");
          list("search://", true);
        }
      });
    }
  }, {
    key: "sendSearchQuery",
    value: function sendSearchQuery() {
      var self = this;
      lastListingRequestId = RequestBuilder.sRequestBuilder.get("/notes/search?path=." + "&query=" + encodeURIComponent(this.query) + "&from=" + this.from, function (error, data) {
        if (!error) {
          if (data['end'] || data['files'].length > 0) {
            document.getElementById("page-content").style.display = "block";
            document.getElementById("note-loading-view").style.display = "none";

            if (data['files'].length > 0) {
              var hasChanged = false;

              var _iterator = _createForOfIteratorHelper(data['files']),
                  _step;

              try {
                for (_iterator.s(); !(_step = _iterator.n()).done;) {
                  var node = _step.value;
                  if (node.path == "quickdoc") continue;
                  file = new File(node.path, !node.isDir, node.name);
                  var isIn = false;

                  var _iterator2 = _createForOfIteratorHelper(self.result),
                      _step2;

                  try {
                    for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
                      var fileIn = _step2.value;

                      if (fileIn.path == node.path) {
                        isIn = true;
                        break;
                      }
                    }
                  } catch (err) {
                    _iterator2.e(err);
                  } finally {
                    _iterator2.f();
                  }

                  if (!isIn) {
                    self.result.push(file);
                    hasChanged = true;
                  }
                }
              } catch (err) {
                _iterator.e(err);
              } finally {
                _iterator.f();
              }

              var callbackFiles = [];
              callbackFiles = callbackFiles.concat(self.result);
              if (hasChanged) onListEnd("search://", callbackFiles, undefined, true);
            }
          }

          self.from = data['next'];
          if (!data['end']) refreshTimeout = setTimeout(function () {
            self.sendSearchQuery();
          }, 500);
        }
      });
    }
  }, {
    key: "searchInNotes",
    value: function searchInNotes(query) {
      if (compatibility.isElectron) {
        searchEngine.oldSearchInNotes(query);
        return;
      }

      if (refreshTimeout !== undefined) clearTimeout(refreshTimeout);

      if (lastListingRequestId != undefined) {
        RequestBuilder.sRequestBuilder.cancelRequest(lastListingRequestId);
      }

      this.result = [];
      oldFiles = [];
      this.query = query;
      resetGrid(false);
      notes = [];
      document.getElementById("note-loading-view").style.display = "inline";
      this.from = 0;
      this.sendSearchQuery();
    }
  }]);

  return SearchEngine;
}();

var searchEngine = undefined;

document.getElementById("search-input").onkeydown = function (event) {
  if (event.key === 'Enter') {
    if (searchEngine == undefined) searchEngine = new SearchEngine();
    searchEngine.searchInNotes(this.value);
  }
};

document.getElementById("search-button").onclick = function () {
  var value = document.getElementById("search-input").value;

  if (value.length > 0) {
    if (searchEngine == undefined) searchEngine = new SearchEngine();
    searchEngine.searchInNotes(value);
  }
};