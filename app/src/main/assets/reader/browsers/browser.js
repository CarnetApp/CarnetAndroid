"use strict";

function _createForOfIteratorHelper(o, allowArrayLike) { var it = typeof Symbol !== "undefined" && o[Symbol.iterator] || o["@@iterator"]; if (!it) { if (Array.isArray(o) || (it = _unsupportedIterableToArray(o)) || allowArrayLike && o && typeof o.length === "number") { if (it) o = it; var i = 0; var F = function F() {}; return { s: F, n: function n() { if (i >= o.length) return { done: true }; return { done: false, value: o[i++] }; }, e: function e(_e) { throw _e; }, f: F }; } throw new TypeError("Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); } var normalCompletion = true, didErr = false, err; return { s: function s() { it = it.call(o); }, n: function n() { var step = it.next(); normalCompletion = step.done; return step; }, e: function e(_e2) { didErr = true; err = _e2; }, f: function f() { try { if (!normalCompletion && it["return"] != null) it["return"](); } finally { if (didErr) throw err; } } }; }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i]; return arr2; }
function _get() { if (typeof Reflect !== "undefined" && Reflect.get) { _get = Reflect.get.bind(); } else { _get = function _get(target, property, receiver) { var base = _superPropBase(target, property); if (!base) return; var desc = Object.getOwnPropertyDescriptor(base, property); if (desc.get) { return desc.get.call(arguments.length < 3 ? target : receiver); } return desc.value; }; } return _get.apply(this, arguments); }
function _superPropBase(object, property) { while (!Object.prototype.hasOwnProperty.call(object, property)) { object = _getPrototypeOf(object); if (object === null) break; } return object; }
function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); Object.defineProperty(subClass, "prototype", { writable: false }); if (superClass) _setPrototypeOf(subClass, superClass); }
function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }
function _createSuper(Derived) { var hasNativeReflectConstruct = _isNativeReflectConstruct(); return function _createSuperInternal() { var Super = _getPrototypeOf(Derived), result; if (hasNativeReflectConstruct) { var NewTarget = _getPrototypeOf(this).constructor; result = Reflect.construct(Super, arguments, NewTarget); } else { result = Super.apply(this, arguments); } return _possibleConstructorReturn(this, result); }; }
function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } else if (call !== void 0) { throw new TypeError("Derived constructors may only return object or undefined"); } return _assertThisInitialized(self); }
function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }
function _isNativeReflectConstruct() { if (typeof Reflect === "undefined" || !Reflect.construct) return false; if (Reflect.construct.sham) return false; if (typeof Proxy === "function") return true; try { Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function () {})); return true; } catch (e) { return false; } }
function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf.bind() : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }
function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, _toPropertyKey(descriptor.key), descriptor); } }
function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); Object.defineProperty(Constructor, "prototype", { writable: false }); return Constructor; }
function _toPropertyKey(arg) { var key = _toPrimitive(arg, "string"); return _typeof(key) === "symbol" ? key : String(key); }
function _toPrimitive(input, hint) { if (_typeof(input) !== "object" || input === null) return input; var prim = input[Symbol.toPrimitive]; if (prim !== undefined) { var res = prim.call(input, hint || "default"); if (_typeof(res) !== "object") return res; throw new TypeError("@@toPrimitive must return a primitive value."); } return (hint === "string" ? String : Number)(input); }
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
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
var shouldPreloadEditor = false; // used to preopen editor
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
    paths += "paths[]=" + encodeURIComponent(this.list[i].path) + "&";
    if (cachedMetadata[this.list[i].path] == undefined) cachedMetadata[this.list[i].path] = this.list[i];
  }
  var myTask = this;
  if (paths.length > 0) {
    RequestBuilder.sRequestBuilder.get("/metadata?" + paths, function (error, data) {
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
function onPrepared(error, data, notePath, action) {
  if (error) return;
  if (writerFrame.src == "") {
    if (notePath !== undefined) {
      $("#editor-container").show();
      $(loadingView).fadeIn(function () {
        writerFrame.style.display = "inline-flex";
        if (compatibility.isElectron) {
          writerFrame.addEventListener("dom-ready", function () {
            var remote = require('@electron/remote');
            var main = remote.require("./main");
            main.enableEditorWebContent(document.getElementById("writer-webview").getWebContentsId());
            // writerFrame.openDevTools()
            writerFrame.send('remote_ready', undefined);
          });
        }
        writerFrame.src = data + (notePath != undefined ? "?path=" + encodeURIComponent(notePath) + (action != undefined ? "&action=" + action : "") : "");
      });
    }
  } else {
    console.log("reuse old iframe");
    $("#editor-container").show();
    if (compatibility.isElectron) {
      writerFrame.send('loadnote', notePath);
      writerFrame.send('action', action);
    } else writerFrame.contentWindow.loadPath(notePath, action);
    $(loadingView).fadeIn(function () {
      writerFrame.style.display = "inline-flex";
    });
  }
}
function openNote(notePath, action) {
  isLoadCanceled = false;
  currentNotePath = notePath;
  if (compatibility.isElectron) {
    RequestBuilder.sRequestBuilder.get("/note/open/prepare", function (error, url) {
      onPrepared(error, url, notePath, action);
    });
  } else {
    //no need to call, always the same on nextcloud
    onPrepared(undefined, "./writer", notePath, action);
  }
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
        keywordElem.dir = "auto";
        keywordElem.classList.add("mdl-navigation__link");
        keywordElem.innerHTML = key;
        keywordElem.setAttribute("href", "");
        keywordElem.onclick = function () {
          toggleDrawer();
          list("keyword://" + key, false);
          return false;
        };
        keywordsContainer.appendChild(keywordElem);
      },
      keywordElem;
    for (var _i = 0, _dataArray = dataArray; _i < _dataArray.length; _i++) {
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
var ContextualDialog = /*#__PURE__*/function () {
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
var NewFolderDialog = /*#__PURE__*/function (_ContextualDialog) {
  _inherits(NewFolderDialog, _ContextualDialog);
  var _super = _createSuper(NewFolderDialog);
  function NewFolderDialog() {
    var _this;
    _classCallCheck(this, NewFolderDialog);
    _this = _super.call(this);
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
var NoteContextualDialog = /*#__PURE__*/function (_ContextualDialog2) {
  _inherits(NoteContextualDialog, _ContextualDialog2);
  var _super2 = _createSuper(NoteContextualDialog);
  function NoteContextualDialog() {
    _classCallCheck(this, NoteContextualDialog);
    return _super2.call(this);
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
        var _iterator = _createForOfIteratorHelper(context.nameInput.value.split("/")),
          _step;
        try {
          for (_iterator.s(); !(_step = _iterator.n()).done;) {
            var part = _step.value;
            if (part == ".." && !hasOrigin) {
              path = FileUtils.getParentFolderFromPath(path);
            } else {
              hasOrigin = true;
              path += "/" + part;
            }
          }
        } catch (err) {
          _iterator.e(err);
        } finally {
          _iterator.f();
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
  var _iterator2 = _createForOfIteratorHelper(notes),
    _step2;
  try {
    for (_iterator2.s(); !(_step2 = _iterator2.n()).done;) {
      var item = _step2.value;
      notePath.push(item.path);
    }
  } catch (err) {
    _iterator2.e(err);
  } finally {
    _iterator2.f();
  }
  noteCardViewGrid.setNotesAndFolders(notes);
  currentTask = new TextGetterTask(notes);
  console.log("stopping and starting task");
  currentTask.startList();
}
function onListEnd(pathToList, files, metadatas, discret, force, fromCache) {
  lastListingRequestId = undefined;
  if (!_.isEqual(files, oldFiles) || force) {
    var scroll = resetGrid(discret);
    oldFiles = files;
    var noteCardViewGrid = this.noteCardViewGrid;
    notes = [];
    notePath = [];
    if (currentTask != undefined) currentTask["continue"] = false;
    var i = 0;
    var _iterator3 = _createForOfIteratorHelper(files),
      _step3;
    try {
      for (_iterator3.s(); !(_step3 = _iterator3.n()).done;) {
        var file = _step3.value;
        var filename = getFilenameFromPath(file.path);
        if (filename.endsWith(".sqd")) {
          var metadata = undefined;
          if (metadatas != undefined) {
            metadata = metadatas[file.path];
            //bad fix for paths starting with / but having not metadata... Need to find out why
            if (metadata == undefined && file.path.startsWith("/")) {
              metadata = metadatas[file.path.substr(1)];
            }
          }
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
      _iterator3.e(err);
    } finally {
      _iterator3.f();
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
    if (!fromCache && shouldPreloadEditor) {
      openNote(undefined, undefined);
      console.log("preloading");
      shouldPreloadEditor = false;
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
var loadingView = document.getElementById("loading-view");
//var browserElem = document.getElementById("browser")
console.log("pet");
var dias = document.getElementsByClassName("mdl-dialog");
for (var i = 0; i < dias.length; i++) {
  dialogPolyfill.registerDialog(dias[i]);
}

//nav buttons
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
  refreshKeywords();
});
var isWeb = true;
var right = document.getElementById("right-bar");

//writer frame

var isElectron = typeof require === "function";
var writerFrame = undefined;
var events = [];
if (isElectron) {
  writerFrame = document.getElementById("writer-webview");
  writerFrame.addEventListener('ipc-message', function (event) {
    if (events[event.channel] !== undefined) {
      var _iterator4 = _createForOfIteratorHelper(events[event.channel]),
        _step4;
      try {
        for (_iterator4.s(); !(_step4 = _iterator4.n()).done;) {
          var callback = _step4.value;
          callback();
        }
      } catch (err) {
        _iterator4.e(err);
      } finally {
        _iterator4.f();
      }
    }
  });
} else {
  writerFrame = document.getElementById("writer-iframe");
  //iframe events

  var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
  var eventer = window[eventMethod];
  var messageEvent = eventMethod === "attachEvent" ? "onmessage" : "message";
  eventer(messageEvent, function (e) {
    if (events[e.data] !== undefined) {
      var _iterator5 = _createForOfIteratorHelper(events[e.data]),
        _step5;
      try {
        for (_iterator5.s(); !(_step5 = _iterator5.n()).done;) {
          var callback = _step5.value;
          callback();
        }
      } catch (err) {
        _iterator5.e(err);
      } finally {
        _iterator5.f();
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
}

// line / grid switch

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
    var _iterator6 = _createForOfIteratorHelper(data),
      _step6;
    try {
      for (_iterator6.s(); !(_step6 = _iterator6.n()).done;) {
        var sheet = _step6.value;
        Utils.applyCss(sheet, function () {
          num++;
          if (num == data.length) $("#carnet-icon-view").fadeOut('slow');
        });
      }
    } catch (err) {
      _iterator6.e(err);
    } finally {
      _iterator6.f();
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
  if (cachedRecentDB != undefined) onListEnd("recentdb://", cachedRecentDB, cachedMetadata, false, false, true);
}
UISettingsHelper.getInstance().loadSettings(function (settings, fromCache) {
  console.oldlog("settings from cache " + fromCache + " order " + settings["sort_by"]);
  shouldPreloadEditor = settings['should_preload_editor'];
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