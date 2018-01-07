var fs = require("fs");
var getParentFolderFromPath = require('path').dirname;

var FolderPicker = function(){
    this.elem =  document.getElementById("table-container");
    this.progressView = document.getElementById("progress-view");
    const { ipcRenderer } = require('electron')    
    var picker = this
    for(var elem of document.getElementsByClassName("import-button")){
        elem.onclick=function(){
            ipcRenderer.sendToHost('pathSelected',picker.path)
        }
    }
}

FolderPicker.prototype.listPath = function(path){
    console.log("list "+path)
    this.path = path;
    this.elem.innerHTML = "";
    $(this.progressView).show();
    for(var elem of document.getElementsByClassName("import-button")){
        $(elem).hide();
    }
    var table = document.createElement("table");
    table.classList.add("mdl-data-table")
    table.classList.add("mdl-js-data-table")
    table.classList.add("mdl-shadow--2dp")
    
    var tbody = document.createElement("tbody");
    table.appendChild(tbody)
    this.elem.appendChild(table)
    folderPicker = this;
    var tr = document.createElement("tr");
    tbody.appendChild(tr)
    
    var td = document.createElement("td");
    td.classList.add("mdl-data-table__cell--non-numeric")
    tr.appendChild(td)
    td.innerHTML = "Parent folder"
    td.path = getParentFolderFromPath(path)
    td.onclick = function(elem){
        console.log(this.path)
        folderPicker.listPath(this.path)
    }
    fs.readdir(path, (err, dir) => {
        console.log(err);
  
        for (let filename of dir) {
            if(filename.startsWith("."))
            continue;
            var filePath = path +"/"+ filename
            var stat = fs.statSync(filePath);
            if(stat.isFile())
                continue;
            var tr = document.createElement("tr");
            tbody.appendChild(tr)
            
            var td = document.createElement("td");
            td.classList.add("mdl-data-table__cell--non-numeric")
            tr.appendChild(td)
            var img = document.createElement("img");
            td.appendChild(img)
            img.src="../img/directory.png";
            img.classList.add("icon")
            var span = document.createElement("span");
            td.appendChild(span)
            span.innerHTML = filename
            td.path = filePath
            td.onclick = function(elem){
                console.log(this.path)
                folderPicker.listPath(this.path)
            }
           
            
           
        }
        $(folderPicker.progressView).hide()
        for(var elem of document.getElementsByClassName("import-button")){
            $(elem).show();
        }
    });
}
