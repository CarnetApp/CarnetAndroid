var fs = require("fs");
var getParentFolderFromPath = require('path').dirname;
var FileBrowser = function (path){
    this.path = path;
}

FileBrowser.prototype.list = function(callback){
    fs.readdir(this.path, (err, dir) => {
    //console.log(dir);
    var files = [];
    var dirs_in = [];
    var files_in = [];
        for(let filePath of dir){
            var filename = filePath;
            filePath = this.path+"/"+filePath
            var stat = fs.statSync(filePath);
            file = new File(filePath, stat.isFile(), filename);
            console.log(filePath)
            if(stat.isFile())
                files_in.push(file)
            else
                dirs_in.push(file)
            
        }
        files = files.concat(dirs_in)
        files = files.concat(files_in)
        
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
