var ArchiverCompatibility = function(){
}
ArchiverCompatibility.create = function(type, ktkl){
    return new Archive();
}

var Archive = function(){
}

Archive.prototype.pipe = function(output){
    this.output = output;
}
Archive.prototype.directory = function(dir){
    this.dir = dir;
    return this;
}

Archive.prototype.finalize = function(){
    app.zipDir(this.dir, this.output)
}