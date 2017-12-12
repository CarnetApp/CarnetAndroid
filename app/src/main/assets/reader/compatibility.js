

var isElectron = true;
if(typeof require !== "function"){

	isElectron = false;
	var require = function(required){
		if(required == "fs"){
			return FSCompatibility;
		}
		else if(required == "mkdirp"){
		    return MKDirPCompatibility;
		}else if(required == "archiver"){
             return ArchiverCompatibility;
         } else if(required == "path")
		    return PathCompatibility;
		 else if(required == "../keywords/keywords_db_manager"){
		    return KeywordDBManagerCompatibility;
		 }
	return "";	
	}

}

var Compatibility = function(){}
Compatibility.onBackPressed = function(){
if(isElectron){
        var { ipcRenderer, remote } = require('electron');
        var main = remote.require("./main.js");
        var win = remote.getCurrentWindow();
        main.displayMainWindow(win.getSize(), win.getPosition());
        win.close()
        }

    app.onBackPressed();
}