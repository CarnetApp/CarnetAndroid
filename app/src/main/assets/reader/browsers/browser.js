var initPath = "/home/phoenamandre/QuickNote"
var currentPath;
var currentTask = undefined;
var noteCardViewGrid = undefined;
var oldNotes = {}
var TextGetterTask = function (list) {
  this.list = list;
  this.current = 0;
  this.continue = true;
}

TextGetterTask.prototype.startList = function () {
  this.getNext();
}

TextGetterTask.prototype.getNext = function () {
  if (this.current >= this.list.length)
    return;
  if (this.list[this.current] instanceof Note) {
    var opener = new NoteOpener(this.list[this.current])
    var myTask = this;
    var note = this.list[this.current]
    try{
    opener.getMainTextAndMetadata(function (txt, metadata) {
      if (myTask.continue) {
        if(txt != undefined)
          note.text = txt.substring(0, 200);
        if (metadata != undefined)
          note.metadata = metadata;
        oldNotes[note.path] = note;
        noteCardViewGrid.updateNote(note)
        noteCardViewGrid.msnry.layout();
        myTask.getNext();
      }
    });
  }
  catch(error){
    console.log(error);
  }
    this.current++;
  }
  else {
    this.current++;
    this.getNext();
  }

}

var NewNoteCreationTask = function (callback) {
  var fb = new FileBrowser(currentPath);
  console.log(currentPath + " fefef")
  var task = this;
  fb.list(function (files) {
    task.files = files;
    var name = "untitled.sqd";
    var sContinue = true;
    var i = 1;
    while (sContinue) {
      sContinue = false
      for (let file of files) {
        if (file.name == name) {
          sContinue = true;
          i++;
          name = "untitled " + i+".sqd";
        }
      }
    }
    callback(currentPath + "/" + name)

  });
}


function openNote(notePath) {
  const electron = require('electron')
  const remote = electron.remote;
  const BrowserWindow = remote.BrowserWindow;

  const path = require('path')
  //var win = new BrowserWindow({ width: 800, height: 600 });

  var rimraf = require('rimraf');
  rimraf('tmp', function () {
    var fs = require('fs');

    fs.mkdir(__dirname+"/tmp", function (e) {
      fs.createReadStream(__dirname+'/reader/reader.html').pipe(fs.createWriteStream(__dirname+'/tmp/reader.html'));
      var size = remote.getCurrentWindow().getSize();
      var pos = remote.getCurrentWindow().getPosition();
      var win = new BrowserWindow({ width: size[0], height: size[1], x: pos[0], y: pos[1], frame: false });
      // win.hide()
      console.log("w " + remote.getCurrentWindow().getPosition()[0])
      const url = require('url')
      win.loadURL(url.format({
        pathname: path.join(__dirname, 'tmp/reader.html'),
        protocol: 'file:',
        query: { 'path': notePath },
        slashes: true
      }))

    });

  })
}


function list(pathToList, discret) {
  if (pathToList == undefined)
    pathToList = currentPath;
  console.log("listing path " + pathToList);
  currentPath = pathToList;
  if (initPath == currentPath) {
    $("#back_arrow").hide()
  }
  else
    $("#back_arrow").show()
  var grid = document.getElementById("page-content");
  var scroll = 0;
  if (discret)
    scroll = document.getElementById("grid-container").scrollTop;
  grid.innerHTML = "";
  noteCardViewGrid = new NoteCardViewGrid(grid, discret);

  noteCardViewGrid.onFolderClick(function (folder) {
    list(folder.path)
  })
  noteCardViewGrid.onNoteClick(function (note) {

    openNote(note.path)
    // var reader = new Writer(note,"");
    // reader.extractNote()
  })
  var notes = [];

  var fb = new FileBrowser(pathToList);
  fb.list(function (files) {
    if (currentTask != undefined)
      currentTask.continue = false
    for (let file of files) {
      var filename = getFilenameFromPath(file.path);
      if (file.isFile && filename.endsWith(".sqd")) {
        var oldNote = oldNotes[file.path];

        var noteTestTxt = new Note(stripExtensionFromName(filename), oldNote != undefined ? oldNote.text : "", file.path, oldNote != undefined ? oldNote.metadata : undefined);
        notes.push(noteTestTxt)
      }
      else if (!file.isFile) {

        notes.push(file)
      }
    }
    noteCardViewGrid.setNotesAndFolders(notes)
    if (discret) {
      document.getElementById("grid-container").scrollTop = scroll;
      console.log("scroll : " + scroll)

    }
    currentTask = new TextGetterTask(notes);
    console.log("stopping and starting task")
    currentTask.startList();

  });

}
list(initPath)
document.getElementById("add-note-button").onclick = function () {
  new NewNoteCreationTask(function (path) {
    console.log("found " + path)
    openNote(path)
  })
}

document.getElementById("back_arrow").addEventListener("click", function () {
    list(getParentFolderFromPath(currentPath))
});
$(window).focus(function () {
  list(currentPath, true)
});