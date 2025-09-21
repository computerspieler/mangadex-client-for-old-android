#!/bin/bash

#./Configure --prefix=$PREFIX android-arm -DOPENSSL_NO_AUTOLOAD_CONFIG -DOPENSSL_NO_HW
docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6 -c 'build prj debug'
adb uninstall fr.speilkoun.mangareader
adb install project/bin/MainActivity-debug.apk
adb shell 'am start -n fr.speilkoun.mangareader/fr.speilkoun.mangareader.MainActivity'
