var fs = require("fs");
var getParentFolderFromPath = require('path').dirname;
var FileBrowser = function(path) {
    this.path = path;
}

FileBrowser.prototype.list = function(callback) {
    var { ipcRenderer, remote } = require('electron');
    var main = remote.require("./main.js");
    var mainPath = main.getNotePath();
    
    if (this.path == "recentdb://") {
        console.log("getting recent")
        var db = new RecentDBManager(mainPath + "/quickdoc/recentdb/" + main.getAppUid())
        db.getFlatenDB(function(err, flaten) {
            console.log(JSON.stringify(flaten))
            var files = [];
            for (let filePath of flaten) {
                var filename = filePath;
                filePath = mainPath +"/"+ filePath
                file = new File(filePath, true, filename);
                files.push(file)
            }
            callback(files)
        })
    } else if (this.path.startsWith("keyword://")) {
        console.log("getting keyword")
        var KeywordsDBManager = require("./keywords/keywords_db_manager").KeywordsDBManager;
        var keywordsDBManager = new KeywordsDBManager(main.getNotePath()+ "/quickdoc/keywords/"+main.getAppUid())    
        var filebrowser = this;
        keywordsDBManager.getFlatenDB(function(error, data){
            var files = [];
            console.log("keyword "+filebrowser.path.substring("keyword://".length))
            for (let filePath of data[filebrowser.path.substring("keyword://".length)]) {
                var filename = filePath;
                console.log("file "+filePath)
                
                filePath = mainPath +"/"+ filePath
                file = new File(filePath, true, filename);
                files.push(file)
            }
            callback(files)
        })
    }
    else {
        fs.readdir(this.path, (err, dir) => {
            //console.log(dir);
            var files = [];
            var dirs_in = [];
            var files_in = [];
            for (let filePath of dir) {
                var filename = filePath;
                if(filename == "quickdoc" || filename.startsWith("."))
                    continue;
                filePath = this.path + "/" + filePath
                var stat = fs.statSync(filePath);
                file = new File(filePath, stat.isFile(), filename);
                console.log(filePath)
                if (stat.isFile())
                    files_in.push(file)
                else
                    dirs_in.push(file)

            }
            files = files.concat(dirs_in)
            files = files.concat(files_in)

            callback(files)
        });
    }
}

var File = function(path, isFile, name, extension) {
    this.path = path;
    this.isFile = isFile;
    this.name = name;
    this.extension = extension;

}
File.prototype.getName = function() {
    return getFilenameFromPath(this.path);
}

function getFilenameFromPath(path) {
    return path.replace(/^.*[\\\/]/, '');
}

function stripExtensionFromName(name) {
    return name.replace(/\.[^/.]+$/, "")
}