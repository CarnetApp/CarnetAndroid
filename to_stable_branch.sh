#!/bin/bash


#git checkout stable
#git checkout -b stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
#git push origin stable-$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
version=$(grep -o "versionName\s\+.*" app/build.gradle | tail -1 | awk '{ print $2 }' | tr -d \''"\')
git branch -D stable
git push origin --delete stable
git push framagit --delete stable
git checkout main
git checkout -b stable
git push origin stable
git push framagit stable
git checkout -b stable-$version
git push origin stable-$version
git push framagit stable-$version
git checkout main

cd ../Sync
git branch -D stable
git push origin --delete stable
git push framagit --delete stable
git checkout main
git checkout -b stable
git push origin stable
git push framagit stable
git checkout -b stable-$version
git push origin stable-$version
git push framagit stable-$version
git checkout main
cd ../CarnetAndroid
