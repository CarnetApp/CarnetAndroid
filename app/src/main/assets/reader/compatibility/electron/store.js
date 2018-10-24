"use strict";

var ElectronStore = function ElectronStore() {
  var Store = require('electron-store');

  this.store = new Store();
};

ElectronStore.prototype.get = function (key) {
  return this.store.get(key);
};

ElectronStore.prototype.set = function (key, value) {
  this.store.set("note_cache", JSON.stringify(oldNotes));
};