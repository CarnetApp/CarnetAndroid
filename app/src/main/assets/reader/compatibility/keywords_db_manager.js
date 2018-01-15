var KeywordDBManagerCompatibility = function(){
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

KeywordDBManagerCompatibility.callbacks = []
KeywordDBManagerCompatibility.KeywordsDBManager= function(path){

}
KeywordDBManagerCompatibility.KeywordsDBManager.prototype.addToDB = function(word, path){
    app.addKeyword(word, path);
}

KeywordDBManagerCompatibility.KeywordsDBManager.prototype.removeFromDB = function(word, path){
    app.removeKeyword(word, path);
}

KeywordDBManagerCompatibility.KeywordsDBManager.prototype.getFlatenDB = function(callback){
    var uid = generateUID();
    KeywordDBManagerCompatibility.callbacks[uid] = callback
    app.getFlatenKeywordsDB(uid)
}

KeywordDBManagerCompatibility.getFlatenDBResult = function(callback, dataStr){
    var data = JSON.parse(dataStr)
    KeywordDBManagerCompatibility.callbacks[callback](false,data)
}