function generateUID() {
    // I generate the UID from two parts here
    // to ensure the random number provide enough bits.
    var firstPart = (Math.random() * 46656) | 0;
    var secondPart = (Math.random() * 46656) | 0;
    firstPart = ("000" + firstPart.toString(36)).slice(-3);
    secondPart = ("000" + secondPart.toString(36)).slice(-3);
    return firstPart + secondPart;
}

var FileOpener = function () {}
FileOpener.callbacks = []

FileOpener.selectFile = function (callback) {
    var uid = generateUID();
    FileOpener.callbacks[uid] = callback;
    app.selectFile(uid);
}

FileOpener.selectFileResult = function (callback, path) {
    var paths = []
    paths.push(path)
    FileOpener.callbacks[callback](paths);
}