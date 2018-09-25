var KeywordsDBManager = function (path) {

}

KeywordsDBManager.prototype.getFullDB = function (callback) {
    console.log("getFullDB")
    RequestBuilder.sRequestBuilder.get("/keywordsdb", callback)
}

KeywordsDBManager.prototype.getFlatenDB = function (callback) {
    this.getFullDB(function (err, data) {
        console.log(data)
        var fullDB = data["data"];
        var flaten = {};
        for (let item of fullDB) {
            var keyword = item.keyword
            if (keyword == undefined)
                continue;
            if (keyword != undefined && flaten[keyword] == undefined) {
                flaten[keyword] = []
            }
            var index = flaten[keyword].indexOf(item.path);
            if (item.action == "add") {
                if (index == -1) {

                    flaten[keyword].push(item.path)

                }
            } else if (item.action == "remove") {
                if (index > -1) {
                    flaten[keyword].splice(index, 1);
                }
            } else if (item.action == "move") {
                for (let key in flaten) {
                    var indexBis = flaten[key].indexOf(item.path);
                    flaten[key][indexBis] = item.newPath;
                }
            }
        }
        for (let key in flaten) {
            flaten[key].reverse() //unshift seems slower...
        }
        callback(false, flaten);
    });
}

KeywordsDBManager.prototype.addToDB = function (keyword, path, callback) {
    console.log("path 1 " + path)
    if (path.startsWith("/"))
        path = NoteUtils.getNoteRelativePath(settingsHelper.getNotePath(), path)
    console.log("path 2 " + path)

    this.action(keyword, path, "add", callback)
}

KeywordsDBManager.prototype.removeFromDB = function (keyword, path, callback) {
    if (path.startsWith("/"))
        path = NoteUtils.getNoteRelativePath(settingsHelper.getNotePath(), path)
    this.action(keyword, path, "remove", callback)
}

KeywordsDBManager.prototype.action = function (keyword, path, action, callback) {
    this.actionArray([{
        keyword,
        time: new Date().getTime(),
        action: action,
        path: path
    }], callback)
}

KeywordsDBManager.prototype.actionArray = function (items, callback) {
    RequestBuilder.sRequestBuilder.post("/keywordsdb/action", {
        data: items
    }, function (error, data) {
        console.log(data)
        callback();
    });

}

//returns last time
KeywordsDBManager.prototype.mergeDB = function (path, callback) {
    console.log("merging with " + path);
    var db = this;
    var hasChanged = false;
    lockFile.lock('keyword.lock', {
        wait: 10000
    }, function (er) {
        console.log(er)
        lockFile.unlock('keyword.lock', function (er) {})
        db.getFullDB(function (err, data) {
            var otherDB = new KeywordsDBManager(path)
            otherDB.getFullDB(function (err, dataBis) {
                var dataJson = JSON.parse(data)
                try {
                    var dataBisJson = JSON.parse(dataBis)
                } catch (e) { //bad :(
                    return
                }
                for (let itemBis of dataBisJson["data"]) {
                    var isIn = false;
                    for (let item of dataJson["data"]) {
                        if (itemBis.time == item.time && itemBis.path == item.path && itemBis.action == item.action) {
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        dataJson["data"].push(itemBis);
                        hasChanged = true;
                    }
                }
                if (hasChanged) {
                    dataJson["data"].sort(keysrt('time'))
                    require("mkdirp")(getParentFolderFromPath(db.path), function () {
                        lockFile.lock('recent.lock', {
                            wait: 10000
                        }, function (er) {
                            fs.writeFile(db.path, JSON.stringify(dataJson), function (err) {
                                console.log(err);
                                callback(hasChanged);
                            });
                            lockFile.unlock('keyword.lock', function (er) {})

                        })
                    })
                } else callback(hasChanged);
            });
        })
    });
}

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
    }
}

exports.KeywordsDBManager = KeywordsDBManager