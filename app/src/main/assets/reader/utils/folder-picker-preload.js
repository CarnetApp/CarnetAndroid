"use strict";

var _require = require('electron'),
    ipcRenderer = _require.ipcRenderer;

setInterval(function () {
  ipcRenderer.sendToHost('ping');
}, 1000);