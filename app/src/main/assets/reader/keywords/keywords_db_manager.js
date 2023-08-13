"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i]; return arr2; }
var KeywordsDBManager = function KeywordsDBManager(path) {};
KeywordsDBManager.prototype.getFullDB = function (callback) {
  console.log("getFullDB");
  return RequestBuilder.sRequestBuilder.get("/keywordsdb", callback);
};
KeywordsDBManager.prototype.getFlatenDB = function (callback) {
  return this.getFullDB(function (err, data) {
    console.log(data);
    var fullDB = data["data"];
    var flaten = {};
    var _iterator = _createForOfIteratorHelper(fullDB),
      _step;
    try {
      for (_iterator.s(); !(_step = _iterator.n()).done;) {
        var item = _step.value;
        var keyword = item.keyword;
        if (keyword == undefined && item.action !== "remove" && item.action !== "move") {
          continue;
        }
        if (keyword != undefined && flaten[keyword] == undefined) {
          flaten[keyword] = [];
        }
        var index = -1;
        if (keyword !== undefined) index = flaten[keyword].indexOf(item.path);
        if (item.action == "add") {
          if (index == -1) {
            flaten[keyword].push(item.path);
          }
        } else if (item.action == "remove") {
          if (index > -1) {
            flaten[keyword].splice(index, 1);
          }
          if (keyword == undefined) {
            for (var key in flaten) {
              var indexBis = flaten[key].indexOf(item.path);
              if (indexBis >= 0) {
                flaten[key].splice(indexBis, 1);
              }
            }
          }
        } else if (item.action == "move") {
          for (var _key2 in flaten) {
            var indexBis = flaten[_key2].indexOf(item.path);
            flaten[_key2][indexBis] = item.newPath;
          }
        }
      }
    } catch (err) {
      _iterator.e(err);
    } finally {
      _iterator.f();
    }
    for (var _key in flaten) {
      flaten[_key].reverse(); //unshift seems slower...
    }

    callback(false, flaten);
  });
};
KeywordsDBManager.prototype.addToDB = function (keyword, path, callback) {
  this.action(keyword, path, "add", callback);
};
KeywordsDBManager.prototype.removeFromDB = function (keyword, path, callback) {
  this.action(keyword, path, "remove", callback);
};
KeywordsDBManager.prototype.action = function (keyword, path, action, callback) {
  this.actionArray([{
    keyword: keyword,
    time: new Date().getTime(),
    action: action,
    path: path
  }], callback);
};
KeywordsDBManager.prototype.actionArray = function (items, callback) {
  RequestBuilder.sRequestBuilder.post("/keywordsdb/action", {
    data: items,
    json: JSON.stringify(items)
  }, function (error, data) {
    console.log(data);
    callback();
  });
};

//returns last time
KeywordsDBManager.prototype.mergeDB = function (path, callback) {
  console.log("merging with " + path);
  var db = this;
  var hasChanged = false;
  lockFile.lock('keyword.lock', {
    wait: 10000
  }, function (er) {
    console.log(er);
    lockFile.unlock('keyword.lock', function (er) {});
    db.getFullDB(function (err, data) {
      var otherDB = new KeywordsDBManager(path);
      otherDB.getFullDB(function (err, dataBis) {
        var dataJson = JSON.parse(data);
        try {
          var dataBisJson = JSON.parse(dataBis);
        } catch (e) {
          //bad :(
          return;
        }
        var _iterator2 = _createForOfIteratorHelper(dataBisJson["data"]),
          _step2;
        try {
          for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
            var itemBis = _step2.value;
            var isIn = false;
            var _iterator3 = _createForOfIteratorHelper(dataJson["data"]),
              _step3;
            try {
              for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
                var item = _step3.value;
                if (itemBis.time == item.time && itemBis.path == item.path && itemBis.action == item.action) {
                  isIn = true;
                  break;
                }
              }
            } catch (err) {
              _iterator3.e(err);
            } finally {
              _iterator3.f();
            }
            if (!isIn) {
              dataJson["data"].push(itemBis);
              hasChanged = true;
            }
          }
        } catch (err) {
          _iterator2.e(err);
        } finally {
          _iterator2.f();
        }
        if (hasChanged) {
          dataJson["data"].sort(keysrt('time'));
          require("mkdirp")(getParentFolderFromPath(db.path), function () {
            lockFile.lock('recent.lock', {
              wait: 10000
            }, function (er) {
              fs.writeFile(db.path, JSON.stringify(dataJson), function (err) {
                console.log(err);
                callback(hasChanged);
              });
              lockFile.unlock('keyword.lock', function (er) {});
            });
          });
        } else callback(hasChanged);
      });
    });
  });
};

/*KeywordsDBManager.prototype.actionArray = function (items, action, callback) {
    var db = this;
    lockFile.lock('keyword.lock', {
        wait: 10000
    }, function (er) {
        db.getFullDB(function (err, data) {
            var fullDB = JSON.parse(data);
            for (var i of items) {
                var item = new function () {
                    this.time = new Date().getTime();
                    this.action = action;
                    this.path = i.path;
                    this.keyword = i.keyword;

                };
                fullDB["data"].push(item);
            }
            require("mkdirp")(getParentFolderFromPath(db.path), function () {
                // opts is optional, and defaults to {} 

                console.log("writing")

                fs.writeFile(db.path, JSON.stringify(fullDB), function (err) {
                    if (callback)
                        callback()

                });
                lockFile.unlock('keyword.lock', function (er) {})
            })

        })
    });
}*/

// sort on key values
function keysrt(key, desc) {
  return function (a, b) {
    return desc ? ~~(a[key] < b[key]) : ~~(a[key] > b[key]);
  };
}