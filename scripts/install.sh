#!/bin/bash

adb uninstall fr.speilkoun.mangareader
adb install project/bin/MainActivity-debug.apk
adb shell 'am start -n fr.speilkoun.mangareader/fr.speilkoun.mangareader.MainActivity'
