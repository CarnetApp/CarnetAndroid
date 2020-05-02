"use strict";

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _get(target, property, receiver) { if (typeof Reflect !== "undefined" && Reflect.get) { _get = Reflect.get; } else { _get = function _get(target, property, receiver) { var base = _superPropBase(target, property); if (!base) return; var desc = Object.getOwnPropertyDescriptor(base, property); if (desc.get) { return desc.get.call(receiver); } return desc.value; }; } return _get(target, property, receiver || target); }

function _superPropBase(object, property) { while (!Object.prototype.hasOwnProperty.call(object, property)) { object = _getPrototypeOf(object); if (object === null) break; } return object; }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

var initPath = "recentdb://";
var currentPath;
var currentTask = undefined;
var noteCardViewGrid = undefined;
var notePath = [];
var oldFiles = undefined;
var wasNewNote = false;
var dontOpen = false;
var currentNotePath = undefined;
var root_url = document.getElementById("root-url") != undefined ? document.getElementById("root-url").innerHTML : "";
var api_url = Utils.getParameterByName("api_url");
if (api_url == undefined) api_url = document.getElementById("api-url").innerHTML !== "!API_URL" ? document.getElementById("api-url").innerHTML : "./";
new RequestBuilder(api_url);
var store = new Store();
var noteCacheStr = String(store.get("note_cache"));
if (noteCacheStr == "undefined") noteCacheStr = "{}";
var cachedMetadata = JSON.parse(noteCacheStr);
var recentCacheStr = String(store.get("recent_cache"));
var cachedRecentDB = undefined;
if (recentCacheStr != "undefined") cachedRecentDB = JSON.parse(recentCacheStr);

var TextGetterTask = function TextGetterTask(list) {
  this.list = list;
  this.current = 0;
  this["continue"] = true;
  this.stopAt = 50;
};

TextGetterTask.prototype.startList = function () {
  this.getNext();
};

TextGetterTask.prototype.getNext = function () {
  console.log(this.current);

  if (this.current >= this.stopAt || this.current >= this.list.length) {
    console.log("save cache ");
    store.set("note_cache", JSON.stringify(cachedMetadata));
    return;
  }

  var paths = "";
  var start = this.current;

  for (var i = start; i < this.stopAt && i < this.list.length && i - start < 20; i++) {
    //do it 20 by 20
    this.current = i + 1;
    if (!(this.list[i] instanceof Note) || !this.list[i].needsRefresh) continue;
    paths += this.list[i].path + ",";
    if (cachedMetadata[this.list[i].path] == undefined) cachedMetadata[this.list[i].path] = this.list[i];
  }

  var myTask = this;

  if (paths.length > 0) {
    RequestBuilder.sRequestBuilder.get("/metadata?paths=" + encodeURIComponent(paths), function (error, data) {
      for (var meta in data) {
        var note = new Note(Utils.cleanNoteName(getFilenameFromPath(meta)), data[meta].shorttext, meta, data[meta].metadata, data[meta].previews, false, data[meta].media);
        cachedMetadata[meta] = note;
        notes[notePath.indexOf(meta)] = note;
        noteCardViewGrid.updateNote(note);
        noteCardViewGrid.msnry.layout();
      }

      myTask.getNext();
    });
  } else myTask.getNext();
};

String.prototype.replaceAll = function (search, replacement) {
  var target = this;
  return target.replace(new RegExp(search, 'g'), replacement);
};

function openNote(notePath, action) {
  isLoadCanceled = false;
  currentNotePath = notePath;
  RequestBuilder.sRequestBuilder.get("/note/open/prepare", function (error, data) {
    console.log("opening " + data);
    if (error) return;

    if (writerFrame.src == "") {
      if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1 && navigator.userAgent.toLowerCase().indexOf("android") > -1) {
        //open in new tab for firefox android
        window.open("writer?path=" + encodeURIComponent(notePath) + (action != undefined ? "&action=" + action : ""), "_blank");
      } else {
        $("#editor-container").show();
        $(loadingView).fadeIn(function () {
          writerFrame.src = data + "?path=" + encodeURIComponent(notePath) + (action != undefined ? "&action=" + action : "");
          writerFrame.style.display = "inline-flex";
        });
      }
      /*setTimeout(function () {
          writerFrame.openDevTools()
      }, 1000)*/

    } else {
      console.log("reuse old iframe");
      $("#editor-container").show();
      $(loadingView).fadeIn(function () {
        if (compatibility.isElectron) {
          writerFrame.send('loadnote', notePath);
          writerFrame.send('action', action);
        } else writerFrame.contentWindow.loadPath(notePath, action);

        writerFrame.style.display = "inline-flex";
      });
    }
  });
}

var displaySnack = function displaySnack(data) {
  var snackbarContainer = document.querySelector('#snackbar');
  if (!(_typeof(snackbarContainer.MaterialSnackbar) == undefined)) snackbarContainer.MaterialSnackbar.showSnackbar(data);
};

function onDragEnd(gg) {
  console.log("ondragend");
  dontOpen = true;
}

function refreshKeywords() {
  var keywordsDBManager = new KeywordsDBManager();
  keywordsDBManager.getFlatenDB(function (error, data) {
    var keywordsContainer = document.getElementById("keywords");
    keywordsContainer.innerHTML = "";
    var dataArray = [];

    for (var key in data) {
      if (data[key].length == 0) continue;
      dataArray.push(key);
    }

    dataArray.sort(Utils.caseInsensitiveSrt);

    var _loop = function _loop() {
      var key = _dataArray[_i];
      keywordElem = document.createElement("a");
      keywordElem.classList.add("mdl-navigation__link");
      keywordElem.innerHTML = key;
      keywordElem.setAttribute("href", "");

      keywordElem.onclick = function () {
        toggleDrawer();
        list("keyword://" + key, false);
        return false;
      };

      keywordsContainer.appendChild(keywordElem);
    };

    for (var _i = 0, _dataArray = dataArray; _i < _dataArray.length; _i++) {
      var keywordElem;

      _loop();
    }
  });
}

function resetGrid(discret) {
  var grid = document.getElementById("page-content");
  var scroll = 0;
  if (discret) scroll = document.getElementById("grid-container").scrollTop;
  grid.innerHTML = "";
  noteCardViewGrid = new NoteCardViewGrid(grid, UISettingsHelper.getInstance().get("in_line"), discret, onDragEnd);
  this.noteCardViewGrid = noteCardViewGrid;
  noteCardViewGrid.onFolderClick(function (folder) {
    list(folder.path);
  });

  noteCardViewGrid.onTodoListChange = function (note) {
    RequestBuilder.sRequestBuilder.post("/notes/metadata", {
      path: note.path,
      metadata: JSON.stringify(note.metadata)
    }, function (error) {});
  };

  noteCardViewGrid.onNoteClick(function (note) {
    if (!dontOpen) {
      if (note.path != "untitleddonotedit.sqd") openNote(note.path);else displaySnack({
        message: $.i18n("fake_notes_warning"),
        timeout: 2000
      });
    }

    dontOpen = false;
  });
  noteCardViewGrid.onMenuClick(function (note) {
    mNoteContextualDialog.show(note);
  });
  return scroll;
}

var ContextualDialog =
/*#__PURE__*/
function () {
  function ContextualDialog() {
    _classCallCheck(this, ContextualDialog);

    this.showDelete = true;
    this.showArchive = true;
    this.showPin = true;
    this.dialog = document.querySelector('#contextual-dialog');
    this.nameInput = this.dialog.querySelector('#name-input');
    this.deleteButton = this.dialog.querySelector('.delete-button');
    this.archiveButton = this.dialog.querySelector('#archive-button');
    this.pinButton = this.dialog.querySelector('#pin-button');
    this.cancel = this.dialog.querySelector('.cancel');
    this.ok = this.dialog.querySelector('.ok');
    var context = this;

    this.cancel.onclick = function () {
      context.dialog.close();
    };
  }

  _createClass(ContextualDialog, [{
    key: "show",
    value: function show() {
      this.showDelete ? $(this.deleteButton).show() : $(this.deleteButton).hide();
      this.showArchive ? $(this.archiveButton).show() : $(this.archiveButton).hide();
      this.showPin ? $(this.pinButton).show() : $(this.pinButton).hide();
      this.dialog.showModal();
      this.nameInput.focus();
    }
  }]);

  return ContextualDialog;
}();

var NewFolderDialog =
/*#__PURE__*/
function (_ContextualDialog) {
  _inherits(NewFolderDialog, _ContextualDialog);

  function NewFolderDialog() {
    var _this;

    _classCallCheck(this, NewFolderDialog);

    _this = _possibleConstructorReturn(this, _getPrototypeOf(NewFolderDialog).call(this));
    _this.showDelete = false;
    _this.showArchive = false;
    _this.showPin = false;
    return _this;
  }

  _createClass(NewFolderDialog, [{
    key: "show",
    value: function show() {
      var context = this;

      this.ok.onclick = function () {
        RequestBuilder.sRequestBuilder.post("/browser/newfolder", {
          path: currentPath + "/" + context.nameInput.value
        }, function (error) {
          if (error) {}

          list(currentPath, true);
          context.dialog.close();
        });
      };

      _get(_getPrototypeOf(NewFolderDialog.prototype), "show", this).call(this);
    }
  }]);

  return NewFolderDialog;
}(ContextualDialog);

var NoteContextualDialog =
/*#__PURE__*/
function (_ContextualDialog2) {
  _inherits(NoteContextualDialog, _ContextualDialog2);

  function NoteContextualDialog() {
    _classCallCheck(this, NoteContextualDialog);

    return _possibleConstructorReturn(this, _getPrototypeOf(NoteContextualDialog).call(this));
  }

  _createClass(NoteContextualDialog, [{
    key: "show",
    value: function show(note) {
      var context = this;
      this.nameInput.value = note.title;

      this.deleteButton.onclick = function () {
        var db = RecentDBManager.getInstance();
        var keywordDB = new KeywordsDBManager();
        context.dialog.close();
        db.removeFromDB(note.path, function (error, data) {
          console.log("deleted from db " + error);
          if (!error) keywordDB.removeFromDB(undefined, note.path, function (error, data) {
            console.log("deleted from db " + error);
            if (!error) RequestBuilder.sRequestBuilder["delete"]("/notes?path=" + encodeURIComponent(note.path), function () {
              list(currentPath, true);
            });
          });
        });
      };

      if (RecentDBManager.getInstance().lastDb.indexOf(note.path) < 0) {
        this.archiveButton.innerHTML = $.i18n("unarchive");
      } else this.archiveButton.innerHTML = $.i18n("archive");

      this.archiveButton.onclick = function () {
        var db = RecentDBManager.getInstance();

        if (RecentDBManager.getInstance().lastDb.indexOf(note.path) < 0) {
          db.addToDB(note.path, function () {
            context.dialog.close();
            list(currentPath, true);
          });
        } else {
          db.removeFromDB(note.path, function () {
            context.dialog.close();
            list(currentPath, true);
          });
        }
      };

      if (note.isPinned == true) {
        this.pinButton.innerHTML = "Unpin";
      } else this.pinButton.innerHTML = "Pin";

      this.pinButton.onclick = function () {
        var db = RecentDBManager.getInstance();
        if (note.isPinned == true) db.unpin(note.path, function () {
          context.dialog.close();
          list(currentPath, true);
        });else db.pin(note.path, function () {
          context.dialog.close();
          list(currentPath, true);
        });
      };

      this.ok.onclick = function () {
        var path = FileUtils.getParentFolderFromPath(note.path);
        var hasOrigin = false;
        var _iteratorNormalCompletion = true;
        var _didIteratorError = false;
        var _iteratorError = undefined;

        try {
          for (var _iterator = context.nameInput.value.split("/")[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
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
            if (!_iteratorNormalCompletion && _iterator["return"] != null) {
              _iterator["return"]();
            }
          } finally {
            if (_didIteratorError) {
              throw _iteratorError;
            }
          }
        }

        RequestBuilder.sRequestBuilder.post("/notes/move", {
          from: note.path,
          to: path + ".sqd"
        }, function () {
          list(currentPath, true);
        });
        context.dialog.close();
      };

      _get(_getPrototypeOf(NoteContextualDialog.prototype), "show", this).call(this);
    }
  }]);

  return NoteContextualDialog;
}(ContextualDialog);

var mNoteContextualDialog = new NoteContextualDialog();
var mNewFolderDialog = new NewFolderDialog();
var refreshTimeout = undefined;
var lastListingRequestId = undefined;

function sortBy(sortBy, reversed, discret) {
  notePath = [];
  var sorter = Utils.sortByDefault;

  switch (sortBy) {
    case "creation":
      sorter = Utils.sortByCreationDate;
      break;

    case "modification":
      sorter = Utils.sortByModificationDate;
      break;

    case "custom":
      sorter = Utils.sortByCustomDate;
      break;
  }

  resetGrid(discret);
  notes.sort(reversed ? function (a, b) {
    return -sorter(a, b);
  } : sorter);
  var _iteratorNormalCompletion2 = true;
  var _didIteratorError2 = false;
  var _iteratorError2 = undefined;

  try {
    for (var _iterator2 = notes[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
      var item = _step2.value;
      notePath.push(item.path);
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

  noteCardViewGrid.setNotesAndFolders(notes);
  currentTask = new TextGetterTask(notes);
  console.log("stopping and starting task");
  currentTask.startList();
}

function onListEnd(pathToList, files, metadatas, discret, force) {
  lastListingRequestId = undefined;

  if (!_.isEqual(files, oldFiles) || force) {
    var scroll = resetGrid(discret);
    oldFiles = files;
    var noteCardViewGrid = this.noteCardViewGrid;
    notes = [];
    notePath = [];
    if (currentTask != undefined) currentTask["continue"] = false;
    var i = 0;
    var _iteratorNormalCompletion3 = true;
    var _didIteratorError3 = false;
    var _iteratorError3 = undefined;

    try {
      for (var _iterator3 = files[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
        var file = _step3.value;
        var filename = getFilenameFromPath(file.path);

        if (filename.endsWith(".sqd")) {
          var metadata = metadatas != undefined ? metadatas[file.path] : undefined;
          var needsRefresh = metadata == undefined;

          if (metadata == undefined) {
            metadata = cachedMetadata[file.path];
          }

          var noteTestTxt = new Note(Utils.cleanNoteName(filename), metadata != undefined ? metadata.shorttext : "", file.path, metadata != undefined ? metadata.metadata : undefined, metadata != undefined ? metadata.previews : undefined, needsRefresh, metadata != undefined ? metadata.media : undefined);
          noteTestTxt.isPinned = file.isPinned;
          noteTestTxt.originalIndex = i;
          notes.push(noteTestTxt);
          if (metadata != undefined) cachedMetadata[file.path] = metadata;
        } else if (!file.isFile) {
          file.originalIndex = i;
          notes.push(file);
        }

        i++;
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

    if (files.length == 0 && pathToList === "recentdb://") {
      $("#emty-view").fadeOut("fast");
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_1"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: [],
        rating: 5,
        color: "none"
      }, undefined);
      notes.push(noteTestTxt);
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_5"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: [],
        rating: -1,
        color: "red"
      }, undefined);
      noteTestTxt.previews = [];
      noteTestTxt.previews.push(root_url + "img/bike.png");
      notes.push(noteTestTxt);
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_2"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: ["keyword"],
        rating: -1,
        color: "orange"
      }, undefined);
      notes.push(noteTestTxt);
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_3"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: [],
        rating: 3,
        color: "none"
      }, undefined);
      notes.push(noteTestTxt);
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_4"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: [],
        rating: -1,
        color: "green"
      }, undefined);
      notes.push(noteTestTxt);
      var noteTestTxt = new Note("untitleddonotedit.sqd", $.i18n("fake_note_6"), "untitleddonotedit.sqd", {
        creation_date: new Date().getTime(),
        last_modification_date: new Date().getTime(),
        keywords: [],
        rating: -1,
        urls: {
          "https://carnet.live": {}
        },
        todolists: [{
          todo: [$.i18n("fake_note_todo_item_1"), $.i18n("fake_note_todo_item_2")]
        }],
        color: "none"
      }, undefined);
      notes.push(noteTestTxt);
    }

    sortBy(UISettingsHelper.getInstance().get('sort_by'), UISettingsHelper.getInstance().get('reversed'), discret);

    if (discret) {
      document.getElementById("grid-container").scrollTop = scroll;
      console.log("scroll : " + scroll);
    }
  }
}

var notes = [];

function list(pathToList, discret) {
  if (refreshTimeout !== undefined) clearTimeout(refreshTimeout);

  if (lastListingRequestId != undefined) {
    RequestBuilder.sRequestBuilder.cancelRequest(lastListingRequestId);
  }

  if (pathToList == undefined) pathToList = currentPath;
  console.log("listing path " + pathToList);
  var hasPathChanged = currentPath !== pathToList;
  currentPath = pathToList;

  if (pathToList == "/" || pathToList == "recentdb://" || pathToList.startsWith("keyword://")) {
    if (pathToList != "/") {
      $("#add-directory-button").hide();
    } else $("#add-directory-button").show();

    $("#back_arrow").hide();
  } else {
    $("#back_arrow").show();
    $("#add-directory-button").show();
  }

  if (!discret) {
    document.getElementById("note-loading-view").style.display = "inline";
    document.getElementById("page-content").style.display = "none";
  }

  var fb = new FileBrowser(pathToList);
  lastListingRequestId = fb.list(function (error, files, endOfSearch, metadatas) {
    if (error || endOfSearch || files.length > 0) {
      document.getElementById("page-content").style.display = "block";
      document.getElementById("note-loading-view").style.display = "none";
    }

    if (error) {
      document.getElementById("page-content").style.display = "none";
      $("#emty-view").fadeIn("fast");
      document.getElementById("emty-view").innerHTML = $.i18n("something_went_wrong_please_reload");
      return;
    }

    if (files != null && pathToList === "recentdb://" && files.length > 0) {
      //save to cache
      store.set("recent_cache", JSON.stringify(files));
    }

    if (files.length > 0) {
      $("#emty-view").fadeOut("fast");
    } else if (endOfSearch) $("#emty-view").fadeIn("fast");

    onListEnd(pathToList, files, metadatas, discret);

    if (!endOfSearch) {
      refreshTimeout = setTimeout(function () {
        list(pathToList, files.length > 0);
      }, 1000);
    } else {
      refreshTimeout = setTimeout(function () {
        list(pathToList, true);
      }, 60000);
    }
  });
}

refreshKeywords();

function minimize() {
  remote.BrowserWindow.getFocusedWindow().minimize();
}

function maximize() {
  if (remote.BrowserWindow.getFocusedWindow().isMaximized()) remote.BrowserWindow.getFocusedWindow().unmaximize();else remote.BrowserWindow.getFocusedWindow().maximize();
}

function closeW() {
  remote.app.exit(0);
  console.log("cloose");
}
/*main.setMergeListener(function () {
    list(initPath, true)
})*/


document.getElementById("add-note-button").onclick = function () {
  createAndOpenNote();
};

document.getElementById("add-record-button").onclick = function () {
  createAndOpenNote("record-audio");
};

function createAndOpenNote(action) {
  var path = currentPath;
  if (path == "recentdb://" || path.startsWith("keyword://")) path = "";
  RequestBuilder.sRequestBuilder.get("/note/create?path=" + encodeURIComponent(path), function (error, data) {
    if (error) return;
    console.log("found " + data);
    wasNewNote = true;
    var db = RecentDBManager.getInstance();
    db.addToDB(data, function () {
      openNote(data, action);
    });
  });
}

document.getElementById("add-directory-button").onclick = function () {
  mNewFolderDialog.show();
};

document.getElementById("back_arrow").addEventListener("click", function () {
  list(FileUtils.getParentFolderFromPath(currentPath));
});

function getNotePath() {
  return main.getNotePath();
}

function loadNextNotes() {
  browser.noteCardViewGrid.addNext(15);
  currentTask.stopAt += 15;
  currentTask.getNext();
}

var browser = void 0;

document.getElementById("grid-container").onscroll = function () {
  if (this.offsetHeight + this.scrollTop >= this.scrollHeight - 80) {
    loadNextNotes();
  }
};

var hasLoadedOnce = false;
var loadingView = document.getElementById("loading-view"); //var browserElem = document.getElementById("browser")

console.log("pet");
var dias = document.getElementsByClassName("mdl-dialog");

for (var i = 0; i < dias.length; i++) {
  dialogPolyfill.registerDialog(dias[i]);
} //nav buttons


document.getElementById("browser-button").onclick = function () {
  toggleDrawer();
  list("/");
  return false;
};

document.getElementById("recent-button").onclick = function () {
  toggleDrawer();
  list("recentdb://");
  return false;
};

function toggleDrawer() {
  if (document.getElementsByClassName("is-small-screen").length > 0) document.getElementsByClassName("mdl-layout__drawer-button")[0].click();
}

function isFullScreen() {
  return document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement || document.msFullscreenElement;
}

RequestBuilder.sRequestBuilder.get("/recentdb/merge", function (error, data) {
  if (data == true && currentPath == "recentdb://") list("recentdb://", true);
});
RequestBuilder.sRequestBuilder.get("/keywordsdb/merge", function (error, data) {
  if (data == true) refreshKeywords();
});
var isWeb = true;
var right = document.getElementById("right-bar"); //writer frame

var isElectron = typeof require === "function";
var writerFrame = undefined;
var events = [];

if (isElectron) {
  writerFrame = document.getElementById("writer-webview");
  writerFrame.addEventListener('ipc-message', function (event) {
    if (events[event.channel] !== undefined) {
      var _iteratorNormalCompletion4 = true;
      var _didIteratorError4 = false;
      var _iteratorError4 = undefined;

      try {
        for (var _iterator4 = events[event.channel][Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
          var callback = _step4.value;
          callback();
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
    }
  });
} else {
  writerFrame = document.getElementById("writer-iframe"); //iframe events

  var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
  var eventer = window[eventMethod];
  var messageEvent = eventMethod === "attachEvent" ? "onmessage" : "message";
  eventer(messageEvent, function (e) {
    if (events[e.data] !== undefined) {
      var _iteratorNormalCompletion5 = true;
      var _didIteratorError5 = false;
      var _iteratorError5 = undefined;

      try {
        for (var _iterator5 = events[e.data][Symbol.iterator](), _step5; !(_iteratorNormalCompletion5 = (_step5 = _iterator5.next()).done); _iteratorNormalCompletion5 = true) {
          var callback = _step5.value;
          callback();
        }
      } catch (err) {
        _didIteratorError5 = true;
        _iteratorError5 = err;
      } finally {
        try {
          if (!_iteratorNormalCompletion5 && _iterator5["return"] != null) {
            _iterator5["return"]();
          }
        } finally {
          if (_didIteratorError5) {
            throw _iteratorError5;
          }
        }
      }
    }
  });
}

function registerWriterEvent(event, callback) {
  if (events[event] == null) {
    events[event] = [];
  }

  events[event].push(callback);
}

registerWriterEvent("exit", function () {
  $(writerFrame).fadeOut();
  $("#editor-container").hide();
  $("#drag-bar").show();
  setDraggable(true);

  if (!wasNewNote) {
    if (currentTask != undefined) {
      var index = notePath.indexOf(currentNotePath);
      currentTask.current = index;
      currentTask.stopAt = index + 1;
      currentTask.list[index].needsRefresh = true;
      currentTask.startList();
    }
  }

  list(currentPath, true);
  wasNewNote = false;
});
var isLoadCanceled = false;
registerWriterEvent("loaded", function () {
  if (!isLoadCanceled) {
    $(loadingView).fadeOut();
    $("#drag-bar").hide();
    setDraggable(false);
  }
});
registerWriterEvent("error", function () {
  hideEditor();
});

function setDraggable(draggable) {
  if (draggable) $(document.getElementsByClassName("mdl-layout__header")[0]).css("-webkit-app-region", "drag");else $(document.getElementsByClassName("mdl-layout__header")[0]).css("-webkit-app-region", "unset");
}

function hideEditor() {
  isLoadCanceled = true;
  $(loadingView).fadeOut();
  $(writerFrame).fadeOut();
  $("#editor-container").hide();
  $("#drag-bar").show();
  setDraggable(true);
} // line / grid switch


function setInLineButton(isInLine) {
  document.getElementById("line-grid-switch-button").getElementsByClassName("material-icons")[0].innerHTML = !isInLine ? "view_headline" : "view_module";
}

function setInLine(isInLine) {
  UISettingsHelper.getInstance().set("in_line", isInLine);
  UISettingsHelper.getInstance().postSettings();
  setInLineButton(isInLine);
  onListEnd(currentPath, oldFiles, cachedMetadata, true, true);
}

function toggleInLine() {
  setInLine(!UISettingsHelper.getInstance().get("in_line"));
}

document.getElementById("line-grid-switch-button").onclick = function () {
  toggleInLine();
};

document.getElementById("cancel-load-button").onclick = function () {
  hideEditor();
  return false;
};

document.getElementById("editor-container").onclick = function () {
  hideEditor();
  return false;
};

setTimeout(function () {
  RequestBuilder.sRequestBuilder.get("/settings/isfirstrun", function (error, data) {
    if (!error && data == true) {
      var elem = document.getElementById("firstrun-container");
      $(elem).show();
      $("#firstrun").slideToggle();
      new Slides(elem, function () {
        $("#firstrun").slideToggle(function () {
          $(elem).hide();
          compatibility.onFirstrunEnds();
        });
      });
    } else {
      RequestBuilder.sRequestBuilder.get("/settings/changelog", function (error, data) {
        if (data.shouldDisplayChangelog) {
          var dialog = document.getElementById("changelog-dialog");
          dialog.getElementsByClassName("mdl-dialog__content")[0].innerHTML = "Changelog <br /><br />" + data.changelog.replace(/\n/g, "<br />");
          ;

          dialog.getElementsByClassName("ok")[0].onclick = function () {
            dialog.close();
          };

          dialog.showModal();
        }
      });
    }
  });
}, 2000);
initDragAreas();
var launchCount = store.get("launch_count");

if (launchCount == null || launchCount == undefined) {
  launchCount = 1;
} else launchCount = parseInt(launchCount);

console.log("launch count " + launchCount);
if (launchCount % 10 == 0) setTimeout(function () {
  displaySnack({
    message: "This application was created for free, please, consider making a donation",
    timeout: 10000,
    actionText: "Donate",
    actionHandler: function actionHandler() {
      var url = 'https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YMHT55NSCLER6';
      compatibility.openUrl(url);
    }
  });
}, 10000);
store.set("launch_count", launchCount + 1);
RequestBuilder.sRequestBuilder.get("/settings/browser_css", function (error, data) {
  if (!error && data != null && data != undefined) {
    store.set("css_sheets", JSON.stringify(data));
    var num = 0;
    var _iteratorNormalCompletion6 = true;
    var _didIteratorError6 = false;
    var _iteratorError6 = undefined;

    try {
      for (var _iterator6 = data[Symbol.iterator](), _step6; !(_iteratorNormalCompletion6 = (_step6 = _iterator6.next()).done); _iteratorNormalCompletion6 = true) {
        var sheet = _step6.value;
        Utils.applyCss(sheet, function () {
          num++;
          if (num == data.length) $("#carnet-icon-view").fadeOut('slow');
        });
      }
    } catch (err) {
      _didIteratorError6 = true;
      _iteratorError6 = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion6 && _iterator6["return"] != null) {
          _iterator6["return"]();
        }
      } finally {
        if (_didIteratorError6) {
          throw _iteratorError6;
        }
      }
    }

    if (data.length == 0) $("#carnet-icon-view").fadeOut('slow');
  } else $("#carnet-icon-view").fadeOut('slow');
});
var isDebug = true;
console.oldlog = console.log;
/*console.log = function (m) {
    if (isDebug)
        console.oldlog(m)
}*/

function loadCachedRecentDB() {
  if (cachedRecentDB != undefined) onListEnd("recentdb://", cachedRecentDB, cachedMetadata);
}

UISettingsHelper.getInstance().loadSettings(function (settings, fromCache) {
  console.oldlog("settings from cache " + fromCache + " order " + settings["sort_by"]);
  if (settings['start_page'] == 'recent') initPath = "recentdb://";

  if (settings['start_page'] == 'browser') {
    // we need to load recent db
    RecentDBManager.getInstance().getFlatenDB(function () {});
    initPath = "/";
  }

  setInLineButton(settings['in_line']);
  $("input[name='sort-by'][value='" + settings['sort_by'] + "']").parent().addClass("is-checked");
  $("input[name='sort-by'][value='" + settings['sort_by'] + "']").attr('checked', 'checked');
  document.getElementById("reversed-order").checked = settings['reversed'];

  if (settings['reversed']) {
    document.getElementById("reversed-order").parentNode.classList.add("is-checked");
  }

  if (fromCache) loadCachedRecentDB();else list(initPath, cachedRecentDB != undefined ? true : false);
});
compatibility.loadLang(function () {
  $('body').i18n();
  console.oldlog("lang loaded");
});
$.i18n().locale = navigator.language;
$(".sort-item").click(function () {
  var radioValue = $("input[name='sort-by']:checked").val();

  if (radioValue) {
    UISettingsHelper.getInstance().set('sort_by', radioValue);
    UISettingsHelper.getInstance().set('reversed', document.getElementById("reversed-order").checked);
    sortBy(radioValue, document.getElementById("reversed-order").checked);
    UISettingsHelper.getInstance().postSettings();
  }
});
setDraggable(true);