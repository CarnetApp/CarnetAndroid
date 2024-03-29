/**
 * http://plugin.bearsthemes.com/jquery/MasonryHybrid/jquery.masonry-hybrid.min.js
 * Create Date: 31-08-2016
 * Version: 1.0.0
 * Author: Bearsthemes
 */
"use strict";

String.prototype.replaceMap = function (a) {
  var c,
    b = this;
  for (c in a) b = b.replace(new RegExp("\\{" + c + "\\}", "gm"), a[c]);
  return b;
}, $.fn.stripClass = function (a, b) {
  var c = new RegExp((b ? "\\S+" : "\\b") + a + "\\S*", "g");
  return this.attr("class", function (a, b) {
    if (b) return b.replace(c, "");
  }), this;
};
var MasonryHybrid = function MasonryHybrid(a, b) {
  return this.elem = a, this.opts = $.extend({
    itemSelector: ".grid-item",
    columnWidth: ".grid-sizer",
    gutter: ".gutter-sizer",
    col: 4,
    space: 20,
    percentPosition: !1
  }, b), this.init(), this;
};
MasonryHybrid.prototype = {
  init: function init() {
    var a = this;
    a.applySelectorClass(), a.renderStyle(), a.applyMasonry(), a.triggerEvent(), $(window).on("load", function () {
      a.elem.trigger("grid:refresh");
    });
  },
  applySelectorClass: function applySelectorClass() {
    this.elemClass = "masonry_hybrid-" + Math.random().toString(36).replace(/[^a-z]+/g, "").substr(0, 9), this.elem.addClass(this.elemClass);
  },
  renderStyle: function renderStyle() {
    var a = this,
      b = "";
    a.style = $("<style>"), b += " .{elemClass} { margin-left: -{space}px; width: calc(100% + {space}px); transition-property: height, width; }", b += " .{elemClass} {itemSelector}, .{elemClass} {columnWidth} { width: calc(100% / {col}); }", b += " .{elemClass} {gutter} { width: 0; }", b += " .{elemClass} {itemSelector} { float: left; box-sizing: border-box; padding-left: {space}px; padding-bottom: {space}px; }", b += " .{elemClass} {itemSelector}.ui-resizable-resizing { z-index: 999 }", b += " .{elemClass} {itemSelector} .screen-size{ visibility: hidden; transition: .5s; -webkit-transition: .5s; opacity: 0; position: absolute; bottom: calc({space}px + 8px); right: 9px; padding: 2px 4px; border-radius: 2px; font-size: 11px; }", b += " .{elemClass} {itemSelector}.ui-resizable-resizing .screen-size{ visibility: visible; opacity: 1; }", b += " .{elemClass} {itemSelector} .ui-resizable-se { right: 0; bottom: {space}px; opacity: 0; }", b += " .{elemClass} {itemSelector}:hover .ui-resizable-se { opacity: 1; }";
    for (var c = 1; c <= a.opts.col; c++) {
      var d = 100 / a.opts.col * c;
      b += " .{elemClass} .grid-item--width" + c + " { width: " + d + "% }";
    }
    b = b.replaceMap({
      elemClass: a.elemClass,
      itemSelector: a.opts.itemSelector,
      gutter: a.opts.gutter,
      columnWidth: a.opts.columnWidth,
      space: a.opts.space,
      col: a.opts.col
    }), a.elem.prepend(a.style.html(b));
  },
  clearStyle: function clearStyle() {
    return this.style.remove(), this;
  },
  applyMasonry: function applyMasonry() {
    var a = this;
    this.grid = a.elem.isotope({
      itemSelector: a.opts.itemSelector,
      percentPosition: a.opts.percentPosition,
      masonry: {
        columnWidth: a.opts.columnWidth,
        gutter: a.opts.gutter
      }
    });
  },
  triggerEvent: function triggerEvent() {
    var a = this;
    a.elem.on({
      "grid:refresh": function gridRefresh(b, c) {
        c && (a.opts = $.extend(a.opts, c), a.clearStyle().renderStyle()), a.grid.isotope("layout").delay(500).queue(function () {
          a.grid.isotope("layout"), $(this).dequeue();
        });
      }
    });
  }
}, MasonryHybrid.prototype.resize = function (a) {
  var b = this;
  return b._resize = {}, b._resize.opts = $.extend({
    celHeight: 140,
    sizeMap: [[1, 1]],
    resize: !1
  }, a), b._resize.applySize = function () {
    for (var a = b.elem.find(b.opts.itemSelector).length, c = b._resize.opts.sizeMap.length, d = 0, e = 0; d <= a; d++) {
      var f = b._resize.opts.sizeMap[e][0],
        g = b._resize.opts.celHeight * b._resize.opts.sizeMap[e][1];
      b.elem.find(b.opts.itemSelector).eq(d).data("grid-size", [b._resize.opts.sizeMap[e][0], b._resize.opts.sizeMap[e][1]]).stripClass("grid-item--width").addClass("grid-item--width" + f).css({
        height: g
      }), e++, e == c && (e = 0);
    }
    b.elem.trigger("grid:refresh");
  }, b._resize.applySize(), b._resize.getSizeMap = function () {
    for (var a = b.elem.find(b.opts.itemSelector).length, c = [], d = 0; d <= a - 1; d++) {
      var e = b.elem.find(b.opts.itemSelector).eq(d),
        f = e.data("grid-size");
      c.push([f[0], f[1]]);
    }
    return c;
  }, b._resize.setSizeMap = function (a) {
    if (a) return b._resize.opts.sizeMap = a, this;
  }, b._resize.resizeHandle = function () {
    0 != b._resize.opts.resize && b.elem.find(b.opts.itemSelector).resizable({
      handles: "se",
      start: function start() {
        $(this).find(".screen-size").length <= 0 ? (this.screenSize = $("<span>", {
          "class": "screen-size"
        }), $(this).append(this.screenSize)) : this.screenSize = $(this).find(".screen-size");
      },
      resize: function resize(a, c) {
        c.size.width = c.size.width + b.opts.space, c.size.height = c.size.height + b.opts.space;
        var e = (this.getBoundingClientRect(), b.elem.width()),
          f = parseInt(e / 100 * (100 / b.opts.col));
        this.step_w = Math.round(c.size.width / f), this.step_h = Math.round(c.size.height / b._resize.opts.celHeight), this.step_w <= 0 && (this.step_w = 1), this.step_h <= 0 && (this.step_h = 1), this.screenSize.html(this.step_w + " x " + this.step_h);
      },
      stop: function stop(a, c) {
        $(this).css({
          width: "",
          height: ""
        }).data("grid-size", [this.step_w, this.step_h]), b._resize.opts.sizeMap = b._resize.getSizeMap(), b._resize.applySize();
      }
    });
  }, b._resize.resizeHandle(), b._resize;
};