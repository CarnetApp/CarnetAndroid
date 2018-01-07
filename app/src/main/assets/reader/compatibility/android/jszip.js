this.zip.loadAsync(this.data, {base64: true}).then(function (contents) {
    extractor.files = Object.keys(contents.files);
    extractor.fullExtract()
  });

var JSZipCompatibility = function(){

}

JSZipCompatibility.prototype.loadAsync = function(data, options){

}

var JSZipTask = function(data, options){
    this.data = data;
}

function generateUID() {
    // I generate the UID from two parts here
    // to ensure the random number provide enough bits.
    var firstPart = (Math.random() * 46656) | 0;
    var secondPart = (Math.random() * 46656) | 0;
    firstPart = ("000" + firstPart.toString(36)).slice(-3);
    secondPart = ("000" + secondPart.toString(36)).slice(-3);
    return firstPart + secondPart;
}

var callbacks = []

JSZipTask.prototype.then = function(callback){
    var uid = generateUID();
    callbacks[uid] = callback;
    //start
    app.listZip(uid, this.data, "base64");
    
}

JSZipTask.prototype.onList = function(callback, array){
    callbacks[callback]({files=array})
}