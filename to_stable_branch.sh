#!/bin/bash


#git checkout stable
#git checkout -b stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
#git push origin stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')

git branch -d stable
git push origin --delete stable
git checkout master
git checkout -b stable
git push origin stable
git checkout -b stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
git push origin stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
git checkout master
