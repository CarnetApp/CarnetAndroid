var KeywordDBManagerCompatibility = function(){
}
KeywordDBManagerCompatibility.KeywordsDBManager= function(path){

}
KeywordDBManagerCompatibility.KeywordsDBManager.prototype.addToDB = function(word, path){
    app.addKeyword(word, path);
}

KeywordDBManagerCompatibility.KeywordsDBManager.prototype.removeFromDB = function(word, path){
    app.removeKeyword(word, path);
}