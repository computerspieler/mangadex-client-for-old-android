#!/bin/bash

build() {
    docker run -v .:/opt/ndk/apps/prj -i -t android-1.6 -c 'build prj debug'
}

install() {
    adb install -r project/bin/MainActivity-debug.apk &&
    adb shell 'am start -n fr.speilkoun.mangareader/fr.speilkoun.mangareader.MainActivity'
}

build && install