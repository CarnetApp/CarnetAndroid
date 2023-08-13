"use strict";

function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i]; return arr2; }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, _toPropertyKey(descriptor.key), descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _toPropertyKey(arg) { var key = _toPrimitive(arg, "string"); return _typeof(key) === "symbol" ? key : String(key); }
function _toPrimitive(input, hint) { if (_typeof(input) !== "object" || input === null) return input; var prim = input[Symbol.toPrimitive]; if (prim !== undefined) { var res = prim.call(input, hint || "default"); if (_typeof(res) !== "object") return res; throw new TypeError("@@toPrimitive must return a primitive value."); } return (hint === "string" ? String : Number)(input); }
var writer = undefined;
var SingleExporter = /*#__PURE__*/function () {
  function SingleExporter(notepath, listener) {
    _classCallCheck(this, SingleExporter);
    this.notepath = notepath;
    this.listener = listener;
  }
  _createClass(SingleExporter, [{
    key: "retrieveNote",
    value: function retrieveNote(callback) {
      console.log("SingleExporter retrieveNote");
      this.listener.onRetrievingNote();
      if (!compatibility.isElectron) {
        var oReq = new XMLHttpRequest();
        oReq.open("GET", compatibility.addRequestToken(RequestBuilder.sRequestBuilder.api_url + RequestBuilder.sRequestBuilder.cleanPath("note/get_note?path=" + encodeURIComponent(this.notepath))), true);
        oReq.responseType = "arraybuffer";
        oReq.onload = function (oEvent) {
          var arrayBuffer = oReq.response; // Note: not oReq.responseText
          callback(arrayBuffer, false);
        };
        oReq.send(null);
      } else {
        RequestBuilder.sRequestBuilder.get("note/get_note?path=" + encodeURIComponent(this.notepath), function (error, data) {
          callback(data, true);
        });
      }
    }
  }, {
    key: "loadZipContent",
    value: function loadZipContent(zip, callback) {
      var exporter = this;
      this.currentZip = zip;
      this.files = Object.keys(zip.files);
      this.html = "";
      this.metadata = {};
      this.attachments = [];
      this.loadNextZipFile(this.files.pop(), function () {
        callback(exporter.html, exporter.metadata, exporter.attachments);
      });
    }
  }, {
    key: "loadNextZipFile",
    value: function loadNextZipFile(path, callback) {
      if (path == undefined) {
        callback();
        return;
      }
      console.log("extracting path " + path + path.indexOf("data/"));
      var exporter = this;
      if (path.indexOf("data/") == 0 && path.indexOf("data/preview_") != 0 && path != "data/") {
        this.currentZip.file(path).async("base64").then(function (data) {
          var attachment = {};
          attachment['name'] = FileUtils.getFilename(path);
          attachment['data'] = data;
          exporter.attachments.push(attachment);
          exporter.loadNextZipFile(exporter.files.pop(), callback);
        });
      } else if (path == "index.html") {
        this.currentZip.file(path).async("string").then(function (data) {
          console.log("index " + data);
          exporter.html = data;
          exporter.loadNextZipFile(exporter.files.pop(), callback);
        });
      } else if (path == "metadata.json") {
        this.currentZip.file(path).async("string").then(function (data) {
          console.log("metadata " + data);
          exporter.metadata = JSON.parse(data);
          exporter.loadNextZipFile(exporter.files.pop(), callback);
        });
      } else {
        console.log("else");
        exporter.loadNextZipFile(exporter.files.pop(), callback);
      }
    }
  }, {
    key: "exportAsPDF",
    value: function exportAsPDF(callback) {
      var exporter = this;
      this.exportAsHtml(false, false, function (html, metadata, attachments) {
        var doc = new jspdf.jsPDF({
          orientation: 'p',
          format: 'a4'
        });
        var specialElementHandlers = {
          '#editor': function editor(element, renderer) {
            return true;
          }
        };
        html.id = "editor";
        var elementHTML = $(html).html();
        document.body.appendChild(html);
        doc.html(html, {
          callback: function callback(doc) {
            //   doc.addImage(attachments[0].data)
            doc.output('datauri');
            download(FileUtils.stripExtensionFromName(FileUtils.getFilename(exporter.notepath)) + ".pdf", undefined, undefined, doc.output('datauri'));
          }
        });
      });
    }
  }, {
    key: "exportAndDownloadAsHtml",
    value: function exportAndDownloadAsHtml(config, share) {
      var exporter = this;
      this.exportAsHtml(config, false, function (htmlElem, metadata, attachments) {
        exporter.download(FileUtils.stripExtensionFromName(FileUtils.getFilename(exporter.notepath)) + ".html", "<!DOCTYPE html>\n<html>" + htmlElem.innerHTML + '</html>', "text/html", undefined, share);
      });
    }
  }, {
    key: "print",
    value: function print(config) {
      this.exportAsHtml(config, false, function (htmlElem, metadata, attachments) {
        compatibility.print(htmlElem);
      });
    }
  }, {
    key: "exportAsHtml",
    value: function exportAsHtml(config, noImages, callback) {
      var exporter = this;
      exporter.listener.onExportStarts();
      this.retrieveNote(function (data, base64) {
        JSZip.loadAsync(data, {
          base64: base64
        }).then(function (zip) {
          exporter.loadZipContent(zip, function (html, metadata, attachments) {
            var htmlElem = document.createElement("html");
            var head = document.createElement("head");
            head.innerHTML = "<meta charset=\"UTF-8\">\
                    <meta name=\"viewport\" content=\"width=device-width, height=device-height, user-scalable=no\" />";
            if (!noImages) {
              head.innerHTML += "<style>body{max-width:1000px; margin:auto; }#media-list{white-space: nowrap; overflow-x: auto;}#media-list img{max-height:300px;margin-right:5px;} #full-media-list img{max-width:100%;} </style>";
            }
            var todolistStyle = "<style></style>";
            head.innerHTML += todolistStyle;
            htmlElem.appendChild(head);
            var body = document.createElement("body");
            if (attachments.length > 0) {
              if (config.displayImages) {
                var mediaList = document.createElement("div");
                if (!config.displayCompleteImages) {
                  mediaList.id = "media-list";
                  console.log("small iamges");
                } else {
                  mediaList.id = "full-media-list";
                }
                var _iterator = _createForOfIteratorHelper(attachments),
                  _step;
                try {
                  for (_iterator.s(); !(_step = _iterator.n()).done;) {
                    var attachment = _step.value;
                    var a = document.createElement("a");
                    var base64ref = "data:" + FileUtils.geMimetypeFromExtension(FileUtils.getExtensionFromPath(attachment.name)) + ";base64," + attachment.data;
                    if (FileUtils.isFileImage(attachment.name)) {
                      var img = document.createElement("img");
                      img.src = base64ref;
                      a.classList.add("img-link");
                      a.appendChild(img);
                    } else {
                      a.href = base64ref;
                    }
                    mediaList.appendChild(a);
                  }
                } catch (err) {
                  _iterator.e(err);
                } finally {
                  _iterator.f();
                }
                body.appendChild(mediaList);
              }
            }
            var dateC = new Date(metadata.creation_date);
            var dateM = new Date(metadata.last_modification_date);
            if (config.displayTitle) body.innerHTML += "<h3>" + FileUtils.stripExtensionFromName(FileUtils.getFilename(exporter.notepath)) + "<h3>";
            if (config.displayCreationDate) body.innerHTML += "<span>" + $.i18n('created') + ": " + dateC.toLocaleDateString() + " " + dateC.toLocaleTimeString() + "</span><br />";
            if (config.displayModificationDate) body.innerHTML += "<span>" + $.i18n('modified') + ": " + dateM.toLocaleDateString() + " " + dateM.toLocaleTimeString() + "</span><br />";
            var text = document.createElement("div");
            text.id = "whole-text";
            text.innerHTML = "<br /><br />" + html;
            var _iterator2 = _createForOfIteratorHelper(metadata.todolists),
              _step2;
            try {
              for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
                var todolist = _step2.value;
                var todolistContainer = text.querySelector("#" + todolist.id);
                if (todolistContainer == undefined) {
                  todolistContainer = document.createElement("div");
                  text.querySelector("#text").appendChild(todolistContainer);
                }
                todolistContainer.innerHTML += "<h3>" + $.i18n('todo') + "</h3>";
                var _iterator3 = _createForOfIteratorHelper(todolist.todo),
                  _step3;
                try {
                  for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
                    var todo = _step3.value;
                    todolistContainer.innerHTML += "☐ " + todo + "<br />";
                  }
                } catch (err) {
                  _iterator3.e(err);
                } finally {
                  _iterator3.f();
                }
                todolistContainer.innerHTML += "<h3>" + $.i18n('completed') + "</h3>";
                var _iterator4 = _createForOfIteratorHelper(todolist.done),
                  _step4;
                try {
                  for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
                    var done = _step4.value;
                    todolistContainer.innerHTML += "☑ " + done + "<br />";
                  }
                } catch (err) {
                  _iterator4.e(err);
                } finally {
                  _iterator4.f();
                }
              }
            } catch (err) {
              _iterator2.e(err);
            } finally {
              _iterator2.f();
            }
            body.appendChild(text);
            body.innerHTML += "<script> \
                    for(var link of document.getElementsByClassName('img-link')){\
                     link.onclick=function(event){\
                        alert('right click on the image to download it');\
                     }\
                    }\
                    </script>\
                    ";
            htmlElem.appendChild(body);
            exporter.listener.onExportFinished();
            callback(htmlElem, metadata, attachments);
          });
        });
        console.log("SingleExporter retrieving: success");
      });
    }
  }, {
    key: "download",
    value: function download(filename, text, mimetype, datauri, share) {
      this.listener.onDownloadStarts();
      if (compatibility.isAndroid) {
        compatibility.largeDownload = text;
        compatibility.onDownloadFinished = this.listener.onDownloadFinished;
        app.startLargeDownload(filename, mimetype, share);
      } else {
        var element = document.createElement('a');
        if (datauri == undefined) element.setAttribute('href', 'data:' + mimetype + ';charset=utf-8,' + encodeURIComponent(text));else element.setAttribute('href', datauri);
        element.setAttribute('download', filename);
        element.style.display = 'none';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
        this.listener.onDownloadFinished();
      }
    }
  }]);
  return SingleExporter;
}();
function css(a) {
  var sheets = document.styleSheets,
    o = {};
  for (var i in sheets) {
    var rules = sheets[i].rules || sheets[i].cssRules;
    for (var r in rules) {
      if (a.is(rules[r].selectorText)) {
        o = $.extend(o, css2json(rules[r].style), css2json(a.attr('style')));
      }
    }
  }
  return o;
}
function css2json(css) {
  var s = {};
  if (!css) return s;
  if (css instanceof CSSStyleDeclaration) {
    for (var i in css) {
      if (css[i].toLowerCase) {
        s[css[i].toLowerCase()] = css[css[i]];
      }
    }
  } else if (typeof css == "string") {
    css = css.split("; ");
    for (var i in css) {
      var l = css[i].split(": ");
      s[l[0].toLowerCase()] = l[1];
    }
  }
  return s;
}
var ExporterUI = /*#__PURE__*/function () {
  function ExporterUI() {
    _classCallCheck(this, ExporterUI);
    var exporterUI = this;
    var path = Utils.getParameterByName("path");
    this.exporter = new SingleExporter(path, this);
    this.downloadButton = document.getElementById("download");
    this.sendButton = document.getElementById("send");
    this.printButton = document.getElementById("print");
    this.loadingView = document.getElementById("loading");
    document.getElementById("photos-checkbox").onchange = function () {
      exporterUI.sendButton.disabled = document.getElementById("photos-checkbox").checked;
    };
    this.downloadButton.onclick = function () {
      console.log("download");
      exporterUI.setButtonDisable(true);
      var config = exporterUI.getConfig();
      exporterUI.exporter.exportAndDownloadAsHtml(config, false);
    };
    this.printButton.onclick = function () {
      var config = exporterUI.getConfig();
      config.displayCompleteImages = true;
      exporterUI.setButtonDisable(true);
      exporterUI.exporter.print(config);
    };
    this.sendButton.onclick = function () {
      console.log("download");
      exporterUI.setButtonDisable(true);
      var config = exporterUI.getConfig();
      exporterUI.exporter.exportAndDownloadAsHtml(config, true);
    };
    if (!compatibility.isAndroid) {
      this.sendButton.style.display = "none";
    }
    compatibility.loadLang(function () {
      $('body').i18n();
    });
    $.i18n().locale = navigator.language;
  }
  _createClass(ExporterUI, [{
    key: "setLoadingViewVisibility",
    value: function setLoadingViewVisibility(show) {
      if (show) $(this.loadingView).show();else $(this.loadingView).hide();
    }
  }, {
    key: "setButtonDisable",
    value: function setButtonDisable(disabled) {
      this.downloadButton.disabled = disabled;
      this.printButton.disabled = disabled;
      if (!disabled) this.sendButton.disabled = document.getElementById("photos-checkbox").checked;else this.sendButton.disabled = disabled;
    }
  }, {
    key: "getConfig",
    value: function getConfig() {
      var config = {};
      config.displayTitle = document.getElementById("title-checkbox").checked;
      config.displayModificationDate = document.getElementById("mod-checkbox").checked;
      config.displayCreationDate = document.getElementById("creation-checkbox").checked;
      config.displayImages = document.getElementById("photos-checkbox").checked;
      return config;
    }
  }, {
    key: "onError",
    value: function onError(error) {
      this.setLoadingViewVisibility(false);
    }
  }, {
    key: "onRetrievingNote",
    value: function onRetrievingNote() {
      this.setLoadingViewVisibility(true);
    }
  }, {
    key: "onDownloadStarts",
    value: function onDownloadStarts() {
      //this.setLoadingViewVisibility(true)
    }
  }, {
    key: "onDownloadFinished",
    value: function onDownloadFinished() {
      this.setLoadingViewVisibility(false);
    }
  }, {
    key: "onExportFinished",
    value: function onExportFinished() {
      this.setLoadingViewVisibility(false);
      this.setButtonDisable(false);
    }
  }, {
    key: "onExportStarts",
    value: function onExportStarts() {
      this.setLoadingViewVisibility(true);
    }
  }]);
  return ExporterUI;
}();
new RequestBuilder((compatibility.isAndroid ? "../" : "") + Utils.getParameterByName("api_path"));
new ExporterUI();