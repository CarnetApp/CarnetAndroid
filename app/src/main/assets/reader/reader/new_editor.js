"use strict";

var _tiptapCoreCjs = require("../node_modules/@tiptap/core/dist/tiptap-core.cjs.js");

var editor = new _tiptapCoreCjs.Editor({
  element: document.querySelector('.element'),
  extensions: [StarterKit],
  content: '<p>Hello World!</p>'
});