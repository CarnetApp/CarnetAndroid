"use strict";

var FileUtils = function FileUtils() {};

FileUtils.base64MimeType = function (encoded) {
  var result = null;

  if (typeof encoded !== 'string') {
    return result;
  }

  var mime = encoded.match(/data:([a-zA-Z0-9]+\/[a-zA-Z0-9-.+]+).*,.*/);

  if (mime && mime.length) {
    result = mime[1];
  }

  return result;
};

FileUtils.isFileImage = function (filePath) {
  filePath = filePath.toLowerCase();
  return filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".gif") || filePath.endsWith(".bmp");
};

FileUtils.isFileAudio = function (filePath) {
  filePath = filePath.toLowerCase();
  return filePath.endsWith(".opus") || filePath.endsWith(".mp3") || filePath.endsWith(".ogg") || filePath.endsWith(".flac") || filePath.endsWith(".wav") || filePath.endsWith(".m4a") || filePath.endsWith(".webm");
};

FileUtils.getExtensionFromMimetype = function (mimetype) {
  switch (mimetype) {
    case "audio/3gpp":
      return "3gpp";

    case "image/jpeg":
      return "jpg";

    case "image/png":
      return "png";
  }
};

FileUtils.geMimetypeFromExtension = function (extension) {
  switch (extension) {
    case "3gpp":
      return "audio/3gpp";

    case "jpg":
      return "image/jpeg";

    case "png":
      return "image/png";
  }
};

FileUtils.getExtensionFromPath = function (path) {
  return path.split('.').pop().toLowerCase();
};

FileUtils.deleteFolderRecursive = function (path) {
  var fs = require('fs');

  var utils = this;

  if (fs.existsSync(path)) {
    fs.readdirSync(path).forEach(function (file, index) {
      var curPath = path + "/" + file;

      if (fs.lstatSync(curPath).isDirectory()) {
        // recurse
        utils.deleteFolderRecursive(curPath);
      } else {
        // delete file
        fs.unlinkSync(curPath);
      }
    });
    fs.rmdirSync(path);
  }
};

FileUtils.base64ToBlob = function (base64) {
  var binary = atob(base64);
  var len = binary.length;
  var buffer = new ArrayBuffer(len);
  var view = new Uint8Array(buffer);

  for (var i = 0; i < len; i++) {
    view[i] = binary.charCodeAt(i);
  }

  var blob = new Blob([view]);
  return blob;
};

FileUtils.getFilename = function (filepath) {
  return filepath.replace(/^.*[\\\/]/, '');
};

FileUtils.stripExtensionFromName = function (name) {
  return name.replace(/\.[^/.]+$/, "");
};

FileUtils.splitPathRe = /^(\/?|)([\s\S]*?)((?:\.{1,2}|[^\/]+?|)(\.[^.\/]*|))(?:[\/]*)$/;

FileUtils.posixSplitPath = function (filename) {
  return FileUtils.splitPathRe.exec(filename).slice(1);
};

FileUtils.getParentFolderFromPath = function (path) {
  var result = FileUtils.posixSplitPath(path),
      root = result[0],
      dir = result[1];

  if (!root && !dir) {
    // No dirname whatsoever
    return '.';
  }

  if (dir) {
    // It has a dirname, strip trailing slash
    dir = dir.substr(0, dir.length - 1);
  }

  return root + dir;
};

if (typeof exports !== 'undefined') exports.FileUtils = FileUtils;