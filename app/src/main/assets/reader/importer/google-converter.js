"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

var GoogleConverter = function GoogleConverter(importer) {};

GoogleConverter.prototype.convertNoteToSQD = function (currentZip, keepNotePath, destFolder, callback) {
  var fileName = FileUtils.getFilename(keepNotePath);
  if (fileName.endsWith(".json")) this.JsonConvertNoteToSQD(currentZip, keepNotePath, destFolder, callback);else this.XMLConvertNoteToSQD(currentZip, keepNotePath, destFolder, callback);
};

GoogleConverter.prototype.convertToBlob = function (zip, metadata, fileName, isPinned, callback) {
  zip.generateAsync({
    type: "blob"
  }).then(function (blob) {
    callback(blob, metadata, fileName, isPinned, "/Keep");
  });
};

GoogleConverter.prototype.getDestPath = function () {
  return "/Keep";
};

GoogleConverter.prototype.extractDateFromXML = function (dateDiv) {
  console.log(dateDiv.innerText.trim());
  console.log(escape(dateDiv.innerText.trim()).replace("%E0%20", ""));
  var date = dateDiv.innerText.trim(); //escape unescape only way I've found to replace the à which didn't work with simple replace("à ","")

  var fixedDate = unescape(escape(date).replace("%E0%20", "").replace("%E9", "e").replace("%FB", "u")).replace("at ", "").replace("à ", "").replace("&agrave; ", "").replace("juin", "june").replace("juil.", "july").replace("mars", "march").replace("oct.", "october").replace("jan.", "january").replace("janv.", "january").replace("sept.", "september").replace("déc.", "december").replace("dec.", "december").replace("fevr.", "february").replace("avr.", "april").replace("nov.", "november").replace("mai", "may").replace("aout", "august");
  console.log(fixedDate);
  var time = getDateFromFormat(fixedDate, "dd MMM yyyy HH:mm:ss");
  if (time == 0) //try different
    time = getDateFromFormat(fixedDate, "d MMM yyyy HH:mm:ss");
  if (time == 0) //try moment
    time = moment(fixedDate).toDate().getTime();
  return time;
};

GoogleConverter.prototype.XMLConvertNoteToSQD = function (currentZip, keepNotePath, destFolder, callback) {
  var fileName = FileUtils.getFilename(keepNotePath);
  var zip = new JSZip();
  var importer = this;
  var fileName = FileUtils.stripExtensionFromName(FileUtils.getFilename(keepNotePath)) + ".sqd";
  currentZip.files["Takeout/Keep/" + keepNotePath].async('text').then(function (txt) {
    var buffer = new Buffer(data, 'base64');
    var content = buffer.toString();
    var container = document.createElement("div");
    container.innerHTML = content;
    var titleDiv = container.querySelector("title");
    var title = "";
    if (titleDiv != null) title = titleDiv.innerHTML;
    console.log("title " + title);
    var textDiv = container.querySelector(".content");
    var text = "";
    if (textDiv != null) text = textDiv.innerHTML;
    var newNoteHTML = '<div id="text" contenteditable="true" style="height:100%;">\
        <!-- be aware that THIS will be modified in java -->\
        <!-- soft won\'t save note if contains -->' + text + '\
    </div>\
    <div id="floating">\
    \
    </div>';
    zip.file("index.html", JSON.stringify(newNoteHTML));
    console.log("text " + text);
    var labels = container.getElementsByClassName("label");
    var keywords = [];
    var dateDiv = container.querySelector(".heading");
    console.log(dateDiv.innerText.trim());
    console.log(escape(dateDiv.innerText.trim()).replace("%E0%20", ""));
    var date = dateDiv.innerText.trim(); //escape unescape only way I've found to replace the à which didn't work with simple replace("à ","")

    var fixedDate = unescape(escape(date).replace("%E0%20", "").replace("%E9", "e").replace("%FB", "u")).replace("at ", "").replace("à ", "").replace("&agrave; ", "").replace("juin", "june").replace("juil.", "july").replace("mars", "march").replace("oct.", "october").replace("jan.", "january").replace("janv.", "january").replace("sept.", "september").replace("déc.", "december").replace("dec.", "december").replace("fevr.", "february").replace("avr.", "april").replace("nov.", "november").replace("mai", "may").replace("aout", "august");
    console.log(fixedDate);
    var time = getDateFromFormat(fixedDate, "dd MMM yyyy HH:mm:ss");
    if (time == 0) //try different
      time = getDateFromFormat(fixedDate, "d MMM yyyy HH:mm:ss");
    if (time == 0) //try moment
      time = moment(fixedDate).toDate().getTime();
    if (time == 0) throw new DateError();
    var notePath = destFolder + "/" + (title == date ? "untitled" : "") + FileUtils.stripExtensionFromName(fileName) + ".sqd";
    console.log(time);

    if (labels != undefined) {
      var _iterator = _createForOfIteratorHelper(labels),
          _step;

      try {
        for (_iterator.s(); !(_step = _iterator.n()).done;) {
          var label = _step.value;
          if (keywords.indexOf(label.innerText) >= 0) continue;
          keywords.push(label.innerText);
          console.log("label " + label.innerText);
          importer.timeStampedKeywords.push({
            action: 'add',
            time: time,
            path: notePath.substring((importer.notePath + '/').length),
            keyword: label.innerText
          });
        }
      } catch (err) {
        _iterator.e(err);
      } finally {
        _iterator.f();
      }
    }

    var metadata = {
      creation_date: time,
      last_modification_date: time,
      keywords: keywords
    };
    console.log("meta " + JSON.stringify(metadata));
    zip.file("metadata.json", JSON.stringify(metadata)); //attachments

    var attachments = container.querySelector(".attachments");

    if (attachments != undefined) {
      var audioFiles = attachments.getElementsByClassName("audio");

      if (audioFiles != undefined) {
        var _iterator2 = _createForOfIteratorHelper(audioFiles),
            _step2;

        try {
          for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
            var audioFile = _step2.value;
            var data = audioFile.getAttribute("href");
            zip.file("data/" + generateUID() + "." + FileUtils.getExtensionFromMimetype(FileUtils.base64MimeType(data)), data.substr(data.indexOf(',') + 1), {
              base64: true
            });
          }
        } catch (err) {
          _iterator2.e(err);
        } finally {
          _iterator2.f();
        }
      }

      var imgFiles = attachments.getElementsByTagName("img");

      if (imgFiles != undefined) {
        var _iterator3 = _createForOfIteratorHelper(imgFiles),
            _step3;

        try {
          for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
            var imageFile = _step3.value;
            console.log("adding img1");
            var data = imageFile.getAttribute("src");
            console.log("adding img");
            zip.file("data/" + generateUID() + "." + FileUtils.getExtensionFromMimetype(FileUtils.base64MimeType(data)), data.substr(data.indexOf(',') + 1), {
              base64: true
            });
          }
        } catch (err) {
          _iterator3.e(err);
        } finally {
          _iterator3.f();
        }
      }
    }

    importer.convertToBlob(zip, metadata, fileName, undefined, callback);
  });
};

GoogleConverter.prototype.getListOfNotesFromZip = function (zip, callback) {
  var list = [];
  zip.folder("Takeout/Keep").forEach(function (relativePath, zipEntry) {
    // 2) print entries
    console.log("note " + relativePath);
    if (relativePath.endsWith(".json")) list.push(relativePath);
  });
  callback(list);
};

GoogleConverter.prototype.JsonConvertNoteToSQD = function (currentZip, keepNotePath, destFolder, callback) {
  var importer = this;
  var originalFilename = FileUtils.stripExtensionFromName(FileUtils.getFilename(keepNotePath));
  var filename = FileUtils.stripExtensionFromName(FileUtils.getFilename(keepNotePath)) + ".sqd";
  currentZip.files["Takeout/Keep/" + keepNotePath].async('text').then(function (txt) {
    var json = JSON.parse(txt);
    console.log(json['title']);
    var zip = new JSZip();
    zip.file("index.html", '<div id="text" contenteditable="true" style="height:100%;">\
            <!-- be aware that THIS will be modified in java -->\
            <!-- soft won\'t save note if contains -->' + Utils.nl2br(json['textContent']) + '\
        </div>\
        <div id="floating">\
        \
        </div>');
    if (json['title'] == "" || json['title'] === undefined) filename = "untitled " + filename;
    var mdate = Math.round(json['userEditedTimestampUsec'] / 1000);

    if (mdate <= 0) {
      var dateStr = originalFilename;
      dateStr = dateStr.replace(/_/g, ":");
      mdate = Date.parse(dateStr);

      if (!(mdate > 0)) {
        /*console.log("mdate " + mdate)
        //try to retrieve from xml
        var xmlContent = currentZip.files["Takeout/Keep/" + originalFilename + ".html"].async('text').then(function (txt) {
            var container = document.createElement("div");
            container.innerHTML = xmlContent
            var dateDiv = container.querySelector(".heading");
             console.log("extracted date " + importer.extractDateFromXML(dateDiv))
        })
         return*/
        importer.onDateAvailable(mdate, json, zip, filename, currentZip, keepNotePath, destFolder, callback);
      } else importer.onDateAvailable(mdate, json, zip, filename, currentZip, keepNotePath, destFolder, callback);
    } else importer.onDateAvailable(mdate, json, zip, filename, currentZip, keepNotePath, destFolder, callback);
  });
};

GoogleConverter.prototype.onDateAvailable = function (date, json, zip, filename, currentZip, keepNotePath, destFolder, callback) {
  var importer = this;
  var metadata = {};
  metadata['creation_date'] = date;
  metadata['title'] = json['title'];
  metadata['last_modification_date'] = date;
  console.log("date " + date);
  metadata['keywords'] = [];
  metadata['rating'] = -1;
  metadata['color'] = "none";
  metadata['urls'] = {};
  metadata['todolists'] = [];

  if (json['labels'] != undefined) {
    var _iterator4 = _createForOfIteratorHelper(json['labels']),
        _step4;

    try {
      for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
        var label = _step4.value;
        metadata['keywords'].push(label.name);
      }
    } catch (err) {
      _iterator4.e(err);
    } finally {
      _iterator4.f();
    }
  }

  if (json['annotations'] != undefined) {
    var _iterator5 = _createForOfIteratorHelper(json['annotations']),
        _step5;

    try {
      for (_iterator5.s(); !(_step5 = _iterator5.n()).done;) {
        var annotation = _step5.value;

        if (annotation['url'] != undefined) {
          var url = {};
          url['title'] = annotation['title'];
          url['description'] = annotation['description'];
          metadata['urls'][annotation['url']] = url;
        }
      }
    } catch (err) {
      _iterator5.e(err);
    } finally {
      _iterator5.f();
    }
  }

  if (json['listContent'] != undefined) {
    var todoList = {
      "id": "todolist" + generateUID()
    };
    todoList.todo = [];
    todoList.done = [];
    todoList.todoIds = [];
    todoList.doneIds = [];
    todoList.stats = [];

    var _iterator6 = _createForOfIteratorHelper(json['listContent']),
        _step6;

    try {
      for (_iterator6.s(); !(_step6 = _iterator6.n()).done;) {
        var todos = _step6.value;

        if (todos['text'] != undefined) {
          if (todos['text'] != undefined && !todos['isChecked']) {
            todoList.todo.push(todos['text']);
            todoList.todoIds.push(generateUID());
          } else {
            todoList.done.push(todos['text']);
            todoList.doneIds.push(generateUID());
          }
        }
      }
    } catch (err) {
      _iterator6.e(err);
    } finally {
      _iterator6.f();
    }

    metadata['todolists'].push(todoList);
  }

  zip.file("metadata.json", JSON.stringify(metadata));
  importer.importNoteAttachments(currentZip, zip, json['attachments'], function () {
    console.log("json['isPinned'] " + json['isPinned']);
    importer.convertToBlob(zip, JSON.stringify(metadata), filename, json['isPinned'], callback);
  });
};

GoogleConverter.prototype.importNoteAttachments = function (currentZip, destZip, attachments, callback) {
  this.importNoteAttachment(currentZip, destZip, attachments, callback);
};

GoogleConverter.prototype.importNoteAttachment = function (currentZip, destZip, attachments, callback) {
  if (attachments == undefined || attachments.length <= 0) {
    callback();
    return;
  }

  var importer = this;
  var attachment = attachments.pop();
  var attachmentName = attachment['filePath'];
  if (attachmentName.endsWith("jpeg")) attachmentName = attachmentName.substr(0, attachmentName.length - 4) + "jpg";
  console.log("attachment " + "Takeout/Keep/" + attachmentName);
  currentZip.files["Takeout/Keep/" + attachmentName].async("base64").then(function (data) {
    destZip.file("data/" + attachmentName, data, {
      base64: true
    });

    if (FileUtils.isFileImage(attachmentName)) {
      var img = new Image();

      img.onload = function () {
        var maxWidth = 400,
            maxHeight = 400,
            imageWidth = img.width,
            imageHeight = img.height;

        if (imageWidth > imageHeight) {
          if (imageWidth > maxWidth) {
            imageHeight *= maxWidth / imageWidth;
            imageWidth = maxWidth;
          }
        } else if (imageHeight > maxHeight) {
          imageWidth *= maxHeight / imageHeight;
          imageHeight = maxHeight;
        }

        var canvas = document.createElement('canvas');
        canvas.width = imageWidth;
        canvas.height = imageHeight;
        this.width = imageWidth;
        this.height = imageHeight;
        var ctx = canvas.getContext("2d");
        ctx.drawImage(this, 0, 0, imageWidth, imageHeight); //console.log(canvas.toDataURL('image/jpeg').substr("data:image/jpeg;base64,".length));

        destZip.file("data/" + "preview_" + attachmentName + ".jpg", canvas.toDataURL('image/jpeg').substr("data:image/jpeg;base64,".length), {
          base64: true
        });
        importer.importNoteAttachment(currentZip, destZip, attachments, callback);
      };

      img.src = "data:" + attachment['mimetype'] + ";base64," + data;
    } else importer.importNoteAttachment(currentZip, destZip, attachments, callback);
  });
};