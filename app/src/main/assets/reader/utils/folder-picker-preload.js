const { ipcRenderer } = require('electron')    
setInterval(function(){
    ipcRenderer.sendToHost('ping')
},1000)
