var RecentDBManager = function RecentDBManager() {};

RecentDBManager.prototype.getFullDB = function (callback) {
  RequestBuilder.sRequestBuilder.get("/recentdb", callback);
};

RecentDBManager.prototype.getFlatenDB = function (callback) {
  this.getFullDB(function (err, data) {
    //with electron, working on big object transmitted to ui is a nightmare... so... sending string (far faster)
    if (typeof data === "string") data = JSON.parse(data);
    var fullDB = data["data"];
    var flaten = [];
    var pin = [];
    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
      for (var _iterator = fullDB[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
        var item = _step.value;
        var index = flaten.indexOf(item.path);
        var indexPin = pin.indexOf(item.path);

        if (item.action == "add") {
          if (index > -1) {
            flaten.splice(index, 1);
          }

          flaten.push(item.path);
        } else if (item.action == "remove") {
          if (index > -1) {
            flaten.splice(index, 1);
          }

          if (indexPin > -1) {
            pin.splice(indexPin, 1);
          }
        } else if (item.action == "move") {
          if (index > -1) {
            flaten[index] = item.newPath;
          }

          if (indexPin > -1) {
            pin[indexPin] = item.newPath;
          }
        } else if (item.action == "pin") {
          if (indexPin > -1) {
            pin.splice(indexPin, 1);
          }

          pin.push(item.path);
        } else if (item.action == "unpin") {
          if (indexPin > -1) {
            pin.splice(indexPin, 1);
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

    flaten.reverse();
    pin.reverse();
    callback(false, flaten, pin);
  });
};

RecentDBManager.prototype.addToDB = function (path, callback) {
  this.action(path, "add", callback);
};

RecentDBManager.prototype.removeFromDB = function (path, callback) {
  this.action(path, "remove", callback);
};

RecentDBManager.prototype.pin = function (path, callback) {
  this.action(path, "pin", callback);
};

RecentDBManager.prototype.unpin = function (path, callback) {
  this.action(path, "unpin", callback);
};

RecentDBManager.prototype.actionArray = function (items, action, callback) {
  var db = this;
  var time = new Date().getTime();
  db.getFullDB(function (err, data) {
    var fullDB = JSON.parse(data);
    var _iteratorNormalCompletion2 = true;
    var _didIteratorError2 = false;
    var _iteratorError2 = undefined;

    try {
      for (var _iterator2 = items[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
        var i = _step2.value;
        var item = new function () {
          this.time = i.time;
          this.action = action;
          this.path = i.path;
        }();
        fullDB["data"].push(item);
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

    require("mkdirp")(getParentFolderFromPath(db.path), function () {
      // opts is optional, and defaults to {} 
      console.log("writing");
      fs.writeFile(db.path, JSON.stringify(fullDB), function (err) {
        if (callback) callback();
      });
    });
  });
};

RecentDBManager.prototype.actionArray = function (items, callback) {
  RequestBuilder.sRequestBuilder.post("/recentdb/action", {
    data: items
  }, function (error, data) {
    console.log(data);
    callback();
  });
};

RecentDBManager.prototype.action = function (path, action, callback) {
  this.actionArray([{
    time: new Date().getTime(),
    action: action,
    path: path
  }], callback);
}; // sort on key values


function keysrt(key, desc) {
  return function (a, b) {
    return desc ? ~~(a[key] < b[key]) : ~~(a[key] > b[key]);
  };
}