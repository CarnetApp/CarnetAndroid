var FSCompatibility = function () {

}

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
FSCompatibility.readFile = function (path, encoding, callback) {
    if (typeof encoding === 'function') {
        callback = encoding;
        encoding = undefined;
    }
    console.log("readfile comatibility");
    var uid = generateUID();
    callbacks[uid] = callback;
    app.readFile(uid, path);

}

FSCompatibility.readdir = function (path, callback) {
    var uid = generateUID();
    callbacks[uid] = callback;
    app.readdir(path, uid)
}

FSCompatibility.resultReaddir = function (callback, err, data) {
    console.log("result dir " + data)
    callbacks[callback](err, JSON.parse(data)['data']);
}

FSCompatibility.resultFileRead = function (callback, error, content) {
    console.log("resultFileRead comatibility ok " + content);
    callbacks[callback](error, content);

}

FSCompatibility.unlink = function (path, callback) {
    var uid = generateUID();
    callbacks[uid] = callback;
    app.unlink(path, uid);
}
FSCompatibility.unlinkResult = function (callback, content) {
    callbacks[callback]();

}

FSCompatibility.writeFileResult = function (callback, content) {
    callbacks[callback]();

}

FSCompatibility.writeFile = function (path, content, encoding, callback) {
    if (typeof encoding === 'function') {
        callback = encoding;
        encoding = undefined;
    }
    if (encoding == undefined)
        encoding = "utf8";
    var uid = generateUID();
    callbacks[uid] = callback;
    app.writeFile(path, content, uid, encoding);
}

FSCompatibility.writeFileSync = function (path, content, encoding) {
    if (encoding == undefined)
        encoding = "utf8";
    app.writeFileSync(path, content, encoding);
}

FSCompatibility.createWriteStream = function (path) {
    var wr = new WriteStream()
    wr.path = path
    return wr;
}

var WriteStream = function () {

}
WriteStream.prototype.on = function (action, callback) {
    this.close = callback
}