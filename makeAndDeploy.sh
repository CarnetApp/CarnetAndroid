#!/bin/bash

if [ "release" == "$1" ]
  then
	./gradlew assembleRelease;
	apksigner sign --ks ../keystore --ks-key-alias quicknote --out quickdoc-release.apk app/build/outputs/apk/google/release/app-google-release.apk
	adb install -r quickdoc-release.apk
fi;

