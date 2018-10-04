"use strict";

var _get = function get(object, property, receiver) { if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { return get(parent, property, receiver); } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var initPath = "recentdb://";
var currentPath;
var currentTask = undefined;
var noteCardViewGrid = undefined;
var notePath = [];
var wasNewNote = false;
var dontOpen = false;
var currentNotePath = undefined;
var root_url = document.getElementById("root-url") != undefined ? document.getElementById("root-url").innerHTML : "";
new RequestBuilder();
/*const {
    ipcRenderer,
    remote,
    app
} = require('electron');*/
var store = new Store();
var noteCacheStr = String(store.get("note_cache"));
if (noteCacheStr == "undefined") noteCacheStr = "{}";
console.log("cache loaded " + noteCacheStr);
var oldNotes = JSON.parse(noteCacheStr);
/*var main = remote.require("./main.js");
var SettingsHelper = require("./settings/settings_helper").SettingsHelper
var settingsHelper = new SettingsHelper()*/
var TextGetterTask = function TextGetterTask(list) {
    this.list = list;
    this.current = 0;
    this.continue = true;
    this.stopAt = 50;
};

TextGetterTask.prototype.startList = function () {
    this.getNext();
};

TextGetterTask.prototype.getNext = function () {
    console.log(this.current);
    if (this.current >= this.stopAt || this.current >= this.list.length) {
        console.log("save cache ");
        store.set("note_cache", JSON.stringify(oldNotes));
        return;
    }

    var paths = "";
    var start = this.current;
    for (var i = start; i < this.stopAt && i < this.list.length && i - start < 10; i++) {
        //do it ten by ten
        this.current = i + 1;
        if (!(this.list[i] instanceof Note)) continue;
        paths += this.list[i].path + ",";
        if (oldNotes[this.list[i].path] == undefined) oldNotes[this.list[i].path] = this.list[i];
    }
    var myTask = this;
    RequestBuilder.sRequestBuilder.get("/metadata?paths=" + encodeURIComponent(paths), function (error, data) {
        for (var meta in data) {
            oldNotes[meta].metadata = data[meta].metadata != undefined ? data[meta].metadata : new NoteMetadata();
            oldNotes[meta].text = data[meta].shorttext;
            oldNotes[meta].previews = data[meta].previews;
            noteCardViewGrid.updateNote(oldNotes[meta]);
            noteCardViewGrid.msnry.layout();
        }
        myTask.getNext();
    });
    /* if (this.list[this.current] instanceof Note) {
         var opener = new NoteOpener(this.list[this.current])
         var myTask = this;
         var note = this.list[this.current]
         var fast = false;
         //should we go fast or slow refresh ?
         for (var i = this.current; i < this.stopAt && i < this.list.length && i < oldNotes.length; i++) {
             if (oldNotes[this.list[i].path] == undefined) {
                 fast = true;
                 break;
             }
         }
         setTimeout(function () {
             try {
                 opener.getMainTextMetadataAndPreviews(function (txt, metadata, previews) {
                     if (myTask.continue) {
                         if (txt != undefined)
                             note.text = txt.substring(0, 200);
                         if (metadata != undefined)
                             note.metadata = metadata;
                         note.previews = previews;
                         oldNotes[note.path] = note;
                         noteCardViewGrid.updateNote(note)
                         noteCardViewGrid.msnry.layout();
                          myTask.getNext();
                     }
                 });
             } catch (error) {
                 console.log(error);
             }
             myTask.current++;
         }, !fast ? 1000 : 100)
      } else {
         this.current++;
         this.getNext();
     }*/
};

var NewNoteCreationTask = function NewNoteCreationTask(callback) {
    var path = currentPath;
    if (path == initPath || path.startsWith("keyword://")) path = main.getNotePath();
    var fs = require('fs');
    if (!fs.exists(path)) {
        var mkdirp = require('mkdirp');
        mkdirp.sync(path);
    }

    var fb = new FileBrowser(path);
    console.log(path + " fefef");
    var task = this;
    fb.list(function (files) {
        task.files = files;
        var name = "untitled.sqd";
        var sContinue = true;
        var i = 1;
        while (sContinue) {
            sContinue = false;
            var _iteratorNormalCompletion = true;
            var _didIteratorError = false;
            var _iteratorError = undefined;

            try {
                for (var _iterator = files[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                    var file = _step.value;

                    if (file.name == name) {
                        sContinue = true;
                        i++;
                        name = "untitled " + i + ".sqd";
                    }
                }
            } catch (err) {
                _didIteratorError = true;
                _iteratorError = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion && _iterator.return) {
                        _iterator.return();
                    }
                } finally {
                    if (_didIteratorError) {
                        throw _iteratorError;
                    }
                }
            }
        }
        callback(path + "/" + name);
    });
};

String.prototype.replaceAll = function (search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};

function openNote(notePath) {
    currentNotePath = notePath;
    if (writerFrame.src == "") {
        if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1 && navigator.userAgent.toLowerCase().indexOf("android") > -1) {
            //open in new tab for firefox android
            window.open(root_url + "../writer.php?path=" + encodeURIComponent(notePath), "_blank");
        } else {
            writerFrame.src = root_url + "../writer.php?path=" + encodeURIComponent(notePath);
            writerFrame.style.display = "block";
            loadingView.style.display = "block";
        }
    } else {
        console.log("reuse old iframe");
        writerFrame.contentWindow.loadPath(notePath);
        writerFrame.style.display = "block";
        loadingView.style.display = "block";
    }

    //window.location.assign("writer?path=" + encodeURIComponent(notePath));
}

function oldOpenNote(notePath) {
    currentNotePath = notePath;
    var electron = require('electron');
    var remote = electron.remote;
    var BrowserWindow = remote.BrowserWindow;
    var path = require('path');
    //var win = new BrowserWindow({ width: 800, height: 600 });
    if (!hasLoadedOnce) $(loadingView).fadeIn();
    //$(browserElem).faceOut();
    var rimraf = require('rimraf');
    var tmp = path.join(main.getPath("temp"), "tmpquicknote");
    rimraf(tmp, function () {
        var fs = require('fs');

        fs.mkdir(tmp, function (e) {
            fs.readFile(__dirname + '/reader/reader.html', 'utf8', function (err, data) {
                if (err) {
                    fs.rea;
                    console.log("error ");
                    return console.log(err);
                }
                var index = path.join(tmp, 'reader.html');
                fs.writeFileSync(index, data.replace(new RegExp('<!ROOTPATH>', 'g'), __dirname + '/'));
                /* var size = remote.getCurrentWindow().getSize();
                 var pos = remote.getCurrentWindow().getPosition();
                 var win = new BrowserWindow({
                     width: size[0],
                     height: size[1],
                     x: pos[0],
                     y: pos[1],
                     frame: false
                 });
                 console.log("w " + remote.getCurrentWindow().getPosition()[0])
                 const url = require('url')
                 win.loadURL(url.format({
                     pathname: path.join(__dirname, 'tmp/reader.html'),
                     protocol: 'file:',
                     query: {
                         'path': notePath
                     },
                     slashes: true
                 }))*/
                var url = require('url');

                if (!hasLoadedOnce) {
                    webview.setAttribute("src", url.format({
                        pathname: index,
                        protocol: 'file:',
                        query: {
                            'path': notePath,
                            'tmppath': tmp + "/"
                        },
                        slashes: true
                    }));
                } else webview.send('loadnote', notePath);
                webview.style = "position:fixed; top:0px; left:0px; height:100%; width:100%; z-index:100; right:0; bottom:0;";
                //to resize properly
                hasLoadedOnce = true;
            });
        });
    });
}

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
        var _iteratorNormalCompletion2 = true;
        var _didIteratorError2 = false;
        var _iteratorError2 = undefined;

        try {
            var _loop = function _loop() {
                var key = _step2.value;
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

            for (var _iterator2 = dataArray[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
                var keywordElem;

                _loop();
            }
        } catch (err) {
            _didIteratorError2 = true;
            _iteratorError2 = err;
        } finally {
            try {
                if (!_iteratorNormalCompletion2 && _iterator2.return) {
                    _iterator2.return();
                }
            } finally {
                if (_didIteratorError2) {
                    throw _iteratorError2;
                }
            }
        }
    });
}

function searchInNotes(searching) {
    resetGrid(false);
    notes = [];

    RequestBuilder.sRequestBuilder.get("/notes/search?path=." + "&query=" + encodeURIComponent(searching), function (error, data) {
        if (!error) {
            list("search://", true);
        }
    });
}

function resetGrid(discret) {
    var grid = document.getElementById("page-content");
    var scroll = 0;
    if (discret) scroll = document.getElementById("grid-container").scrollTop;
    grid.innerHTML = "";
    noteCardViewGrid = new NoteCardViewGrid(grid, discret, onDragEnd);
    this.noteCardViewGrid = noteCardViewGrid;
    noteCardViewGrid.onFolderClick(function (folder) {
        list(folder.path);
    });
    noteCardViewGrid.onNoteClick(function (note) {
        if (!dontOpen) openNote(note.path);
        dontOpen = false;
        // var reader = new Writer(note,"");
        // reader.extractNote()
    });

    noteCardViewGrid.onMenuClick(function (note) {
        mNoteContextualDialog.show(note);
    });

    noteCardViewGrid.onFolderMenuClick(function (folder) {
        mFolderContextualDialog.show(folder);
    });
}

var ContextualDialog = function () {
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

var NewFolderDialog = function (_ContextualDialog) {
    _inherits(NewFolderDialog, _ContextualDialog);

    function NewFolderDialog() {
        _classCallCheck(this, NewFolderDialog);

        var _this = _possibleConstructorReturn(this, (NewFolderDialog.__proto__ || Object.getPrototypeOf(NewFolderDialog)).call(this));

        _this.showDelete = false;
        _this.showArchive = false;
        _this.showPin = false;
        return _this;
    }

    _createClass(NewFolderDialog, [{
        key: "show",
        value: function show() {
            var context = this;
            this.nameInput.value = "";

            this.ok.onclick = function () {
                RequestBuilder.sRequestBuilder.post("/browser/newfolder", {
                    path: currentPath + "/" + context.nameInput.value
                }, function (error) {
                    if (error) {}
                    list(currentPath, true);
                    context.dialog.close();
                });
            };
            _get(NewFolderDialog.prototype.__proto__ || Object.getPrototypeOf(NewFolderDialog.prototype), "show", this).call(this);
        }
    }]);

    return NewFolderDialog;
}(ContextualDialog);

var NoteContextualDialog = function (_ContextualDialog2) {
    _inherits(NoteContextualDialog, _ContextualDialog2);

    function NoteContextualDialog() {
        _classCallCheck(this, NoteContextualDialog);

        return _possibleConstructorReturn(this, (NoteContextualDialog.__proto__ || Object.getPrototypeOf(NoteContextualDialog)).call(this));
    }

    _createClass(NoteContextualDialog, [{
        key: "show",
        value: function show(note) {
            var context = this;
            this.nameInput.value = note.title;
            this.deleteButton.onclick = function () {
                var db = new RecentDBManager();
                context.dialog.close();
                db.removeFromDB(note.path, function (error, data) {
                    if (!error) RequestBuilder.sRequestBuilder.delete("/notes?path=" + encodeURIComponent(note.path), function () {
                        list(currentPath, true);
                    });
                });
            };
            this.archiveButton.onclick = function () {
                var db = new RecentDBManager();
                db.removeFromDB(note.path, function () {
                    context.dialog.close();
                    list(currentPath, true);
                });
            };
            if (note.isPinned == true) {
                this.pinButton.innerHTML = "Unpin";
            } else this.pinButton.innerHTML = "Pin";

            this.pinButton.onclick = function () {
                var db = new RecentDBManager();
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
                var _iteratorNormalCompletion3 = true;
                var _didIteratorError3 = false;
                var _iteratorError3 = undefined;

                try {
                    for (var _iterator3 = context.nameInput.value.split("/")[Symbol.iterator](), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
                        var part = _step3.value;

                        if (part == ".." && !hasOrigin) {
                            path = FileUtils.getParentFolderFromPath(path);
                        } else {
                            hasOrigin = true;
                            path += "/" + part;
                        }
                    }
                } catch (err) {
                    _didIteratorError3 = true;
                    _iteratorError3 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion3 && _iterator3.return) {
                            _iterator3.return();
                        }
                    } finally {
                        if (_didIteratorError3) {
                            throw _iteratorError3;
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
            _get(NoteContextualDialog.prototype.__proto__ || Object.getPrototypeOf(NoteContextualDialog.prototype), "show", this).call(this);
        }
    }]);

    return NoteContextualDialog;
}(ContextualDialog);

var FolderContextualDialog = function (_ContextualDialog3) {
    _inherits(FolderContextualDialog, _ContextualDialog3);

    function FolderContextualDialog() {
        _classCallCheck(this, FolderContextualDialog);

        var _this3 = _possibleConstructorReturn(this, (FolderContextualDialog.__proto__ || Object.getPrototypeOf(FolderContextualDialog)).call(this));

        _this3.showArchive = false;
        _this3.showPin = false;
        return _this3;
    }

    _createClass(FolderContextualDialog, [{
        key: "show",
        value: function show(folder) {
            var context = this;
            this.nameInput.value = folder.name;

            this.ok.onclick = function () {
                var path = FileUtils.getParentFolderFromPath(folder.path);
                var hasOrigin = false;
                var _iteratorNormalCompletion4 = true;
                var _didIteratorError4 = false;
                var _iteratorError4 = undefined;

                try {
                    for (var _iterator4 = context.nameInput.value.split("/")[Symbol.iterator](), _step4; !(_iteratorNormalCompletion4 = (_step4 = _iterator4.next()).done); _iteratorNormalCompletion4 = true) {
                        var part = _step4.value;

                        if (part == ".." && !hasOrigin) {
                            path = FileUtils.getParentFolderFromPath(path);
                        } else {
                            hasOrigin = true;
                            path += "/" + part;
                        }
                    }
                } catch (err) {
                    _didIteratorError4 = true;
                    _iteratorError4 = err;
                } finally {
                    try {
                        if (!_iteratorNormalCompletion4 && _iterator4.return) {
                            _iterator4.return();
                        }
                    } finally {
                        if (_didIteratorError4) {
                            throw _iteratorError4;
                        }
                    }
                }

                RequestBuilder.sRequestBuilder.post("/browser/move", {
                    from: folder.path,
                    to: path
                }, function () {
                    list(currentPath, true);
                });
                context.dialog.close();
            };
            _get(FolderContextualDialog.prototype.__proto__ || Object.getPrototypeOf(FolderContextualDialog.prototype), "show", this).call(this);
        }
    }]);

    return FolderContextualDialog;
}(ContextualDialog);

var mNoteContextualDialog = new NoteContextualDialog();
var mFolderContextualDialog = new FolderContextualDialog();

var mNewFolderDialog = new NewFolderDialog();

function list(pathToList, discret) {
    if (pathToList == undefined) pathToList = currentPath;
    console.log("listing path " + pathToList);
    currentPath = pathToList;
    var settingsHelper = {};
    settingsHelper.getNotePath = function () {
        return "pet";
    };
    if (pathToList == settingsHelper.getNotePath() || pathToList == initPath || pathToList.startsWith("keyword://")) {
        if (pathToList != settingsHelper.getNotePath()) {
            $("#add-directory-button").hide();
        } else $("#add-directory-button").show();

        $("#back_arrow").hide();
    } else {
        $("#back_arrow").show();
        $("#add-directory-button").show();
    }

    var fb = new FileBrowser(pathToList);
    fb.list(function (files, endOfSearch) {
        resetGrid(discret);
        var noteCardViewGrid = this.noteCardViewGrid;
        var notes = [];
        notePath = [];
        if (currentTask != undefined) currentTask.continue = false;
        if (files.length > 0) {
            $("#emty-view").fadeOut("fast");
        } else if (endOfSearch) $("#emty-view").fadeIn("fast");
        var _iteratorNormalCompletion5 = true;
        var _didIteratorError5 = false;
        var _iteratorError5 = undefined;

        try {
            for (var _iterator5 = files[Symbol.iterator](), _step5; !(_iteratorNormalCompletion5 = (_step5 = _iterator5.next()).done); _iteratorNormalCompletion5 = true) {
                var file = _step5.value;

                var filename = getFilenameFromPath(file.path);
                if (file.isFile && filename.endsWith(".sqd")) {
                    var oldNote = oldNotes[file.path];

                    var noteTestTxt = new Note(stripExtensionFromName(filename), oldNote != undefined ? oldNote.text : "", file.path, oldNote != undefined ? oldNote.metadata : undefined, oldNote != undefined ? oldNote.previews : undefined);
                    noteTestTxt.isPinned = file.isPinned;
                    notes.push(noteTestTxt);
                } else if (!file.isFile) {

                    notes.push(file);
                }
                notePath.push(file.path);
            }
        } catch (err) {
            _didIteratorError5 = true;
            _iteratorError5 = err;
        } finally {
            try {
                if (!_iteratorNormalCompletion5 && _iterator5.return) {
                    _iterator5.return();
                }
            } finally {
                if (_didIteratorError5) {
                    throw _iteratorError5;
                }
            }
        }

        noteCardViewGrid.setNotesAndFolders(notes);
        if (discret) {
            document.getElementById("grid-container").scrollTop = scroll;
            console.log("scroll : " + scroll);
        }
        currentTask = new TextGetterTask(notes);
        console.log("stopping and starting task");
        currentTask.startList();
        if (!endOfSearch) {
            setTimeout(function () {
                list(pathToList, true);
            }, 1000);
        }
    });
}
list(initPath);
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

function toggleSearch() {
    $("#search-container").slideToggle();
    if ($("#search-container").css("display") != "none") $("#search-input").focus();
}

/*main.setMergeListener(function () {
    list(initPath, true)
})*/

document.getElementById("add-note-button").onclick = function () {
    var path = currentPath;
    if (path == initPath || path.startsWith("keyword://")) path = "";
    RequestBuilder.sRequestBuilder.get("/note/create?path=" + encodeURIComponent(path), function (error, data) {
        if (error) return;
        console.log("found " + data);
        wasNewNote = true;
        var db = new RecentDBManager();
        db.addToDB(data, function () {
            openNote(data);
        });
    });
    /*new NewNoteCreationTask(function (path) {
        console.log("found " + path)
        wasNewNote = true;
        var db = new RecentDBManager(main.getNotePath() + "/quickdoc/recentdb/" + main.getAppUid())
        db.addToDB(NoteUtils.getNoteRelativePath(main.getNotePath(), path));
        openNote(path)
    })*/
};

document.getElementById("add-directory-button").onclick = function () {
    mNewFolderDialog.show();
};

document.getElementById("search-input").onkeydown = function (event) {
    if (event.key === 'Enter') {
        toggleSearch();
        searchInNotes(this.value);
    }
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

var browser = undefined;
document.getElementById("grid-container").onscroll = function () {
    if (this.offsetHeight + this.scrollTop >= this.scrollHeight - 80) {
        loadNextNotes();
    }
};
var webview = document.getElementById("writer-webview");

webview.addEventListener('ipc-message', function (event) {
    if (event.channel == "exit") {
        webview.style = "position:fixed; top:0px; left:0px; height:0px; width:0px; z-index:100; right:0; bottom:0;";
        //$(browserElem).faceIn();
        $("#no-drag-bar").hide();
        if (wasNewNote) list(currentPath, true);else if (currentTask != undefined) {
            var noteIndex;
            if ((noteIndex = notePath.indexOf(currentNotePath)) == -1) {
                noteIndex = 0;
            }
            currentTask.current = noteIndex;
            currentTask.getNext();
        }
        wasNewNote = false;
        refreshKeywords();
    } else if (event.channel == "loaded") {
        $(loadingView).fadeOut();
        $("#no-drag-bar").show();
    }
});
var hasLoadedOnce = false;
webview.addEventListener('dom-ready', function () {
    webview.openDevTools();
});
var loadingView = document.getElementById("loading-view");
//var browserElem = document.getElementById("browser")
console.log("pet");

var _iteratorNormalCompletion6 = true;
var _didIteratorError6 = false;
var _iteratorError6 = undefined;

try {
    for (var _iterator6 = document.getElementsByClassName("mdl-dialog")[Symbol.iterator](), _step6; !(_iteratorNormalCompletion6 = (_step6 = _iterator6.next()).done); _iteratorNormalCompletion6 = true) {
        var dia = _step6.value;

        dialogPolyfill.registerDialog(dia);
    }
} catch (err) {
    _didIteratorError6 = true;
    _iteratorError6 = err;
} finally {
    try {
        if (!_iteratorNormalCompletion6 && _iterator6.return) {
            _iterator6.return();
        }
    } finally {
        if (_didIteratorError6) {
            throw _iteratorError6;
        }
    }
}

document.getElementById("search-button").onclick = function () {
    toggleSearch();
};

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
document.getElementById("size-button").onclick = function () {
    if (!isFullScreen()) {
        var docElm = document.getElementById("content");
        if (docElm.requestFullscreen) {
            docElm.requestFullscreen();
        } else if (docElm.mozRequestFullScreen) {
            docElm.mozRequestFullScreen();
        } else if (docElm.webkitRequestFullScreen) {
            docElm.webkitRequestFullScreen();
        }
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        } else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
        }
    }
};

RequestBuilder.sRequestBuilder.get("/recentdb/merge", function (error, data) {
    if (data == true && currentPath == "recentdb://") list("recentdb://", true);
});

RequestBuilder.sRequestBuilder.get("/keywordsdb/merge", function (error, data) {
    if (data == true) refreshKeywords();
});

var isWeb = true;
var right = document.getElementById("right-bar");
if (isWeb) {
    right.removeChild(document.getElementById("minus-button"));
    right.removeChild(document.getElementById("close-button"));
    document.getElementById("settings-button").href = "./settings";
}

//writer frame

var isElectron = typeof require === "function";
var writerFrame = undefined;
events = [];

if (isElectron) {} else {
    writerFrame = document.getElementById("writer-iframe");
    //iframe events

    var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
    var eventer = window[eventMethod];
    var messageEvent = eventMethod === "attachEvent" ? "onmessage" : "message";
    eventer(messageEvent, function (e) {
        if (events[e.data] !== undefined) {
            var _iteratorNormalCompletion7 = true;
            var _didIteratorError7 = false;
            var _iteratorError7 = undefined;

            try {
                for (var _iterator7 = events[e.data][Symbol.iterator](), _step7; !(_iteratorNormalCompletion7 = (_step7 = _iterator7.next()).done); _iteratorNormalCompletion7 = true) {
                    var callback = _step7.value;

                    callback();
                }
            } catch (err) {
                _didIteratorError7 = true;
                _iteratorError7 = err;
            } finally {
                try {
                    if (!_iteratorNormalCompletion7 && _iterator7.return) {
                        _iterator7.return();
                    }
                } finally {
                    if (_didIteratorError7) {
                        throw _iteratorError7;
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
    document.getElementById("writer-iframe").style.display = "none";
    if (wasNewNote) list();else {
        if (currentTask != undefined) {
            var index = notePath.indexOf(currentNotePath);
            currentTask.current = index;
            currentTask.stopAt = index + 1;
            currentTask.startList();
        }
    }
});

registerWriterEvent("loaded", function () {
    loadingView.style.display = "none";
});

setTimeout(function () {
    RequestBuilder.sRequestBuilder.get("/settings/isfirstrun", function (error, data) {
        if (!error && data == true) {
            var elem = document.getElementById("firstrun-container");
            $(elem).show();
            $("#firstrun").slideToggle();
            new Slides(elem, function () {
                $("#firstrun").slideToggle(function () {
                    $(elem).hide();
                });
            });
        }
    });
}, 2000);
initDragAreas();