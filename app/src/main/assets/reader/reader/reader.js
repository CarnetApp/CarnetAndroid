var fs = require('fs');

var Writer = function (elem) {
    this.elem = elem;
    this.seriesTaskExecutor = new SeriesTaskExecutor();
    this.saveNoteTask = new SaveNoteTask(this)
    resetScreenHeight();
    console.log("create Writer")

}

Writer.prototype.setNote = function (note) {
    this.note = note;
    this.noteOpener = new NoteOpener(note);

}

Writer.prototype.displayMediaFullscreen = function (index) {
    var img = document.createElement("img")
    img.src = this.fullscreenableMedia[index];

    $(img).on('load', function () {
        img.style.marginTop = "-" + $(this).height() / 2 + "px"
        img.style.marginLeft = "-" + $(this).width() / 2 + "px"

        console.log(img.height)
    });

    img.style.top = "50%"
    img.style.left = "50%"

    img.style.position = "absolute"
    this.fullscreenViewer.innerHTML = "";
    this.fullscreenViewer.appendChild(img)
    $(this.fullscreenViewer).fadeIn("slow");
    this.fullscreenViewer.style.display = "table-cell"
    this.currentFullscreen = index;
}

Writer.prototype.previousMedia = function () {
    if (this.currentFullscreen > 0)
        this.displayMediaFullscreen(this.currentFullscreen - 1);
}
Writer.prototype.nextMedia = function () {
    if (this.currentFullscreen < this.fullscreenableMedia.length - 1)
        this.displayMediaFullscreen(this.currentFullscreen + 1);
}
Writer.prototype.refreshMedia = function () {
    var writer = this;
    writer.mediaList.innerHTML = "";

    fs.readdir('tmp/data/', (err, dir) => {
        if (err) {
            throw err
        }
        writer.currentFullscreen = 0;
        writer.fullscreenableMedia = []
        var mediaCount = 0;
        for (let filePath of dir) {
            console.log("file " + filePath)
            var el = document.createElement("div")
            el.classList.add("media")
            if (FileUtils.isFileImage(filePath)) {
                var img = document.createElement("img")
                img.src = "data/" + filePath
                el.appendChild(img)
                writer.fullscreenableMedia.push("data/" + filePath)

                img.mediaIndex = mediaCount;
                el.onclick = function (event) {
                    console.log(event.target)
                    writer.displayMediaFullscreen(event.target.mediaIndex)

                }
                mediaCount++;
            } else {
                var img = document.createElement("img")
                img.src = rootpath + "/img/file.svg"
                el.appendChild(img)
                el.innerHTML += "<br /> " + filePath
                el.classList.add("media-file")
            }
            writer.mediaList.appendChild(el)
        }
    })
}

Writer.prototype.addMedia = function () {
    console.log("add media")
    var writer = this;
    FileOpener.selectFile(function (fileNames) {

        if (fileNames === undefined) return;

        var filePath = fileNames[0];
        console.log("file " + filePath)
        fs.readFile(filePath, 'base64', function read(err, data) {
            if (err) {
                throw err;
            }
            //filename
            require('mkdirp').sync('tmp/data/');
            var name = FileUtils.getFilename(filePath)
            fs.writeFile('tmp/data/' + name, data, 'base64', function (err) {
                if (!err) {
                    writer.seriesTaskExecutor.addTask(writer.saveNoteTask.saveTxt)
                    writer.refreshMedia();
                }
            })

        })


    })

}

Writer.prototype.extractNote = function () {
    console.log("Writer.prototype.extractNote")

    var writer = this;
    console.log("extractNote")
    writer.noteOpener.extractTo("tmp/", function (noSuchFile) {
        console.log("done")
        if (!noSuchFile) {
            var fs = require('fs');
            fs.readFile('tmp/index.html', 'base64', function read(err, data) {
                if (err) {
                    throw err;
                }
                fs.readFile('tmp/metadata.json', 'base64', function read(err, metadata) {
                    if (err) {
                        throw err;
                    }
                    writer.note.metadata = JSON.parse(decodeURIComponent(escape(atob(metadata))));
                    writer.refreshKeywords()
                    writer.refreshMedia()
                });
                content = data;
                writer.fillWriter(decodeURIComponent(escape(atob(content))))
            });
        } else {
            writer.fillWriter(undefined)
        }
        /*fs.readFile('tmp/metadata.json', function read(err, data) {
            if (err) {
                throw err;
            }
            
            content = data;
            console.log(data)
            this.note.metadata = JSON.parse(content)
        });*/
        //copying reader.html
    })
}



Writer.prototype.fillWriter = function (extractedHTML) {
    if (extractedHTML != undefined)
        this.oEditor.innerHTML = extractedHTML;
    this.oDoc = document.getElementById("text");
    this.oFloating = document.getElementById("floating");
    var writer = this
    this.oDoc.addEventListener("input", function () {
        writer.seriesTaskExecutor.addTask(writer.saveNoteTask.saveTxt)
    }, false);
    this.sDefTxt = this.oDoc.innerHTML;
    /*simple initialization*/
    this.oDoc.focus();
    if (typeof app == 'object')
        app.hideProgress();
    resetScreenHeight();
    this.refreshKeywords();
    //  $("#editor").webkitimageresize().webkittableresize().webkittdresize();

}

Writer.prototype.refreshKeywords = function () {
    var keywordsContainer = document.getElementById("keywords-list");
    keywordsContainer.innerHTML = "";
    var writer = this;
    for (let word of this.note.metadata.keywords) {
        var keywordElem = document.createElement("a")
        keywordElem.classList.add("mdl-navigation__link");
        keywordElem.innerHTML = word;
        keywordsContainer.appendChild(keywordElem);
        keywordElem.addEventListener('click', function () {
            writer.removeKeyword(word);
        });

    }
}

Writer.prototype.formatDoc = function (sCmd, sValue) {
    this.oEditor.focus();
    document.execCommand(sCmd, false, sValue);
    this.oEditor.focus();
}

Writer.prototype.displayTextColorPicker = function () {
    var writer = this;
    this.displayColorPicker(function (color) {
        writer.setColor(color)
    });
}

Writer.prototype.displayFillColorPicker = function () {
    var writer = this;
    this.displayColorPicker(function (color) {
        writer.fillColor(color)
    });
}
var currentColor = undefined;
Writer.prototype.setPickerColor = function (picker) {
    currentColor = "#" + picker.toString();
}
Writer.prototype.displayColorPicker = function (callback) {
    currentColorCallback = callback;
    this.colorPickerDialog.querySelector('.ok').addEventListener('click', function () {
        writer.colorPickerDialog.close();
        callback(currentColor);
    });
    this.colorPickerDialog.showModal()
    document.getElementById('color-picker-div').show();
}
Writer.prototype.init = function () {
    document.execCommand('styleWithCSS', false, true);
    var writer = this;
    this.statsDialog = this.elem.querySelector('#statsdialog');
    this.showDialogButton = this.elem.querySelector('#show-dialog');
    if (!this.statsDialog.showModal) {
        dialogPolyfill.registerDialog(this.statsDialog);
    }

    this.statsDialog.querySelector('.ok').addEventListener('click', function () {
        writer.statsDialog.close();

    });

    this.colorPickerDialog = this.elem.querySelector('#color-picker-dialog');
    if (!this.colorPickerDialog.showModal) {
        dialogPolyfill.registerDialog(this.colorPickerDialog);
    }


    this.newKeywordDialog = this.elem.querySelector('#new-keyword-dialog');
    if (!this.newKeywordDialog.showModal) {
        dialogPolyfill.registerDialog(this.newKeywordDialog);
    }

    this.oEditor = document.getElementById("editor");

    this.backArrow = document.getElementById("back-arrow");
    this.mediaList = document.getElementById("media-list");
    this.fullscreenViewer = document.getElementById("fullscreen-viewer");
    $(document).bind('keydown', function (event) {
        console.log(event.keyCode);
        if ($(writer.fullscreenViewer).is(":visible")) {
            switch (event.keyCode) {
                case 37:
                    writer.previousMedia()
                    break;
                case 39:
                    writer.nextMedia()
                    break;
                case 27:
                    $(writer.fullscreenViewer).hide("slow")
                    break;
            }
        }
    });
    this.fullscreenViewer.onclick = function () {
        $(writer.fullscreenViewer).hide("slow")
    }
    this.backArrow.addEventListener("click", function () {
        Compatibility.onBackPressed();
    });
    this.toolbarManager = new ToolbarManager()
    var toolbarManager = this.toolbarManager
    for (var toolbar of document.getElementsByClassName("toolbar")) {
        this.toolbarManager.addToolbar(toolbar);
    };
    for (var toolbar of document.getElementsByClassName("toolbar-button")) {
        console.log("tool " + toolbar.getAttribute("for"))

        toolbar.addEventListener("click", function (event) {
            console.log("display " + event.target.getAttribute("for"))
            toolbarManager.toggleToolbar(document.getElementById(event.target.getAttribute("for")))
        });
    };
    this.searchInput = document.getElementById("search-input");
    this.searchInput.onfocus = function(){
        var el = document.getElementById('container-button');
        console.log('test')
        $(el).animate({
            scrollLeft: el.scrollLeft+300
        }, 200);
    }
    this.searchInput.onkeyup = function(event){
        if(event.key === 'Enter') {
            window.find(this.value);
        }
    }

    // $("#editor").webkitimageresize().webkittableresize().webkittdresize();
}

Writer.prototype.copy = function () {
    document.execCommand('copy');
}

Writer.prototype.paste = function () {
    document.execCommand('paste');
}

Writer.prototype.displayCountDialog = function () {
    var nouveauDiv;
    if (window.getSelection().toString().length == 0) {
        nouveauDiv = this.oDoc;

    } else {
        nouveauDiv = document.createElement("div");
        nouveauDiv.innerHTML = window.getSelection();
    }
    console.log(" is defined ? " + nouveauDiv)

    var writer = this
    Countable.once(nouveauDiv, function (counter) {
        writer.statsDialog.querySelector('.words_count').innerHTML = counter.words;
        writer.statsDialog.querySelector('.characters_count').innerHTML = counter.characters;
        writer.statsDialog.querySelector('.sentences_count').innerHTML = counter.sentences;
        writer.statsDialog.showModal();
    });

}




Writer.prototype.increaseFontSize = function () {
    this.surroundSelection(document.createElement('big'));
}
Writer.prototype.decreaseFontSize = function () {
    this.surroundSelection(document.createElement('small'));
}
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
}
var KeywordsDBManager = require("../keywords/keywords_db_manager").KeywordsDBManager;
var keywordsDBManager = new KeywordsDBManager()
Writer.prototype.addKeyword = function (word) {
    if (this.note.metadata.keywords.indexOf(word) < 0 && word.length > 0) {
        this.note.metadata.keywords.push(word);
        keywordsDBManager.addToDB(word, this.note.path)
        this.seriesTaskExecutor.addTask(this.saveNoteTask.saveTxt)
        this.refreshKeywords();
    }
}

Writer.prototype.removeKeyword = function (word) {
    if (this.note.metadata.keywords.indexOf(word) >= 0) {
        this.note.metadata.keywords.splice(this.note.metadata.keywords.indexOf(word), 1);
        keywordsDBManager.removeFromDB(word, this.note.path)
        this.seriesTaskExecutor.addTask(this.saveNoteTask.saveTxt)
        this.refreshKeywords();
    }
}

Writer.prototype.reset = function () {
    this.oEditor.innerHTML = '<div id="text" contenteditable="true" style="height:100%;">\
    <!-- be aware that THIS will be modified in java -->\
    <!-- soft won\'t save note if contains donotsave345oL -->\
</div>\
<div id="floating">\
\
</div>';
}

Writer.prototype.setColor = function (color) {
    document.execCommand('styleWithCSS', false, true);
    document.execCommand('foreColor', false, color);
}

Writer.prototype.fillColor = function (color) {
    document.execCommand('backColor', false, color);
}

var ToolbarManager = function () {
    this.toolbars = [];
}
ToolbarManager.prototype.addToolbar = function (elem) {
    this.toolbars.push(elem)
    $(elem).hide()
}

ToolbarManager.prototype.toggleToolbar = function (elem) {
    for (let toolbar of this.toolbars) {
        if (toolbar != elem)
            $(toolbar).hide()
    }
    $(elem).show()

    resetScreenHeight()
}


var SeriesTaskExecutor = function () {
    this.task = []
    this.isExecuting = false
}

SeriesTaskExecutor.prototype.addTask = function (task) {
    this.task.push(task)
    console.log("push " + this.isExecuting)

    if (!this.isExecuting) {
        this.execNext()
    }

}

SeriesTaskExecutor.prototype.execNext = function () {
    this.isExecuting = true
    console.log("exec next ")
    if (this.task == undefined)
        this.task = []
    if (this.task.length == 0) {
        this.isExecuting = false;
        return;
    }
    var executor = this;
    this.task.shift()(function () {
        executor.execNext()
    })
    console.log("this.task length " + this.task.length)

}

var SaveNoteTask = function (writer) {
    this.writer = writer;

}

SaveNoteTask.prototype.saveTxt = function (onEnd) {

    var fs = require('fs');
    var writer = this.writer;
    console.log("saving")
    fs.unlink("tmp/reader.html", function () {
        fs.writeFile('tmp/index.html', writer.oEditor.innerHTML, function (err) {
            if (err) {
                onEnd()
                return console.log(err);
            }
            writer.note.metadata.last_modification_date = Date.now();
            console.log("saving meta  " + writer.note.metadata.keywords[0])
            fs.writeFile('tmp/metadata.json', JSON.stringify(writer.note.metadata), function (err) {
                if (err) {
                    onEnd()
                    return console.log(err);
                }
                console.log("compress")
                writer.noteOpener.compressFrom("tmp", function () {
                    console.log("compressed")

                    onEnd()
                })
            });

        });


    })
}