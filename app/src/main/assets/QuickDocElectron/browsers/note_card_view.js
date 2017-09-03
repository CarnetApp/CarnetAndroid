var NoteCardView = function(elem){
    this.elem = elem;
    this.init();
}
NoteCardView.prototype.setNote = function(note){
    this.cardTitleText.innerHTML = note.title;
    this.cardContent.innerHTML = note.text;
}

NoteCardView.prototype.init = function(){
    this.elem.classList.add("mdl-card");
    this.elem.classList.add("mdl-shadow--2dp");
    this.cardTitle = document.createElement('div');
    this.cardTitle.classList.add("mdl-card__title");
    this.elem.appendChild(this.cardTitle);
    this.cardTitleText = document.createElement('h2');
    this.cardTitleText.classList.add("mdl-card__title-text");
    this.cardTitle.appendChild(this.cardTitleText);
    this.cardContent = document.createElement('div');
    this.cardContent.classList.add("mdl-card__supporting-text");
    this.elem.appendChild(this.cardContent);
    
}

var Isotope = require('isotope-layout');
var NoteCardViewGrid = function (elem){

    this.elem = elem;
    this.init();
}




NoteCardViewGrid.prototype.init = function(){
    this.elem.classList.add("isotope")
    this.iso = new Isotope( this.elem, {
        percentPosition: true,
        itemSelector: '.demo-card-wide.mdl-card',
        masonry: {
          // use outer width of grid-sizer for columnWidth
        }
      });    
      
}

NoteCardViewGrid.prototype.onFolderClick = function(callback){
    this.onFolderClick = callback;
}

NoteCardViewGrid.prototype.setNotesAndFolders = function (notes){
    this.notes = notes;
    for(i = 0; i < notes.length; i++){
        var note = notes[i]
        if(note instanceof Note){
        var noteElem = document.createElement("div");
        noteElem.classList.add("demo-card-wide")
        noteElem.classList.add("isotope-item")
        var noteCard = new NoteCardView(noteElem);
        noteCard.setNote(note);
        this.elem.appendChild(noteElem)
        this.iso.appended(noteElem)
        }
        else{
            var folderElem = document.createElement("div");
            folderElem.classList.add("demo-card-wide")
            folderElem.classList.add("isotope-item")

            $(folderElem).bind('click', { folder: note, callback: this.onFolderClick }, function(event) {
                var data = event.data;
                data.callback(data.folder)
            });
            
           
            var folderCard = new FolderView(folderElem);
            folderCard.setFolder(note);
            this.elem.appendChild(folderElem)
            this.iso.appended(folderElem)
        }
    }
    this.iso.layout();
}



var FolderView = function(elem){
    this.elem = elem;
    this.init();
}
FolderView.prototype.setFolder = function(folder){
    this.folder = folder;
    this.cardTitle.innerHTML = folder.getName();
}

FolderView.prototype.init = function(){
    this.elem.classList.add("mdl-card");
    this.elem.classList.add("mdl-shadow--2dp");
    this.cardContent = document.createElement('div');
    this.cardContent.classList.add("mdl-card__supporting-text");
    this.cardTitle = document.createElement('h2');
    this.cardTitle.classList.add("card-title");
    this.cardContent.appendChild(this.cardTitle);
    this.elem.appendChild(this.cardContent);
    
}