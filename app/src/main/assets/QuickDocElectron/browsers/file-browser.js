const fs = require("fs");

var FileBrowser = function (path){
    this.path = path;
}

FileBrowser.prototype.list = function(callback){
    fs.readdir(this.path, (err, dir) => {
    //console.log(dir);
    var files = [];
        for(let filePath of dir){
            filePath = this.path+"/"+filePath
            var stat = fs.statSync(filePath);
            file = new File(filePath, stat.isFile());
            files.push(file)
        }
        callback(files)
    });
}

var File = function(path, isFile, name, extension){
    this.path = path;
    this.isFile = isFile;
    this.name = name;
    this.extension = extension;
    
}
File.prototype.getName = function(){
    return getFilenameFromPath(this.path);
}
function getFilenameFromPath(path){
    return path.replace(/^.*[\\\/]/, '');
}

function stripExtensionFromName(name){
   return name.replace(/\.[^/.]+$/, "")
}