var ArchiverCompatibility = function () {}
ArchiverCompatibility.create = function (type, ktkl) {
    return new Archive();
}

var Archive = function () {}

function generateUID() {
    // I generate the UID from two parts here
    // to ensure the random number provide enough bits.
    var firstPart = (Math.random() * 46656) | 0;
    var secondPart = (Math.random() * 46656) | 0;
    firstPart = ("000" + firstPart.toString(36)).slice(-3);
    secondPart = ("000" + secondPart.toString(36)).slice(-3);
    return firstPart + secondPart;
}

ArchiverCompatibility.callbacks = []

Archive.prototype.pipe = function (output) {
    this.output = output;
}
Archive.prototype.directory = function (dir) {
    this.dir = dir;
    return this;
}

Archive.prototype.finalize = function () {
    var uid = generateUID();
    console.log("close is" + this.output.close)

    ArchiverCompatibility.callbacks[uid] = this.output.close
    app.zipDir(this.dir, this.output.path, uid)
}
ArchiverCompatibility.finalizeResult = function (callback) {
    console.log(ArchiverCompatibility.callbacks[callback])
    ArchiverCompatibility.callbacks[callback]()
}