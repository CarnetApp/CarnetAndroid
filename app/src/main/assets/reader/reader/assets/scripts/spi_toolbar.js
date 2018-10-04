'use strict';

var SpiToolbarProto = Object.create(HTMLDivElement.prototype);
SpiToolbarProto.prototype = HTMLDivElement.prototype;
SpiToolbarProto.hello = function () {
    alert('Hello!');
};
var SpiToolbarElement = document.registerElement('spi-toolbar', {
    prototype: SpiToolbarProto,
    extends: "div"
});