#!/bin/bash

PACKAGE=fr.speilkoun.mangareader

build() {
    docker run -v .:/opt/ndk/apps/prj -i -t android-1.6 -c 'build prj debug'
}

install_apk() {
    # We have to get grep's return code to check for a success because ADB
    # Doesn't returns anything by default
    adb install -r bin/MainActivity-debug.apk | tee /dev/stderr | grep -q Success
}

install() {
    install_apk
    # In case of failure, try to fully reinstall the package
    if [ $? -ne 0 ]; then
        echo "Failed to reinstall the package, trying a full reinstallation"
        adb uninstall "$PACKAGE"
        install_apk
    fi
    if [ $? -eq 0 ]; then
        # Kill the current process (if it exists, otherwise it will do nothing)
        adb shell kill $(adb shell ps | grep -e 'fr.speilkoun.mangareader' | sed  's/[^ ]*[ ]*\([0-9]*\)[^0-9].*/\1/g')
        # Start a new process
        adb shell "am start -n $PACKAGE/$PACKAGE.MainActivity" & (
            # We have to do this because am can get stuck
            sleep 4; exit 0
        )
    fi
}

build && install
exit $?