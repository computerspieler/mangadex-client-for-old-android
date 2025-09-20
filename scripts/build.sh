#!/bin/bash

docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6 -c 'build prj debug'
apk install project/bin/MainActivity-debug.apk

