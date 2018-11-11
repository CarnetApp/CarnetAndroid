"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

var rootpath = undefined;
var api_url = undefined;

var Writer = function Writer(elem) {
  this.elem = elem;
  this.seriesTaskExecutor = new SeriesTaskExecutor();
  this.saveNoteTask = new SaveNoteTask(this);
  this.hasTextChanged = false;
  resetScreenHeight();
  console.log("create Writer");
};

Writer.prototype.setNote = function (note) {
  this.note = note;
};

Writer.prototype.displayMediaFullscreen = function (index) {
  var writer = this;
  var imgContainer = document.createElement("div");
  imgContainer.setAttribute("id", "fullimg_container");
  var img = document.createElement("img");
  img.src = this.fullscreenableMedia[index];
  $(img).on('load', function () {
    img.style.marginTop = "-" + $(this).height() / 2 + "px";
    img.style.marginLeft = "-" + $(this).width() / 2 + "px";
    console.log(img.height);
    $(img).imgViewer();
  });
  img.style.top = "50%";
  img.style.left = "50%";
  img.setAttribute("id", "fullimage"); // img.style.position = "absolute"

  this.fullscreenViewer.innerHTML = "";
  imgContainer.appendChild(img);
  this.fullscreenViewer.appendChild(imgContainer);
  var toolbar = document.createElement("div"); //insert

  var insertButton = document.createElement("button");

  insertButton.onclick = function (e) {
    e.stopPropagation();
    $(writer.fullscreenViewer).hide("slow");
    return fa;
  };

  insertButton.classList.add('mdl-button');
  insertButton.classList.add('mdl-js-button');
  insertButton.innerHTML = "insert";

  insertButton.onclick = function () {
    writer.warnNotYetImplemented;
  };

  toolbar.setAttribute("id", "toolbar");
  toolbar.appendChild(insertButton); //download

  var a = document.createElement("a");
  a.href = "#";
  a.download = "true"; // force download, not view

  a.target = "_blank";

  a.onclick = function () {
    window.open(writer.fullscreenableMedia[index], '_blank');
    ;
    return false;
  };

  var downloadButton = document.createElement("button");
  downloadButton.classList.add('mdl-button');
  downloadButton.classList.add('mdl-js-button');
  downloadButton.classList.add('mdl-button--icon');
  var imgD = document.createElement("img");
  imgD.src = rootpath + "/img/ic_file_download_white_24px.svg";
  downloadButton.appendChild(imgD);
  a.appendChild(downloadButton);
  toolbar.appendChild(a); //close

  var closeButton = document.createElement("button");

  closeButton.onclick = function (e) {
    e.stopPropagation();
    $(writer.fullscreenViewer).hide("slow");
    return false;
  };

  closeButton.classList.add('mdl-button');
  closeButton.classList.add('mdl-js-button');
  closeButton.classList.add('mdl-button--icon');
  var imgC = document.createElement("img");
  imgC.src = rootpath + "/img/ic_close_white_24px.svg";
  closeButton.appendChild(imgC);
  toolbar.appendChild(closeButton);
  this.fullscreenViewer.appendChild(toolbar);
  this.fullscreenViewer.toolbar = toolbar;
  $(this.fullscreenViewer).fadeIn("slow");
  this.fullscreenViewer.style.display = "table-cell";
  this.currentFullscreen = index;

  this.fullscreenViewer.onclick = function () {
    if ($(toolbar).is(":visible")) $(toolbar).slideUp("fast");else $(toolbar).slideDown("fast"); //  $(writer.fullscreenViewer).hide("slow")
  };
};

Writer.prototype.previousMedia = function () {
  if (this.currentFullscreen > 0) this.displayMediaFullscreen(this.currentFullscreen - 1);
};

Writer.prototype.nextMedia = function () {
  if (this.currentFullscreen < this.fullscreenableMedia.length - 1) this.displayMediaFullscreen(this.currentFullscreen + 1);
};

Writer.prototype.setMediaList = function (list) {
  writer.currentFullscreen = 0;
  writer.fullscreenableMedia = [];
  writer.mediaList.innerHTML = "";
  var mediaCount = 0;
  if (list == undefined) list = [];

  if (list.length > 0) {
    this.addMediaMenu.parentNode.style.left = "unset";
  } else {
    this.addMediaMenu.parentNode.style.left = "0px";
  }

  var _loop = function _loop() {
    var filePath = list[i];
    var name = FileUtils.getFilename(list[i]);
    console.log("file " + filePath);
    el = document.createElement("div");
    el.classList.add("media");

    if (FileUtils.isFileImage(filePath)) {
      if (!filePath.startsWith("preview_")) {
        img = document.createElement("img");
        img.src = filePath;
        el.appendChild(img);
        writer.fullscreenableMedia.push(filePath);
        img.mediaIndex = mediaCount;

        el.onclick = function (event) {
          console.log(event.target);
          writer.displayMediaFullscreen(event.target.mediaIndex);
        };

        mediaCount++;
      }
    } else {
      img = document.createElement("img");
      img.src = rootpath + "/img/file.svg";
      el.appendChild(img);
      el.innerHTML += "<br /> " + name.substr(0, 15);
      el.classList.add("media-file");
      el.filePath = filePath;

      if (filePath.endsWith("opus")) {
        el.onclick = function () {
          writer.recorder.setAudioUrl(filePath, name);
          writer.recorderDialog.showModal();
        };
      }
    }

    writer.mediaList.appendChild(el);
  };

  for (var i = 0; i < list.length; i++) {
    var el;
    var img;
    var img;

    _loop();
  }
};

Writer.prototype.refreshMedia = function () {
  var writer = this;
  RequestBuilder.sRequestBuilder.get("/note/open/" + this.saveID + "/listMedia", function (error, data) {
    if (error) {}

    writer.setMediaList(data);
  });
};

Writer.prototype.deleteMedia = function (name) {
  console.log("name " + name);
  var writer = this;
  RequestBuilder.sRequestBuilder.delete("/note/open/" + this.saveID + "/media?path=" + encodeURIComponent(this.note.path) + "&media=" + encodeURIComponent(name), function (error, data) {
    if (!error) writer.setMediaList(data);
  });
};

Writer.prototype.sendFiles = function (files, callback) {
  $("#media-loading").fadeIn();
  var writer = this;
  RequestBuilder.sRequestBuilder.postFiles("/note/open/" + this.saveID + "/addMedia", {
    path: this.note.path
  }, files, function (error, data) {
    if (error) {}

    $("#media-loading").fadeOut();
    writer.setMediaList(data);
    if (callback) callback(data);
  });
};

Writer.prototype.addMedia = function () {
  console.log("add media");
  document.getElementById("input_file").click();
};

Writer.prototype.setDoNotEdit = function (b) {
  document.getElementById("text").contentEditable = !b;
  document.getElementById("name-input").disabled = b;
};

Writer.prototype.displayErrorLarge = function (error) {};

Writer.prototype.extractNote = function () {
  console.log("Writer.prototype.extractNote");
  var writer = this;
  RequestBuilder.sRequestBuilder.get("/note/open?path=" + encodeURIComponent(this.note.path), function (error, data) {
    if (error) {
      writer.setDoNotEdit(true);
      writer.displayErrorLarge("Error");
      return;
    }

    console.log(data);
    writer.saveID = data.id;
    writer.fillWriter(data.html);
    if (data.metadata == null) writer.note.metadata = new NoteMetadata();else writer.note.metadata = data.metadata;
    writer.refreshKeywords();
    writer.refreshMedia();
    var ratingStars = document.querySelectorAll("input.star");

    for (var i = 0; i < ratingStars.length; i++) {
      ratingStars[i].checked = writer.note.metadata.rating == 5 - i;
    }

    ;
    writer.updateRating(writer.note.metadata.rating);
  });
};

var saveTextIfChanged = function saveTextIfChanged() {
  console.log("has text changed ? " + writer.hasTextChanged);
  if (writer.hasTextChanged) writer.seriesTaskExecutor.addTask(writer.saveNoteTask);
  writer.hasTextChanged = false;
};

Writer.prototype.fillWriter = function (extractedHTML) {
  console.log("fill");
  if (extractedHTML != undefined) this.oEditor.innerHTML = extractedHTML;
  document.getElementById("name-input").value = FileUtils.stripExtensionFromName(FileUtils.getFilename(this.note.path));

  this.oEditor.onscroll = function () {
    lastscroll = $(writer.oEditor).scrollTop();
    console.log("onscroll");
  };

  this.oDoc = document.getElementById("text");
  this.oFloating = document.getElementById("floating");
  var writer = this;
  this.oDoc.addEventListener("input", function () {
    writer.hasTextChanged = true;
  }, false);
  this.saveInterval = setInterval(saveTextIfChanged, 2000);
  this.sDefTxt = this.oDoc.innerHTML;
  /*simple initialization*/

  this.oDoc.focus();
  resetScreenHeight();
  this.refreshKeywords(); //  $("#editor").webkitimageresize().webkittableresize().webkittdresize();

  compatibility.onNoteLoaded();
}; //var KeywordsDBManager = require(rootpath + "keywords/keywords_db_manager").KeywordsDBManager;


var keywordsDBManager = new KeywordsDBManager();

Writer.prototype.refreshKeywords = function () {
  var keywordsContainer = document.getElementById("keywords-list");
  keywordsContainer.innerHTML = "";
  var writer = this;

  for (var i = 0; i < this.note.metadata.keywords.length; i++) {
    var word = this.note.metadata.keywords[i];
    var keywordElem = document.createElement("a");
    keywordElem.classList.add("mdl-navigation__link");
    keywordElem.innerHTML = word;
    keywordsContainer.appendChild(keywordElem);
    keywordElem.word = word;
    keywordElem.addEventListener('click', function () {
      writer.removeKeyword(this.word);
    });
  }

  keywordsDBManager.getFlatenDB(function (error, data) {
    writer.availableKeyword = data;
  });
};

Writer.prototype.simulateKeyPress = function (character) {
  $.event.trigger({
    type: 'keypress',
    which: character.charCodeAt(0)
  });
};

Writer.prototype.formatDoc = function (sCmd, sValue) {
  this.oEditor.focus();
  document.execCommand(sCmd, false, sValue);
  this.oEditor.focus();
};

Writer.prototype.displayTextColorPicker = function () {
  var writer = this;
  this.displayColorPicker(function (color) {
    writer.setColor(color);
  });
};

Writer.prototype.displayFillColorPicker = function () {
  var writer = this;
  this.displayColorPicker(function (color) {
    writer.fillColor(color);
  });
};

var currentColor = undefined;

Writer.prototype.setPickerColor = function (picker) {
  currentColor = "#" + picker.toString();
};

Writer.prototype.displayColorPicker = function (callback) {
  var call = function call() {
    writer.colorPickerDialog.querySelector('.ok').removeEventListener('click', call);
    writer.colorPickerDialog.close();
    callback(currentColor);
  };

  this.colorPickerDialog.querySelector('.ok').addEventListener('click', call);
  this.colorPickerDialog.showModal();
  document.getElementById('color-picker-div').show();
};

Writer.prototype.displayStyleDialog = function () {
  this.styleDialog.showModal();
};

Writer.prototype.warnNotYetImplemented = function () {
  var data = {
    message: 'Not yet implemented',
    timeout: 5000
  };
  this.displaySnack(data);
  return false;
};

Writer.prototype.displaySnack = function (data) {
  var snackbarContainer = document.querySelector('#snackbar');
  if (!(_typeof(snackbarContainer.MaterialSnackbar) == undefined)) snackbarContainer.MaterialSnackbar.showSnackbar(data);
};

Writer.prototype.init = function () {
  var writer = this;
  this.recorder = new CarnetRecorder();

  window.onerror = function myErrorHandler(errorMsg, url, lineNumber) {
    if (errorMsg.indexOf("parentElement") >= 0) //ignore that one
      return;
    if (["TypeError: firstHeader is null"].includes(errorMsg)) return;
    var data = {
      message: "Error occured: " + errorMsg,
      timeout: 5000
    };
    writer.displaySnack(data);
    return false;
  };

  document.execCommand('styleWithCSS', false, true);

  document.getElementById("input_file").onchange = function () {
    writer.sendFiles(this.files);
  };

  this.statsDialog = this.elem.querySelector('#statsdialog');
  this.showDialogButton = this.elem.querySelector('#show-dialog');
  var dias = document.getElementsByClassName("mdl-dialog");

  for (var i = 0; i < dias.length; i++) {
    if (!dias[i].showModal) dialogPolyfill.registerDialog(dias[i]);
  }

  this.statsDialog.querySelector('.ok').addEventListener('click', function () {
    writer.statsDialog.close();
  });
  this.colorPickerDialog = this.elem.querySelector('#color-picker-dialog');
  this.styleDialog = this.elem.querySelector('#style-dialog');
  this.recorderDialog = this.elem.querySelector('#recorder-container');
  this.newKeywordDialog = this.elem.querySelector('#new-keyword-dialog');
  this.oEditor = document.getElementById("editor");
  this.mediaList = document.getElementById("media-list");
  this.fullscreenViewer = document.getElementById("fullscreen-viewer");
  $(document).bind('keydown', function (event) {
    console.log(event.keyCode);

    if ($(writer.fullscreenViewer).is(":visible")) {
      switch (event.keyCode) {
        case 37:
          writer.previousMedia();
          break;

        case 39:
          writer.nextMedia();
          break;

        case 27:
          $(writer.fullscreenViewer).hide("slow");
          break;
      }
    }
  });
  ;
  this.toolbarManager = new ToolbarManager();
  var toolbarManager = this.toolbarManager;
  var toolbars = document.getElementsByClassName("toolbar");

  for (var i = 0; i < toolbars.length; i++) {
    this.toolbarManager.addToolbar(toolbars[i]);
  }

  ;
  var toolbarButtons = document.getElementsByClassName("toolbar-button");

  for (var i = 0; i < toolbarButtons.length; i++) {
    var toolbar = toolbarButtons[i];
    console.log("tool " + toolbar.getAttribute("for"));
    toolbar.addEventListener("click", function (event) {
      console.log("display " + event.target.getAttribute("for"));
      toolbarManager.toggleToolbar(document.getElementById(event.target.getAttribute("for")));
    });
  }

  ;
  this.searchInput = document.getElementById("search-input");

  this.searchInput.onfocus = function () {
    var el = document.getElementById('container-button');
    console.log('test');
    $(el).animate({
      scrollLeft: el.scrollLeft + 300
    }, 200);
  };

  this.searchInput.onkeyup = function (event) {
    if (event.key === 'Enter') {
      window.find(this.value);
    }
  };

  var ratingStars = document.getElementsByClassName("star");

  for (var i = 0; i < ratingStars.length; i++) {
    ratingStars[i].checked = false;

    ratingStars[i].onclick = function () {
      writer.saveRating(this.value);
      writer.updateRating(this.value);
    };
  }

  ;

  document.getElementById("button-add-keyword").onclick = function () {
    writer.toggleDrawer();
    writer.newKeywordDialog.showModal();
    return false;
  };

  document.getElementById("button-add-keyword-ok").onclick = function () {
    writer.addKeyword(document.getElementById('keyword-input').value);
    writer.newKeywordDialog.close();
  };

  var inToolbarButtons = document.getElementsByClassName("in-toolbar-button");

  for (var i = 0; i < inToolbarButtons.length; i++) {
    var button = inToolbarButtons[i];

    button.onclick = function (ev) {
      console.log("on click " + this.id);

      switch (this.id) {
        case "bold":
        case "italic":
        case "underline":
        case "justifyleft":
        case "justifycenter":
        case "justifyright":
          writer.formatDoc(this.id);
          break;

        case "text-color":
          writer.displayTextColorPicker();
          break;

        case "fill-color":
          writer.displayFillColorPicker();
          break;

        case "size-minus":
          writer.decreaseFontSize();
          break;

        case "size-plus":
          writer.increaseFontSize();
          break;

        case "statistics-button":
          writer.displayCountDialog();
          break;

        case "copy-button":
          writer.copy();
          break;

        case "paste-button":
          writer.paste();
          break;

        case "select-all-button":
          document.execCommand("selectAll");
          break;
      }
    };
  }

  this.keywordsList = document.getElementById("keywords");
  writer.keywordsList.innerHTML = "";
  document.getElementById('keyword-input').addEventListener("input", function () {
    console.log("input i");
    writer.keywordsList.innerHTML = "";
    if (this.value.length < 2) return;
    var i = 0;

    for (var word in writer.availableKeyword) {
      if (i > 2) break;
      if (writer.availableKeyword[word] == 0) continue;

      if (word.toLowerCase().indexOf(this.value.toLowerCase()) >= 0) {
        var o = document.createElement("tr");
        var td = document.createElement("td");
        td.classList.add("mdl-data-table__cell--non-numeric");
        td.innerHTML = word;
        o.style = "cursor: pointer;";
        o.appendChild(td);
        o.word = word;

        o.onclick = function () {
          document.getElementById('keyword-input').value = this.word;
          return false;
        };

        writer.keywordsList.appendChild(o);
        i++;
      }
    }

    try {
      new MaterialDataTable(writer.keywordsList);
    } catch (e) {}
  });
  this.addMediaMenu = document.getElementById("add-media-menu");

  document.getElementById("exit").onclick = function () {
    writer.toggleDrawer();
    writer.askToExit();
  };

  document.getElementById("add-file-button").onclick = function () {
    writer.addMedia();
  };

  document.getElementById("add-recording-button").onclick = function () {
    writer.recorder.new();
    writer.recorderDialog.showModal();
  };

  document.getElementById("export-button").onclick = function () {
    writer.toggleDrawer();
    writer.warnNotYetImplemented();
    return false;
  };

  writer.nameTimout = undefined;
  document.getElementById("name-input").addEventListener("input", function () {
    if (writer.nameTimout != undefined) clearTimeout(writer.nameTimout);
    writer.nameTimout = setTimeout(function () {
      writer.seriesTaskExecutor.addTask(writer.saveNoteTask); // first, save.

      writer.seriesTaskExecutor.addTask(new RenameNoteTask(writer));
    }, 1000);
  }); // $("#editor").webkitimageresize().webkittableresize().webkittdresize();
};

Writer.prototype.toggleDrawer = function () {
  if (document.getElementsByClassName("is-small-screen").length > 0) document.getElementsByClassName("mdl-layout__drawer-button")[0].click();
};

Writer.prototype.askToExit = function () {
  console.log("exec? " + this.seriesTaskExecutor.isExecuting);
  if (this.seriesTaskExecutor.isExecuting) return false;else {
    compatibility.exit();
  }
  return false;
};

Writer.prototype.copy = function () {
  document.execCommand('copy');
  this.copied = this.getSelectionHtml();
};

Writer.prototype.paste = function () {
  if (!document.execCommand('paste')) {
    if ((typeof app === "undefined" ? "undefined" : _typeof(app)) === "object") app.paste(); //for android app
    else document.execCommand('insertHTML', false, this.copied);
  }
};

Writer.prototype.getSelectionHtml = function () {
  var html = "";

  if (typeof window.getSelection != "undefined") {
    var sel = window.getSelection();

    if (sel.rangeCount) {
      var container = document.createElement("div");

      for (var i = 0, len = sel.rangeCount; i < len; ++i) {
        container.appendChild(sel.getRangeAt(i).cloneContents());
      }

      html = container.innerHTML;
    }
  } else if (typeof document.selection != "undefined") {
    if (document.selection.type == "Text") {
      html = document.selection.createRange().htmlText;
    }
  }

  return html;
};

Writer.prototype.displayCountDialog = function () {
  var nouveauDiv;

  if (window.getSelection().toString().length == 0) {
    nouveauDiv = this.oDoc;
  } else {
    nouveauDiv = document.createElement("div");
    nouveauDiv.innerHTML = window.getSelection();
  }

  console.log(" is defined ? " + nouveauDiv);
  var writer = this;
  Countable.once(nouveauDiv, function (counter) {
    writer.statsDialog.querySelector('.words_count').innerHTML = counter.words;
    writer.statsDialog.querySelector('.characters_count').innerHTML = counter.characters;
    writer.statsDialog.querySelector('.sentences_count').innerHTML = counter.sentences;
    writer.statsDialog.showModal();
  });
};

Writer.prototype.increaseFontSize = function () {
  this.surroundSelection(document.createElement('big'));
};

Writer.prototype.decreaseFontSize = function () {
  this.surroundSelection(document.createElement('small'));
};

Writer.prototype.surroundSelection = function (element) {
  if (window.getSelection) {
    var sel = window.getSelection();

    if (sel.rangeCount) {
      var range = sel.getRangeAt(0).cloneRange();
      range.surroundContents(element);
      sel.removeAllRanges();
      sel.addRange(range);
    }
  }
}; //var KeywordsDBManager = require(rootpath + "keywords/keywords_db_manager").KeywordsDBManager;
//var keywordsDBManager = new KeywordsDBManager()


Writer.prototype.addKeyword = function (word) {
  var writer = this;

  if (this.note.metadata.keywords.indexOf(word) < 0 && word.length > 0) {
    this.note.metadata.keywords.push(word);
    keywordsDBManager.addToDB(word, this.note.path, function () {
      writer.refreshKeywords();
    });
    this.seriesTaskExecutor.addTask(this.saveNoteTask);
  }
};

Writer.prototype.removeKeyword = function (word) {
  var writer = this;

  if (this.note.metadata.keywords.indexOf(word) >= 0) {
    this.note.metadata.keywords.splice(this.note.metadata.keywords.indexOf(word), 1);
    keywordsDBManager.removeFromDB(word, this.note.path, function () {
      writer.refreshKeywords();
    });
    this.seriesTaskExecutor.addTask(this.saveNoteTask);
  }
};

Writer.prototype.reset = function () {
  if (this.saveInterval !== undefined) clearInterval(this.saveInterval);
  this.oEditor.innerHTML = '<div id="text" contenteditable="true" style="height:100%;">\
    <!-- be aware that THIS will be modified in java -->\
    <!-- soft won\'t save note if contains donotsave345oL -->\
</div>\
<div id="floating">\
\
</div>';
  var dias = document.getElementsByClassName("mdl-dialog");

  for (var i = 0; i < dias.length; i++) {
    if (dias[i].open) dias[i].close();
  }

  var snackbarContainer = document.querySelector('#snackbar');

  if (snackbarContainer != undefined && !(_typeof(snackbarContainer.MaterialSnackbar) == undefined)) {
    snackbarContainer.queuedNotifications_ = [];
    snackbarContainer.MaterialSnackbar.cleanup_();
  }

  this.setDoNotEdit(false);
};

Writer.prototype.setColor = function (color) {
  document.execCommand('styleWithCSS', false, true);
  document.execCommand('foreColor', false, color);
};

Writer.prototype.fillColor = function (color) {
  document.execCommand('styleWithCSS', false, true);
  document.execCommand('backColor', false, color);
};

Writer.prototype.saveRating = function (rating) {
  this.note.metadata.rating = rating;
  console.log("new rating " + this.note.metadata.rating);
  writer.hasTextChanged = true;
};

Writer.prototype.updateRating = function (rating) {
  var ratingStars = document.querySelectorAll("label.star");

  for (var i = 0; i < ratingStars.length; i++) {
    if (5 - i <= this.note.metadata.rating) {
      ratingStars[i].classList.add("checked");
    } else ratingStars[i].classList.remove("checked");
  }

  ;
};

Writer.prototype.getCaretPosition = function () {
  var x = 0;
  var y = 0;
  var sel = window.getSelection();

  if (sel.rangeCount) {
    var range = sel.getRangeAt(0).cloneRange();

    if (range.getClientRects()) {
      range.collapse(true);
      var rect = range.getClientRects()[0];

      if (rect) {
        y = rect.top;
        x = rect.left;
      }
    }
  }

  return {
    x: x,
    y: y
  };
};

var ToolbarManager = function ToolbarManager() {
  this.toolbars = [];
};

ToolbarManager.prototype.addToolbar = function (elem) {
  this.toolbars.push(elem);
  $(elem).hide();
};

ToolbarManager.prototype.toggleToolbar = function (elem) {
  for (var i = 0; i < this.toolbars.length; i++) {
    var toolbar = this.toolbars[i];
    if (toolbar != elem) $(toolbar).slideUp("fast", resetScreenHeight);
  }

  if ($(elem).is(":visible")) $(elem).slideUp("fast", resetScreenHeight);else $(elem).slideDown("fast", resetScreenHeight);
  resetScreenHeight();
};

var RenameNoteTask = function RenameNoteTask(writer) {
  this.writer = writer;
};

RenameNoteTask.prototype.run = function (callback) {
  console.log("RenameNoteTask.run");
  $("#loading").fadeIn();
  this.writer.setDoNotEdit(true);
  var task = this;
  var path = FileUtils.getParentFolderFromPath(this.writer.note.path);
  var hasOrigin = false;
  var nameInput = document.getElementById("name-input");
  var _iteratorNormalCompletion = true;
  var _didIteratorError = false;
  var _iteratorError = undefined;

  try {
    for (var _iterator = nameInput.value.split("/")[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
      var part = _step.value;

      if (part == ".." && !hasOrigin) {
        path = FileUtils.getParentFolderFromPath(path);
      } else {
        hasOrigin = true;
        path += "/" + part;
      }
    }
  } catch (err) {
    _didIteratorError = true;
    _iteratorError = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion && _iterator.return != null) {
        _iterator.return();
      }
    } finally {
      if (_didIteratorError) {
        throw _iteratorError;
      }
    }
  }

  path += ".sqd";
  if (path.startsWith("./")) path = path.substr(2);
  RequestBuilder.sRequestBuilder.post("/notes/move", {
    from: this.writer.note.path,
    to: path
  }, function (error, data) {
    if (!error) {
      task.writer.note.path = path;
      task.writer.setDoNotEdit(false);
      $("#loading").fadeOut();
      var data = {
        message: 'Note correctly renamed',
        timeout: 2000
      };
      task.writer.displaySnack(data);
      callback();
    } else {
      task.writer.setDoNotEdit(false);
      $("#loading").fadeOut();
      nameInput.value = FileUtils.stripExtensionFromName(FileUtils.getFilename(task.writer.note.path));
      var data = {
        message: 'Note couldn\'t be renamed',
        timeout: 2000
      };
      task.writer.displaySnack(data);
      callback();
    }
  });
};

var SeriesTaskExecutor = function SeriesTaskExecutor() {
  this.task = [];
  this.isExecuting = false;
};

SeriesTaskExecutor.prototype.addTask = function (task) {
  this.task.push(task);
  console.log("push " + this.isExecuting);

  if (!this.isExecuting) {
    this.execNext();
  }
};

SeriesTaskExecutor.prototype.execNext = function () {
  this.isExecuting = true;
  console.log("exec next ");
  if (this.task == undefined) this.task = [];

  if (this.task.length == 0) {
    this.isExecuting = false;
    return;
  }

  var executor = this;
  this.task.shift().run(function () {
    executor.execNext();
  });
  console.log("this.task length " + this.task.length);
};

var SaveNoteTask = function SaveNoteTask(writer) {
  this.writer = writer;
};

SaveNoteTask.prototype.trySave = function (onEnd, trial) {
  var task = this;
  if (this.writer.note.metadata.creation_date === "") this.writer.note.metadata.creation_date = Date.now();
  this.writer.note.metadata.last_modification_date = Date.now();
  RequestBuilder.sRequestBuilder.post("/note/saveText", {
    id: this.writer.saveID,
    path: this.writer.note.path,
    html: this.writer.oEditor.innerHTML,
    metadata: JSON.stringify(this.writer.note.metadata)
  }, function (error, data) {
    if (error) {
      if (trial < 3) {
        setTimeout(function () {
          task.trySave(onEnd, trial + 1);
        }, 1000);
      } else {
        writer.displaySnack({
          message: 'An error occured, please save your text and check it is not open in another tab',
          timeout: 60000 * 300
        });
        writer.setDoNotEdit(true);
        onEnd();
      }
    } else onEnd();
  });
};

SaveNoteTask.prototype.saveTxt = function (onEnd) {
  this.trySave(onEnd, 0);
};

SaveNoteTask.prototype.run = SaveNoteTask.prototype.saveTxt;
var lastscroll = 0;

function resetScreenHeight() {
  console.log("resetScreenHeight");
  var screen = $(window).innerHeight(),
      header = $("#header-carnet").height() + $("#toolbars").height(),
      content = screen - header;
  var style = window.getComputedStyle(document.getElementById("header-carnet"));
  if (style.getPropertyValue('display') == "none") content = screen;
  $("#center").height(content);
  $("#editor").height(content - 45);
  $("#editor").scrollTop(lastscroll);

  if (writer != undefined) {
    var diff = content - 45 - writer.getCaretPosition().y + header;
    console.log(diff);
    if (diff < 0) $("#editor").scrollTop(lastscroll - diff);
  }

  console.log(content - 45);
}

function loadPath(path) {
  if (writer == undefined) return;
  writer.reset();
  var note = new Note("", "", path, undefined);
  writer.setNote(note);
  console.log("extract");
  writer.extractNote();
}

if (loaded == undefined) var loaded = false; //don't know why, loaded twice on android

var writer = undefined;
$(document).ready(function () {
  rootpath = document.getElementById("root-url").innerHTML;
  api_url = document.getElementById("api-url").innerHTML;
  new RequestBuilder(api_url);

  if (writer == undefined) {
    writer = new Writer(document);
    writer.init();
  }

  initDragAreas();

  if (!loaded) {
    var getParameterByName = function getParameterByName(name, url) {
      if (!url) {
        url = window.location.href;
      }

      name = name.replace(/[\[\]]/g, "\\$&");
      var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
          results = regex.exec(url);
      if (!results) return null;
      if (!results[2]) return '';
      return decodeURIComponent(results[2].replace(/\+/g, " ").replace(/%2F/g, "/"));
    };

    $(window).on('resize', resetScreenHeight);
    var path = getParameterByName("path");
    var tmp = getParameterByName("tmppath");
    if (tmp != null) tmppath = tmp;

    if (path != undefined) {
      console.log("path " + getParameterByName("path"));
      loadPath(path);
    }

    loaded = true;
  }

  window.oldOncontextmenu = window.oncontextmenu;

  window.oncontextmenu = function (event) {
    event.preventDefault();
    event.stopPropagation();
    return false;
  };
});