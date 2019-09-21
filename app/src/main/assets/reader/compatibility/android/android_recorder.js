"use strict";

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var AndroidRecorder =
/*#__PURE__*/
function () {
  function AndroidRecorder(options) {
    _classCallCheck(this, AndroidRecorder);

    AndroidRecorder.instance = this;
    this.options = options;
    this.state = "none";
  }

  _createClass(AndroidRecorder, [{
    key: "setState",
    value: function setState(state) {
      this.state = state;
    }
  }, {
    key: "start",
    value: function start() {
      console.log("starting compat recorder");
      this.state = "recording";
      AndroidRecorderJava.start(this.options.channels, this.options.encoderBitRate, this.options.encoderSampleRate);
    }
  }, {
    key: "stop",
    value: function stop() {
      this.state = "none";
      AndroidRecorderJava.stop();
    }
  }, {
    key: "pause",
    value: function pause() {
      this.state = "paused";
      AndroidRecorderJava.pause();
    }
  }, {
    key: "resume",
    value: function resume() {
      this.state = "recording";
      AndroidRecorderJava.resume();
    }
  }, {
    key: "onFileReady",
    value: function onFileReady(url) {
      this.ondataavailable(undefined, url);
    }
  }, {
    key: "onEncodingStart",
    value: function onEncodingStart() {
      writer.recorder.onEncodingStart();
    }
  }, {
    key: "onEncodingEnd",
    value: function onEncodingEnd() {
      writer.recorder.onEncodingEnd();
    }
  }]);

  return AndroidRecorder;
}();