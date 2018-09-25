var fs = require("fs");

var KeywordsDBManager = require("./keywords_db_manager").KeywordsDBManager;

var KeywordDBMerger = function (recentFolder, appUid) {
    this.recentFolder = recentFolder;
    this.appUid = appUid;

}

KeywordDBMerger.prototype.startMergin = function (onFinished) {
    this.onFinished = onFinished;
    var merger = this;
    this.hasChanged = false;
    fs.readdir(this.recentFolder, (err, dir) => {
        if (dir == undefined) {
            onFinished(false);
            return;
        }
        merger.dir = dir;
        merger.pos = 0;
        merger.mergeNext();

    })
}
KeywordDBMerger.prototype.mergeNext = function () {
    var db = new KeywordsDBManager(this.recentFolder + "/" + this.appUid);

    var merger = this;
    if (this.pos < this.dir.length) {
        if (this.dir[this.pos].startsWith(".")) { //skip
            merger.pos++;
            merger.mergeNext();
        }
        db.mergeDB(merger.recentFolder + "/" + this.dir[this.pos], function (hasChanged) {
            if (hasChanged)
                merger.hasChanged = hasChanged;
            merger.pos++;
            merger.mergeNext();
        })

    } else this.onFinished(this.hasChanged);
}

exports.KeywordDBMerger = KeywordDBMerger;