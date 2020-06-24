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
    this.cantPause = true;
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
      this.setState("none");
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
    } // used for JS encoder

  }, {
    key: "encodeOgg",
    value: function encodeOgg(arrayBuffer) {
      var recorder = this;
      this.recordedPages = [];
      this.totalLength = 0;
      this.onEncodingStart();
      var encodeWorker = new Worker(rootpath + "reader/libs/recorder/encoderWorker.min.js");
      var bufferLength = 4096;
      encodeWorker.postMessage({
        command: 'init',
        encoderSampleRate: 48000,
        bufferLength: bufferLength,
        originalSampleRate: 44100,
        wavSampleRate: 44100,
        numberOfChannels: 2,
        maxFramesPerPage: 40,
        encoderApplication: 2049,
        encoderFrameSize: 20,
        encoderComplexity: 9,
        resampleQuality: 5,
        bitRate: 192000
      });
      encodeWorker.postMessage({
        command: 'getHeaderPages'
      });
      var typedArray = new Int16Array(arrayBuffer);

      for (var i = 0; i < typedArray.length; i += bufferLength * 2) {
        var tmpBufferList = [];
        var tmpBuffer = new Float32Array(bufferLength);
        var tmpBuffer2 = new Float32Array(bufferLength);
        var buf1 = 0;
        var buf2 = 0;

        for (var j = 0; j < bufferLength * 2; j++) {
          if (j % 2 < 1) {
            tmpBuffer[buf1] = typedArray[i + j] / 32768;
            buf1++;
          } else {
            tmpBuffer2[buf2] = typedArray[i + j] / 32768;
            buf2++;
          }
        }

        tmpBufferList.push(tmpBuffer);
        tmpBufferList.push(tmpBuffer2);
        encodeWorker.postMessage({
          command: 'encode',
          buffers: tmpBufferList
        });
      }

      encodeWorker.postMessage({
        command: 'done'
      });

      encodeWorker.onmessage = function (e) {
        console.log(e);

        if (e.data.message == "done") {
          var outputData = new Uint8Array(recorder.totalLength);
          recorder.recordedPages.reduce(function (offset, page) {
            outputData.set(page, offset);
            return offset + page.length;
          }, 0);
          recorder.onEncodingEnd();
          writer.recorder.recorder.ondataavailable(outputData, null);
        } else if (e.data.message == "page") {
          recorder.recordedPages.push(e.data.page);
          recorder.totalLength += e.data.page.length;
        }
      };
    }
  }, {
    key: "pauseUnavailable",
    value: function pauseUnavailable() {}
  }, {
    key: "onFileReady",
    value: function onFileReady(url, isEncoded) {
      console.log("AndroidRecorder: isEncoded " + isEncoded);

      if (isEncoded) {
        this.ondataavailable(undefined, url);
      }
      /* //if we use JS encoder:
        var rec = this
        var req = new XMLHttpRequest();
        req.open('GET', compatibility.addRequestToken("/api/note/open/tmpwave"), true);
        req.responseType = 'arraybuffer';
        req.onload = function () {
            rec.encodeOgg(req.response);
            
        };
        req.send();
        */

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