"use strict";

var NoteCardView = function NoteCardView(elem, onTodoListChange) {
  this.elem = elem;
  this.init();
  this.onTodoListChange = onTodoListChange;
};

NoteCardView.prototype.refreshTodoList = function () {
  this.cardTodoLists.innerHTML = "";

  for (var i = 0; i < this.note.metadata.todolists.length; i++) {
    var todolist = this.note.metadata.todolists[i];
    if (todolist.todo == undefined) continue;
    var todolistDiv = document.createElement("div");
    todolistDiv.classList.add("todo-list");

    for (var j = 0; j < todolist.todo.length; j++) {
      var id = Utils.generateUID();
      var label = document.createElement("label");
      label.classList.add("mdl-checkbox");
      label.classList.add("mdl-js-checkbox");
      label.classList.add("mdl-js-ripple-effect");
      label["for"] = id;
      var input = document.createElement("input");
      input.type = "checkbox";
      input.id = id;
      input.classList.add("mdl-checkbox__input");
      label.appendChild(input);
      var span = document.createElement("span");
      span.classList.add("mdl-checkbox__label");
      span.classList.add("todo-item-text");
      span.innerHTML = todolist.todo[j].replace(/(?:\r\n|\r|\n)/g, '<br />');
      label.appendChild(span);
      var noteCard = this;
      label.i = i;
      label.j = j;
      label.checkbox = new window['MaterialCheckbox'](label);

      label.onclick = function () {
        noteCard.elem.classList.add("noclick");
        this.checkbox.check();
        var item = noteCard.note.metadata.todolists[this.i].todo[this.j];
        noteCard.note.metadata.todolists[this.i].todo.splice(this.j, 1);
        noteCard.note.metadata.todolists[this.i].done.push(item);
        noteCard.onTodoListChange(noteCard.note);
        setTimeout(function () {
          noteCard.refreshTodoList();
        }, 500);
        return false;
      };

      todolistDiv.appendChild(label);
    }

    this.cardTodoLists.appendChild(todolistDiv);
  }
};

NoteCardView.prototype.setNote = function (note) {
  var _this = this;

  if (this.oldColor != undefined) {
    this.elem.classList.remove(this.oldColor);
  }

  this.note = note;

  if (this.note.metadata != undefined && this.note.metadata.color != undefined) {
    this.elem.classList.add(this.note.metadata.color);
    this.oldColor = this.note.metadata.color;
  }

  if (note.title.indexOf("untitled") == 0) this.cardTitleText.innerHTML = "";else this.cardTitleText.innerHTML = note.title;
  var dateStamp = note.metadata.custom_date;
  if (dateStamp == undefined) dateStamp = note.metadata.last_modification_date;
  var date = new Date(dateStamp).toLocaleDateString();
  var text = "";
  if (note.text != undefined) if (note.metadata.urls != undefined && note.metadata.urls.length > 0) {
    text = note.text.replace(Utils.httpReg, "");
  } else text = note.text; //avoid empty note for old notes

  this.cardText.innerHTML = text;
  this.cardText.classList.remove("big-text");
  this.cardText.classList.remove("medium-text");

  if (note.metadata.todolists != undefined) {
    this.refreshTodoList();
  } else {
    if (text.length < 40 && this.cardTitleText.innerHTML == "") this.cardText.classList.add("big-text");else if (text.length < 100 && this.cardTitleText.innerHTML == "") {
      this.cardText.classList.add("medium-text");
    }
  }

  this.cardDate.innerHTML = date;
  if (note.metadata.rating > 0) this.cardRating.innerHTML = note.metadata.rating + "★";
  this.cardKeywords.innerHTML = "";

  if (typeof note.metadata.keywords[Symbol.iterator] === 'function') {
    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
      for (var _iterator = note.metadata.keywords[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
        var keyword = _step.value;
        console.log("keyword " + keyword);
        keywordSpan = document.createElement('span');
        keywordSpan.innerHTML = keyword;
        keywordSpan.classList.add("keyword");
        this.cardKeywords.appendChild(keywordSpan);
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
  }

  this.cardMedias.innerHTML = "";

  if (note.previews != undefined) {
    var _iteratorNormalCompletion2 = true;
    var _didIteratorError2 = false;
    var _iteratorError2 = undefined;

    try {
      for (var _iterator2 = note.previews[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
        var preview = _step2.value;
        var img = document.createElement('img');
        img.src = preview;
        this.cardMedias.appendChild(img);
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
  }

  this.cardUrls.innerHTML = "";

  if (note.metadata.urls != undefined) {
    var _loop = function _loop() {
      var url = _Object$keys[_i];
      div = document.createElement('div');
      div.classList.add("note-url");
      a = document.createElement('a');
      a.href = url;

      a.onclick = function () {
        return false;
      };

      div.onclick = function (event) {
        event.stopPropagation();
        compatibility.openUrl(url);
        return false;
      };

      a.innerHTML = url;
      div.appendChild(a);

      _this.cardUrls.appendChild(div);
    };

    for (var _i = 0, _Object$keys = Object.keys(note.metadata.urls); _i < _Object$keys.length; _i++) {
      var div;
      var a;

      _loop();
    }
  }
};

NoteCardView.prototype.init = function () {
  this.elem.classList.add("mdl-card");
  this.elem.classList.add("note-card-view");
  this.menuButton = document.createElement('button');
  this.menuButton.classList.add("mdl-button");
  this.menuButton.classList.add("mdl-js-button");
  this.menuButton.classList.add("mdl-button--icon");
  this.menuButton.classList.add("card-more-button");
  var menuButtonIcon = document.createElement('li');
  menuButtonIcon.classList.add("material-icons");
  menuButtonIcon.innerHTML = "more_vert";
  this.menuButton.appendChild(menuButtonIcon);
  this.elem.classList.add("mdl-shadow--2dp");
  this.cardContent = document.createElement('div');
  this.cardContent.classList.add("mdl-card__supporting-text");
  this.cardContent.appendChild(this.menuButton);
  this.cardText = document.createElement('div');
  this.cardText.classList.add("card-text");
  this.cardTodoLists = document.createElement('div');
  this.cardTitleText = document.createElement('h2');
  this.cardTitleText.classList.add("card-title");
  this.cardContent.appendChild(this.cardTitleText);
  this.cardContent.appendChild(this.cardText);
  this.cardContent.appendChild(this.cardTodoLists);
  this.cardRating = document.createElement('div');
  this.cardRating.classList.add("card-rating");
  this.cardContent.appendChild(this.cardRating);
  this.cardDate = document.createElement('div');
  this.cardDate.classList.add("card-date");
  this.cardContent.appendChild(this.cardDate);
  this.cardKeywords = document.createElement('div');
  this.cardKeywords.classList.add("keywords");
  this.cardContent.appendChild(this.cardKeywords);
  this.cardUrls = document.createElement('div');
  this.cardUrls.classList.add("card-urls");
  this.cardContent.appendChild(this.cardUrls);
  this.cardMedias = document.createElement('div');
  this.cardMedias.classList.add("card-medias");
  this.cardContent.appendChild(this.cardMedias);
  this.elem.appendChild(this.cardContent);
}; //var Masonry = require('masonry-layout');


var NoteCardViewGrid = function NoteCardViewGrid(elem, discret, dragCallback) {
  this.elem = elem;
  this.discret = discret;
  this.init();
  this.dragCallback = dragCallback;
};

NoteCardViewGrid.prototype.init = function () {
  this.noteCards = [];
  this.lastAdded = 0;
  this.notes = [];
  var grid = this; //calculating card width

  this.width = 200;

  if (document.body.clientWidth / 2 - 10 < 200) {
    if (document.body.clientWidth > 300) this.width = document.body.clientWidth / 2 - 13;else this.width = document.body.clientWidth - 10;
  }

  var Masonry = compatibility.getMasonry();
  this.msnry = new Masonry(this.elem, {
    // options
    itemSelector: '.demo-card-wide.mdl-card',
    fitWidth: true,
    columnWidth: this.width + 10,
    transitionDuration: grid.discret ? 0 : "0.6s",
    animationOptions: {
      queue: false,
      isAnimated: false
    }
  });
};

NoteCardViewGrid.prototype.onFolderClick = function (callback) {
  this.onFolderClick = callback;
};

NoteCardViewGrid.prototype.onNoteClick = function (callback) {
  this.onNoteClick = callback;
};

NoteCardViewGrid.prototype.onMenuClick = function (callback) {
  this.onMenuClick = callback;
};

NoteCardViewGrid.prototype.updateNote = function (note) {
  for (var i = 0; i < this.noteCards.length; i++) {
    var noteCard = this.noteCards[i];

    if (noteCard.note.path == note.path) {
      noteCard.setNote(note);
    }
  }
};

NoteCardViewGrid.prototype.setNotesAndFolders = function (notes) {
  this.notes = notes;
  this.noteCards = [];
  this.lastAdded = 0;
  this.addNext(45);
};

NoteCardViewGrid.prototype.addNote = function (note) {
  this.notes.push(note);
  this.addNext(1);
};

NoteCardViewGrid.prototype.addNext = function (num) {
  var lastAdded = this.lastAdded;

  for (i = this.lastAdded; i < this.notes.length && i < num + lastAdded; i++) {
    var note = this.notes[i];

    if (note instanceof Note) {
      var noteElem = document.createElement("div");
      noteElem.classList.add("demo-card-wide");
      noteElem.classList.add("isotope-item");
      noteElem.style.width = this.width + "px";
      var noteCard = new NoteCardView(noteElem, this.onTodoListChange);
      noteCard.setNote(note);
      noteElem.note = note;
      this.noteCards.push(noteCard);
      this.elem.appendChild(noteElem);
      this.msnry.appended(noteElem);
      $(noteElem).bind('click', {
        note: note,
        callback: this.onNoteClick
      }, function (event) {
        if (!$(this).hasClass('noclick')) {
          var data = event.data;
          data.callback(data.note);
        }

        this.classList.remove("noclick");
      });
      $(noteCard.menuButton).bind('click', {
        note: note,
        callback: this.onMenuClick
      }, function (event) {
        if (!$(this).hasClass('noclick')) {
          var data = event.data;
          data.callback(data.note);
          return false;
        }
      });
    } else {
      var folderElem = document.createElement("div");
      folderElem.classList.add("demo-card-wide");
      folderElem.classList.add("isotope-item");
      folderElem.style.width = this.width + "px";
      $(folderElem).bind('click', {
        folder: note,
        callback: this.onFolderClick
      }, function (event) {
        if (!$(this).hasClass('noclick')) {
          var data = event.data;
          data.callback(data.folder);
        }
      });
      var folderCard = new FolderView(folderElem);
      folderCard.setFolder(note);
      this.elem.appendChild(folderElem);
      this.msnry.appended(folderElem);
    }

    this.lastAdded = i + 1;
  } // make all grid-items draggable


  var grid = this;
  this.msnry.layout();
  this.msnry.options.transitionDuration = "0.6s"; //restore even when discret
};

var FolderView = function FolderView(elem) {
  this.elem = elem;
  this.init();
};

FolderView.prototype.setFolder = function (folder) {
  this.folder = folder;
  this.cardTitle.innerHTML = folder.getName();
};

FolderView.prototype.init = function () {
  this.elem.classList.add("mdl-card");
  this.elem.classList.add("folder-card-view");
  this.elem.classList.add("mdl-shadow--2dp");
  this.cardContent = document.createElement('div');
  this.cardContent.classList.add("mdl-card__supporting-text");
  this.img = document.createElement('i');
  this.img.classList.add("folder-icon");
  this.img.classList.add("material-icons");
  this.img.innerHTML = "folder";
  this.cardContent.appendChild(this.img);
  this.cardTitle = document.createElement('h2');
  this.cardTitle.classList.add("card-title");
  this.cardTitle.style = "display:inline;margin-left:5px;";
  this.cardContent.appendChild(this.cardTitle);
  this.elem.appendChild(this.cardContent);
};