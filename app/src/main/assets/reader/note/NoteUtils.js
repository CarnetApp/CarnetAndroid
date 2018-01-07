var NoteUtils = function() {

}
NoteUtils.getNoteRelativePath = function(rootPath, notePath) {
    return notePath.substring(rootPath.length+1)
}
NoteUtils.deleteNote = function(notePath, callback){
    console.log("delete "+notePath)
    var fs = require('fs');
    fs.unlink(notePath, function(){
        var db = new RecentDBManager(main.getNotePath() + "/quickdoc/recentdb/" + main.getAppUid())
        db.removeFromDB(NoteUtils.getNoteRelativePath(main.getNotePath(), notePath), callback);
    })
}

NoteUtils.moveNote = function(notePath, callback){
   
}

NoteUtils.renameNote = function(notePath, newTitle, callback){
    var newPath = getParentFolderFromPath(notePath)+"/"+newTitle;
    console.log("renameNote "+newPath)
    var fs = require('fs');
    if (!fs.exists(newPath)) {
        fs.rename(notePath, newPath, function (err) {
            console.log(err)
            if(err)
                return;
            var db = new RecentDBManager(main.getNotePath() + "/quickdoc/recentdb/" + main.getAppUid())
            db.move(NoteUtils.getNoteRelativePath(main.getNotePath(), notePath), NoteUtils.getNoteRelativePath(main.getNotePath(), newPath), function(){
                callback();
            })
          
        })
    }
}

exports.NoteUtils = NoteUtils