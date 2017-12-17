


var NoteOpenerResultReceiver = function(){};

NoteOpenerResultReceiver.callbacks = []
NoteOpenerResultReceiver.generateUID = function() {
    // I generate the UID from two parts here
    // to ensure the random number provide enough bits.
    var firstPart = (Math.random() * 46656) | 0;
    var secondPart = (Math.random() * 46656) | 0;
    firstPart = ("000" + firstPart.toString(36)).slice(-3);
    secondPart = ("000" + secondPart.toString(36)).slice(-3);
    return firstPart + secondPart;
}

NoteOpener.prototype.extractTo = function (path, callback) {
    //extract + read
    var uid = NoteOpenerResultReceiver.generateUID();
    NoteOpenerResultReceiver.callbacks[uid] = callback;    
    app.extractTo(this.note.path, path, uid)
}

NoteOpenerResultReceiver.extractResult = function (callback, error) {
    NoteOpenerResultReceiver.callbacks[callback](error);
}