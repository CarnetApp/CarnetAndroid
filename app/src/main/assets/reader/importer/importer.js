"use strict";

new RequestBuilder(Utils.getParameterByName("api_path") != null ? Utils.getParameterByName("api_path") : undefined);

var Importer = function Importer(destPath) {
  this.elem = document.getElementById("table-container");
  this.progressView = document.getElementById("progress-view");
  this.destPath = destPath;
  this.importingSpan = document.getElementById("importing");
  this.webview = document.getElementById("webview");
  var importer = this;
  document.getElementById("folder-picker").style.display = "none";
  $("#note-selection-view").hide();
  $("#importing-view").hide();
  $('#import-finished').hide();

  document.getElementById("import-button1").onclick = document.getElementById("import-button2").onclick = function () {
    importer.importNotes();
  };

  document.getElementById("select-folder-button").onclick = function () {
    document.getElementById("input_file").click();
  };

  document.getElementById("back_arrow").onclick = document.getElementById("exit-button").onclick = function () {
    compatibility.exit();
  };

  this.webview.addEventListener('ipc-message', function (event) {
    if (event.channel == "pathSelected") {
      importer.path = event.args[0];
      console.log("event.channel " + event.args[0]);
      importer.fillNoteList(function () {});
      document.getElementById("folder-picker").style.display = "none";
      $("#select-folder").hide();
      $("#note-selection-view").show();
    }
  });

  document.getElementById("input_file").onchange = function () {
    importer.onArchiveSelected(this.files[0]);
  };
};

function generateUID() {
  // I generate the UID from two parts here
  // to ensure the random number provide enough bits.
  var firstPart = Math.random() * 46656 | 0;
  var secondPart = Math.random() * 46656 | 0;
  firstPart = ("000" + firstPart.toString(36)).slice(-3);
  secondPart = ("000" + secondPart.toString(36)).slice(-3);
  return firstPart + secondPart;
}

Importer.prototype.listNotes = function (callback) {
  var fs = require("fs");

  var importer = this;
  fs.readdir(this.path, function (err, dir) {
    if (err) callback(false);else {
      importer.result = [];
      importer.dir = dir;
      importer.readNext(callback);
    }
  });
};

function keysrt(key, desc) {
  return function (a, b) {
    return desc ? ~~(a[key] < b[key]) : ~~(a[key] > b[key]);
  };
}

Importer.prototype.importNotes = function () {
  $("#importing-view").show();
  $("#note-selection-view").hide();
  this.notesToImport = this.getSelectedNotes();
  this.timeStampedNotes = [];
  this.timeStampedKeywords = [];
  this.error = [];
  var importer = this;
  importer.imported = 0;
  this.importNext(function () {
    $('#import-finished').show();
    $('#importing-view').hide();
    $('#import-report').html(importer.imported + " note(s) imported <br />" + importer.error.length + " failed");
  });
};

Importer.prototype.importNext = function (callback) {
  if (this.notesToImport.length <= 0) {
    callback();
    return;
  }

  var notePath = this.notesToImport.pop();
  var importer = this;
  this.importNote(notePath, this.destPath, function () {
    importer.importNext(callback);
  });
};

Importer.prototype.onArchiveSelected = function (archive) {
  var importer = this;
  importer.archiveName = archive.name;
  console.log("$(input[name='archive-type']:checked).val() " + $("input[name='archive-type']:checked").val());

  switch (parseInt($("input[name='archive-type']:checked").val())) {
    case 0:
      importer.converter = new GoogleConverter(this);
      break;

    default:
      importer.converter = new CarnetConverter(this);
  }

  importer.destPath = importer.converter.getDestPath();
  JSZip.loadAsync(archive).then(function (zip) {
    importer.currentZip = zip;
    importer.converter.getListOfNotesFromZip(zip, function (list) {
      document.getElementById("folder-picker").style.display = "none";
      $("#select-folder").hide();
      $("#note-selection-view").show();
      importer.fillNoteList(function () {}, list);
    });
  });
};

Importer.prototype.fillNoteList = function (callback, list) {
  var importer = this;
  $(this.progressView).show();
  var _iteratorNormalCompletion = true;
  var _didIteratorError = false;
  var _iteratorError = undefined;

  try {
    for (var _iterator = document.getElementsByClassName("import-button")[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
      var elem = _step.value;
      $(elem).hide();
    }
  } catch (err) {
    _didIteratorError = true;
    _iteratorError = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion && _iterator["return"] != null) {
        _iterator["return"]();
      }
    } finally {
      if (_didIteratorError) {
        throw _iteratorError;
      }
    }
  }

  $("#add-to-recent").hide();
  var table = document.createElement("table");
  table.classList.add("mdl-data-table");
  table.classList.add("mdl-js-data-table");
  table.classList.add("mdl-data-table--selectable");
  table.classList.add("mdl-shadow--2dp");
  var head = document.createElement("thead");
  table.appendChild(head);
  var tr = document.createElement("tr");
  head.appendChild(tr);
  var th = document.createElement("th");
  th.classList.add("mdl-data-table__cell--non-numeric");
  th.innerHTML = "Title";
  tr.appendChild(th);
  var tbody = document.createElement("tbody");
  table.appendChild(tbody);
  importer.elem.appendChild(table);
  var _iteratorNormalCompletion2 = true;
  var _didIteratorError2 = false;
  var _iteratorError2 = undefined;

  try {
    for (var _iterator2 = list[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
      var note = _step2.value;
      var tr = document.createElement("tr");
      tbody.appendChild(tr);
      var td = document.createElement("td");
      td.classList.add("mdl-data-table__cell--non-numeric");
      tr.appendChild(td);
      td.innerHTML = note;
      td = document.createElement("td");
      tr.appendChild(td);
      td.classList.add("value");
      td.innerHTML = note;
    }
  } catch (err) {
    _didIteratorError2 = true;
    _iteratorError2 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion2 && _iterator2["return"] != null) {
        _iterator2["return"]();
      }
    } finally {
      if (_didIteratorError2) {
        throw _iteratorError2;
      }
    }
  }

  new MaterialDataTable(table);
  $(importer.progressView).hide();
  var _iteratorNormalCompletion3 = true;
  var _didIteratorError3 = false;
  var _iteratorError3 = undefined;

  try {
    for (var _iterator3 = document.getElementsByClassName("import-button")[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
      var elem = _step3.value;
      $(elem).show();
    }
  } catch (err) {
    _didIteratorError3 = true;
    _iteratorError3 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion3 && _iterator3["return"] != null) {
        _iterator3["return"]();
      }
    } finally {
      if (_didIteratorError3) {
        throw _iteratorError3;
      }
    }
  }

  $("#add-to-recent").show();
  callback();
};

Importer.prototype.getSelectedNotes = function () {
  var toImport = [];
  var _iteratorNormalCompletion4 = true;
  var _didIteratorError4 = false;
  var _iteratorError4 = undefined;

  try {
    for (var _iterator4 = document.getElementsByClassName("value")[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
      var note = _step4.value;
      if (note.parentElement.getElementsByClassName("mdl-checkbox__input")[0].checked) toImport.push(note.innerText);
    }
  } catch (err) {
    _didIteratorError4 = true;
    _iteratorError4 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion4 && _iterator4["return"] != null) {
        _iterator4["return"]();
      }
    } finally {
      if (_didIteratorError4) {
        throw _iteratorError4;
      }
    }
  }

  return toImport;
};

Importer.prototype.readNext = function (callback) {
  if (this.dir.length == 0 || this.result.length == -1) {
    callback(true, this.result);
    return;
  }

  var importer = this;
  var fileName = this.dir.pop();

  var fs = require("fs");

  fs.readFile(this.path + "/" + fileName, 'base64', function (err, data) {
    if (err) {
      callback(false);
      return;
    }

    var buffer = new Buffer(data, 'base64');
    var file = buffer.toString();

    if (file.indexOf("<?xml") > -1) {
      var container = document.createElement("div");
      container.innerHTML = file;
      var thisTitle = container.querySelector("title").innerHTML;
      console.log(thisTitle);
      importer.result.push({
        title: thisTitle,
        path: importer.path + "/" + fileName
      });
    } else {
      console.log(fileName + " " + "unknown");
    }

    setTimeout(function () {
      importer.readNext(callback);
    }, 10);
  });
};

Importer.prototype.writeNext = function (callback) {
  if (this.toWrite.length <= 0) {
    callback();
    return;
  }

  var fs = require("fs");

  var importer = this;
  var toWrite = this.toWrite.pop();
  console.log("write to " + toWrite.path + " " + toWrite.type);

  var mkdirp = require('mkdirp');

  mkdirp.sync(FileUtils.getParentFolderFromPath(toWrite.path));
  fs.writeFile(toWrite.path, toWrite.data, {
    encoding: toWrite.type
  }, function (err) {
    console.log(err);
    importer.writeNext(callback);
  });
};

function DateError() {}

Importer.prototype.importNote = function (keepNotePath, destFolder, callback) {
  this.importingSpan.innerHTML = FileUtils.getFilename(keepNotePath) + " (" + this.notesToImport.length + " remaining)";
  this.converter.convertNoteToSQD(this.currentZip, keepNotePath, destFolder, function (zip, metadata, fileName, isPinned, path) {
    console.log("path " + path);
    if (zip != undefined) importer.sendNote(zip, metadata, fileName, isPinned, path, callback);else {
      importer.error.push(keepNotePath);
      callback();
    }
  });
};

Importer.prototype.onError = function (filename) {
  this.error.push(filename);
};

Importer.prototype.sendNote = function (blob, metadata, filename, isPinned, path, callback) {
  var self = this;
  console.log("metadata " + metadata);
  var files = [];
  blob.name = filename;
  files.push(blob);
  console.log("document.getElementById().checked " + document.getElementById("add-to-recent-cb").checked);
  RequestBuilder.sRequestBuilder.postFiles("/note/import", {
    add_to_recent: document.getElementById("add-to-recent-cb").checked,
    path: path,
    is_pinned: isPinned,
    metadata: metadata
  }, files, function (error, data) {
    console.log("error " + error);

    if (error) {
      self.onError(filename);
      callback();
    } else {
      self.imported = self.imported + 1;
      callback();
    }
  });
};

var importer;
$(document).ready(function () {
  importer = new Importer("/Keep");
});