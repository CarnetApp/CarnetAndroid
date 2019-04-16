"use strict";

var CarnetRecorder = function CarnetRecorder() {
  this.init();
};

CarnetRecorder.prototype.reset = function () {
  this.hasRecorded = false;
  this.currentUrl = undefined;
  this.name = undefined;
  this.refreshButtons();
  var canvas = document.getElementById("analyser");
  canvas.getContext('2d').clearRect(0, 0, canvas.width, canvas.height);
};

CarnetRecorder.prototype.init = function () {
  if (!Recorder.isRecordingSupported()) {} else {
    var addZero = function addZero(i) {
      if (i < 10) {
        i = "0" + i;
      }

      return i;
    };

    var cancelAnalyserUpdates = function cancelAnalyserUpdates() {
      window.cancelAnimationFrame(rafID);
      rafID = null;
    };

    var carnetRecorder = this;
    var recordingDurationInt;
    this.startIcon = document.getElementById("startIcon");
    var currentTime = document.getElementById("current-time");
    var totalTime = document.getElementById("total-time");
    var stopButton = document.getElementById("stopButton");
    var start = document.getElementById("start");
    this.saveButton = document.getElementById("save-button");
    this.deleteButton = document.getElementById("delete-button");
    this.canvas = document.getElementById("analyser");
    var canvasWidth;
    var canvasHeight;
    var analyserContext;

    this.deleteButton.onclick = function () {
      if (carnetRecorder.hasRecorded) {
        console.log("url " + carnetRecorder.currentUrl);
        var test = carnetRecorder.currentUrl;
        console.log("name " + FileUtils.getFilename(test));
        writer.deleteMedia(carnetRecorder.name);
      }

      writer.recorderDialog.close();
    };

    this.saveButton.onclick = function () {
      writer.recorderDialog.close();
    };

    var wavesurfer = WaveSurfer.create({
      container: '#waveform'
    });
    this.wavesurfer = wavesurfer;
    this.hasRecorded = false;
    wavesurfer.on("finish", function () {
      carnetRecorder.refreshButtons();
    });
    var options = {
      monitorGain: 0,
      recordingGain: 1,
      numberOfChannels: 2,
      encoderBitRate: 192000,
      encoderSampleRate: 48000,
      encoderPath: rootpath + "reader/libs/recorder/encoderWorker.min.js"
    };
    var recordingDuration = 0;
    var recorder = new Recorder(options);
    this.recorder = recorder;

    stopButton.onclick = function () {
      recorder.stop();
    };

    start.onclick = function () {
      if (recorder.state === "recording") {
        recorder.pause();
      } else if (recorder.state === "paused") {
        recorder.resume();
      } else if (!carnetRecorder.hasRecorded) {
        recorder.start();
      } else if (!wavesurfer.isPlaying()) {
        wavesurfer.play();
      } else wavesurfer.pause();

      carnetRecorder.refreshButtons();
    };

    var analyserNode;
    var analyserContext;
    var rafID = null;

    var getFormatedTime = function getFormatedTime(t) {
      var d = new Date(t);
      return d.getMinutes() + ":" + addZero(d.getSeconds());
    };

    var updateTimers = function updateTimers() {
      if (!carnetRecorder.hasRecorded) {
        totalTime.innerHTML = currentTime.innerHTML = getFormatedTime(recordingDuration);
      } else {
        totalTime.innerHTML = getFormatedTime(wavesurfer.getDuration() * 1000);
        currentTime.innerHTML = getFormatedTime(wavesurfer.getCurrentTime() * 1000);
      }
    };

    var updateAnalysers = function updateAnalysers(time) {
      canvasWidth = carnetRecorder.canvas.width;
      canvasHeight = carnetRecorder.canvas.height;
      analyserContext = carnetRecorder.canvas.getContext('2d'); // analyzer draw code here

      {
        var SPACING = 3;
        var BAR_WIDTH = 3;
        var numBars = Math.round(canvasWidth / SPACING);
        var freqByteData = new Uint8Array(analyserNode.frequencyBinCount);
        analyserNode.getByteFrequencyData(freqByteData);
        analyserContext.clearRect(0, 0, canvasWidth, canvasHeight);
        analyserContext.fillStyle = '#F6D565';
        analyserContext.lineCap = 'round';
        var multiplier = analyserNode.frequencyBinCount / numBars; // Draw rectangle for each frequency bin.

        for (var i = 0; i < numBars; ++i) {
          var magnitude = 0;
          var offset = Math.floor(i * multiplier); // gotta sum/average the block, or we miss narrow-bandwidth spikes

          for (var j = 0; j < multiplier; j++) {
            magnitude += freqByteData[offset + j];
          }

          magnitude = magnitude / multiplier;
          var magnitude2 = freqByteData[i * multiplier];
          analyserContext.fillStyle = "hsl( " + Math.round(i * 360 / numBars) + ", 100%, 70%)";
          analyserContext.fillRect(i * SPACING, canvasHeight, BAR_WIDTH, -magnitude);
        }
      }
      rafID = window.requestAnimationFrame(updateAnalysers);
    };

    var updateRecordingDuration = function updateRecordingDuration() {
      recordingDuration += 1000;
    };

    recorder.onstart = function (e) {
      recordingDuration = 0;
      stopButton.disabled = false;
      analyserNode = recorder.audioContext.createAnalyser();
      analyserNode.fftSize = 2048;
      recorder.recordingGainNode.connect(analyserNode);
      updateAnalysers();
      carnetRecorder.refreshButtons();
      recordingDurationInt = setInterval(updateRecordingDuration, 1000);
    };

    recorder.onstop = function (e) {
      start.disabled = false;
      stopButton.disabled = true;
      cancelAnalyserUpdates();
      clearInterval(recordingDurationInt);
    };

    recorder.onpause = function (e) {
      stopButton.disabled = false;
      carnetRecorder.refreshButtons();
      cancelAnalyserUpdates();
      clearInterval(recordingDurationInt);
    };

    recorder.onresume = function (e) {
      stopButton.disabled = false;
      recordingDurationInt = setInterval(updateRecordingDuration, 1000);
      updateAnalysers();
    };

    recorder.ondataavailable = function (typedArray) {
      var fileName = new Date().toISOString() + ".opus";
      var dataBlob = new Blob([typedArray], {
        type: 'audio/ogg'
      });
      dataBlob.name = fileName;
      writer.sendFiles([dataBlob], function (list) {
        for (var i = 0; i < list.length; i++) {
          if (list[i].endsWith(fileName)) {
            carnetRecorder.currentUrl = api_url + list[i];
            carnetRecorder.name = FileUtils.getFilename(list[i]);
            console.log("current url " + carnetRecorder.currentUrl);
            break;
          }
        }
      });
      var url = URL.createObjectURL(dataBlob);
      carnetRecorder.setAudioUrl(url);
    };

    var timerInterval = -1;

    var startTimers = function startTimers() {
      timerInterval = setInterval(updateTimers, 100);
    };

    startTimers();
  }
};

CarnetRecorder.prototype.setAudioUrl = function (url, name) {
  console.log("name " + FileUtils.getFilename(url));
  this.name = name;
  this.currentUrl = url;
  this.hasRecorded = true;
  document.getElementById("waveform").style.display = "block";
  document.getElementById("analyser").style.display = "none";
  this.wavesurfer.load(url);
  this.refreshButtons();
};

CarnetRecorder.prototype["new"] = function () {
  document.getElementById("waveform").style.display = "none";
  document.getElementById("analyser").style.display = "block";
  this.reset();
};

CarnetRecorder.prototype.refreshButtons = function () {
  console.log("call");
  this.saveButton.disabled = !this.hasRecorded;

  if (this.recorder.state === "recording") {
    this.startIcon.innerHTML = "pause";
  } else if (this.recorder.state === "paused" || !this.hasRecorded) {
    this.startIcon.innerHTML = "mic";
  } else if (this.wavesurfer.isPlaying()) {
    this.startIcon.innerHTML = "pause";
  } else {
    this.startIcon.innerHTML = "play_arrow";
  }
};