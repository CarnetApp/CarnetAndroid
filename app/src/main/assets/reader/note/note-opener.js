var JSZip = require('jszip');
var mkdirp = require('mkdirp');
var fs = require('fs');

var NoteOpener = function (note) {
  this.note = note;
}

NoteOpener.prototype.getMainTextAndMetadata = function (callback) {
  var opener = this;
  this.getFullHTML(function (data, zip) {
    if(zip!=undefined){
    opener.getMetadataString(zip, function (metadata) {
      var tempElement = document.createElement("div");
      tempElement.innerHTML = data;
      callback(tempElement.innerText, metadata != undefined ? JSON.parse(metadata) : undefined)
    })
  } 
  else{
    callback(undefined, undefined)
    
  }
  });
}

NoteOpener.prototype.getMetadataString = function (zip, callback) {
  if (zip.file("metadata.json") != null)
    zip.file("metadata.json").async("string").then(callback)
  else {
    callback(undefined)
  }
}

NoteOpener.prototype.getFullHTML = function (callback) {
  console.log("this.note.path  "+this.note.path)
  
  fs.readFile(this.note.path, function (err, data) {
    if (err) {
      console.log("error ")
      return console.log(err);
    }
    if(data.length !=0)
    JSZip.loadAsync(data, {base64: true}).then(function (zip) {
      console.log("called "+zip)
      zip.file("index.html").async("string").then(function (content) {
        console.log("ok  ")
        
        callback(content, zip)
      })
    });
    else  callback(undefined, undefined)
  });
}

NoteOpener.prototype.extractTo = function (path, callback) {
  var opener = this;
  console.log("extractTo")
  fs.readFile(this.note.path,'base64', function (err, data) {
    console.log("read " + err)
    if (!err) {
      var extractor = new Extractor(data, path, opener, callback)
      extractor.start();
    } else callback(true)
  });
}

NoteOpener.prototype.compressFrom = function (path, callback) {
  var comp = new Compressor(path, this.note.path, callback);
  comp.start();
}

var Extractor = function (data, dest, opener, callback) {
  this.data = data;
  this.currentFile = 0;
  this.path = dest;
  this.startTime = Date.now()
  this.callback = callback;
  this.opener = opener;
}

Extractor.prototype.start = function () {
  this.zip = new JSZip();
  var extractor = this;
  this.zip.loadAsync(this.data, {base64: true}).then(function (contents) {
    extractor.files = Object.keys(contents.files);
    extractor.fullExtract()
  });
}

Extractor.prototype.fullExtract = function () {
  console.log("fullExtract = " + this.files.length)

  if (this.currentFile >= this.files.length) {
    console.log("size = " + this.files.length) 
    console.log("took "+(Date.now()-this.startTime)+"ms")
    this.callback()
    return;
  }
  var filename = this.files[this.currentFile]
  var extractor = this;
  console.log("extract  = " + filename)
  var file = this.zip.file(filename);
    console.log("file  = " + file)

  if (file != null) {
    file.async('base64').then(function (content) {

  console.log("content  = " + content)

      if (content != "") {

        var dest = extractor.path + filename;
        console.log("mkdir");
        mkdirp.sync(getParentFolderFromPath(dest));
                console.log("mkdirok");

        fs.writeFileSync(dest, content,'base64');
        if (filename == "metadata.json") {
          extractor.opener.note.metadata = JSON.parse(decodeURIComponent(escape(atob(content))));
        }

      }
      extractor.currentFile++;
      extractor.fullExtract();
    });
  }
  else {
    extractor.currentFile++;
    extractor.fullExtract();
  }
}


var Compressor = function (source, dest, callback) {
  this.source = source;
  this.currentFile = 0;
  this.path = dest;
  this.callback = callback;
}

Compressor.prototype.start = function () {
  var fs = require('fs');
  var archiver = require('archiver');
  console.log("start")
  var archive = archiver.create('zip', {});
  var output = fs.createWriteStream(this.path);
  archive.pipe(output);

  archive
    .directory("tmp", false)
    .finalize();
  this.callback()
}

