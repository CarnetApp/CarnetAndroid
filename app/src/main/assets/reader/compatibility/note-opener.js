function generateUID() {
    // I generate the UID from two parts here
    // to ensure the random number provide enough bits.
    var firstPart = (Math.random() * 46656) | 0;
    var secondPart = (Math.random() * 46656) | 0;
    firstPart = ("000" + firstPart.toString(36)).slice(-3);
    secondPart = ("000" + secondPart.toString(36)).slice(-3);
    return firstPart + secondPart;
}

var callbacks = []

var NoteOpenerResultReceiver = function(){};


NoteOpener.prototype.extractTo = function (path, callback) {
    //extract + read

    var uid = generateUID();
    callbacks[uid] = callback;
    this.note.metadata = []
    app.extractTo(this.note.path, path, uid)
}

NoteOpenerResultReceiver.extractResult = function (callback, error) {
    callbacks[callback](error);
}