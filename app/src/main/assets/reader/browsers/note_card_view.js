"use strict";

var NoteCardView = function NoteCardView(elem) {
  this.elem = elem;
  this.init();
};

NoteCardView.prototype.setNote = function (note) {
  if (this.oldColor != undefined) {
    this.elem.classList.remove(this.oldColor);
  }

  this.note = note;

  if (this.note.metadata != undefined && this.note.metadata.color != undefined) {
    this.elem.classList.add(this.note.metadata.color);
    this.oldColor = this.note.metadata.color;
  }

  if (note.title.indexOf("untitled") == 0) this.cardTitleText.innerHTML = "";else this.cardTitleText.innerHTML = note.title;
  var date = new Date(note.metadata.last_modification_date).toLocaleDateString();
  this.cardText.innerHTML = note.text;
  this.cardDate.innerHTML = date;
  if (note.metadata.rating > 0) this.cardRating.innerHTML = note.metadata.rating + "★";
  this.cardKeywords.innerHTML = "";
  this.cardText.classList.remove("big-text");
  this.cardText.classList.remove("medium-text");
  if (note.text.length < 40 && this.cardTitleText.innerHTML == "") this.cardText.classList.add("big-text");else if (note.text.length < 100 && this.cardTitleText.innerHTML == "") {
    this.cardText.classList.add("medium-text");
  }

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
        if (!_iteratorNormalCompletion && _iterator.return != null) {
          _iterator.return();
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
        if (!_iteratorNormalCompletion2 && _iterator2.return != null) {
          _iterator2.return();
        }
      } finally {
        if (_didIteratorError2) {
          throw _iteratorError2;
        }
      }
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
  this.cardTitleText = document.createElement('h2');
  this.cardTitleText.classList.add("card-title");
  this.cardContent.appendChild(this.cardTitleText);
  this.cardContent.appendChild(this.cardText);
  this.cardRating = document.createElement('div');
  this.cardRating.classList.add("card-rating");
  this.cardContent.appendChild(this.cardRating);
  this.cardDate = document.createElement('div');
  this.cardDate.classList.add("card-date");
  this.cardContent.appendChild(this.cardDate);
  this.cardKeywords = document.createElement('div');
  this.cardKeywords.classList.add("keywords");
  this.cardContent.appendChild(this.cardKeywords);
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
  this.notes = []; //calculating card width

  this.width = 200;

  if (document.body.clientWidth / 2 - 10 < 200) {
    if (document.body.clientWidth > 300) this.width = document.body.clientWidth / 2 - 13;else this.width = document.body.clientWidth - 10;
  }

  console.log("width " + document.body.clientWidth);
  var Masonry = compatibility.getMasonry();
  this.msnry = new Masonry(this.elem, {
    // options
    itemSelector: '.demo-card-wide.mdl-card',
    fitWidth: true,
    columnWidth: this.width + 10,
    transitionDuration: this.discret ? 0 : "0.6s",
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
      var noteCard = new NoteCardView(noteElem);
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
  this.img = document.createElement('img');
  this.img.classList.add("folder-icon");
  this.img.src = root_url + "img/directory.png";
  this.cardContent.appendChild(this.img);
  this.cardTitle = document.createElement('h2');
  this.cardTitle.classList.add("card-title");
  this.cardTitle.style = "display:inline;margin-left:5px;";
  this.cardContent.appendChild(this.cardTitle);
  this.elem.appendChild(this.cardContent);
};