"use strict";

var KeywordsDBManager = function KeywordsDBManager(path) {};

KeywordsDBManager.prototype.getFullDB = function (callback) {
  console.log("getFullDB");
  RequestBuilder.sRequestBuilder.get("/keywordsdb", callback);
};

KeywordsDBManager.prototype.getFlatenDB = function (callback) {
  this.getFullDB(function (err, data) {
    console.log(data);
    var fullDB = data["data"];
    var flaten = {};
    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
      for (var _iterator = fullDB[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
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
              if (indexBis >= -1) flaten[key].splice(indexBis, 1);
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
      _didIteratorError = true;
      _iteratorError = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion && _iterator.return != null) {
          _iterator.return();
        }
      } finally {
        if (_didIteratorError) {
          throw _iteratorError;
        }
      }
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
}; //returns last time


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

        var _iteratorNormalCompletion2 = true;
        var _didIteratorError2 = false;
        var _iteratorError2 = undefined;

        try {
          for (var _iterator2 = dataBisJson["data"][Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
            var itemBis = _step2.value;
            var isIn = false;
            var _iteratorNormalCompletion3 = true;
            var _didIteratorError3 = false;
            var _iteratorError3 = undefined;

            try {
              for (var _iterator3 = dataJson["data"][Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
                var item = _step3.value;

                if (itemBis.time == item.time && itemBis.path == item.path && itemBis.action == item.action) {
                  isIn = true;
                  break;
                }
              }
            } catch (err) {
              _didIteratorError3 = true;
              _iteratorError3 = err;
            } finally {
              try {
                if (!_iteratorNormalCompletion3 && _iterator3.return != null) {
                  _iterator3.return();
                }
              } finally {
                if (_didIteratorError3) {
                  throw _iteratorError3;
                }
              }
            }

            if (!isIn) {
              dataJson["data"].push(itemBis);
              hasChanged = true;
            }
          }
        } catch (err) {
          _didIteratorError2 = true;
          _iteratorError2 = err;
        } finally {
          try {
            if (!_iteratorNormalCompletion2 && _iterator2.return != null) {
              _iterator2.return();
            }
          } finally {
            if (_didIteratorError2) {
              throw _iteratorError2;
            }
          }
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