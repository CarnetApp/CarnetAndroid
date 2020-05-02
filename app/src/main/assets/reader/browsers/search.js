"use strict";

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var SearchEngine =
/*#__PURE__*/
function () {
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
              var _iteratorNormalCompletion = true;
              var _didIteratorError = false;
              var _iteratorError = undefined;

              try {
                for (var _iterator = data['files'][Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                  var node = _step.value;
                  if (node.path == "quickdoc") continue;
                  file = new File(node.path, !node.isDir, node.name);
                  self.result.push(file);
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

              var callbackFiles = [];
              callbackFiles = callbackFiles.concat(self.result);
              onListEnd("search://", callbackFiles, undefined, true);
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