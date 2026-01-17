# How to build this app
First, clone [this repository and retrieve the android-1.6_r1-improved branch](https://github.com/computerspieler/docker-build-env/tree/android-1.6_r1-improved), and follow the instructions in the `README.md` file.

## Build the 3rd party dependancies
Then, to create and run the build environment for the dependancies:
```bash
docker build -t android-1.6-ssl .
docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6-ssl
```

### OpenSSL
Thanks to: https://mta.openssl.org/pipermail/openssl-users/2022-June/015221.html

First, download **OpenSSL 3.5.4**, and extract it in a `openssl` folder within this project.
Then, start the build environment as shown above, and run the following commands:

```bash
cd <THE_NAME_OF_YOUR_OPENSSL_FOLDER>
patch -p1 --merge -i ../openssl.patch
./Configure --prefix=$PREFIX no-threads no-autoload-config no-tests no-jitter no-seed no-idea no-bf no-cast no-md2 no-asm android-arm --debug -march=armv4t
make
make install_sw install_ssldirs
```

### libjpeg (Not used for the moment)
Thanks to: https://warpedtimes.wordpress.com/2010/02/03/building-open-source-libraries-with-android-ndk/

```bash
cd jpeg-9f
./configure --host=arm-linux-androideabi --prefix=$PREFIX -disable-libtool-lock CFLAGS="-nostdlib" LIBS="-lc -lm" LDFLAGS="-Wl,-rpath-link=/opt/ndk/platforms/android-4/arch-arm/usr/lib/ -L/opt/ndk/platforms/android-4/arch-arm/usr/lib" CPP="arm-linux-androideabi-cpp" CPPFLAGS="-I/opt/ndk/platforms/android-4/arch-arm/usr/include"
make
make install
```

### curl
```bash
cd curl
./configure --prefix=$PREFIX \
    --host=arm-linux-androideabi --with-openssl=$PREFIX \
    --enable-debug --enable-shared \
    --disable-dict --disable-ftp --disable-imap --disable-ldap --disable-ldaps --disable-pop3 --disable-proxy --disable-rtsp --disable-smtp --disable-telnet --disable-tftp --disable-zlib --disable-docs \
    --without-ca-bundle --without-gnutls --without-libidn --without-librtmp --without-libssh2 --without-nss --without-zlib --without-libpsl \
    CPPFLAGS="-I/opt/ndk/platforms/android-4/arch-arm/usr/include" \
    LDFLAGS="--sysroot=/opt/ndk/platforms/android-4/arch-arm" \
    LIBS="-lc -ldl"
make
make install
```

You can now quit the root shell of the build environment.

### The main application
To build the main application, either you launch the `scripts/install.sh` script from the root folder,
or you can run the commands manually as follows:

```bash
# This is to build the main application
docker run -v .:/opt/ndk/apps/prj -i -t android-1.6 -c 'build prj debug'

# This is to install it
adb install -r bin/MainActivity-debug.apk
```

**Note:** The build process generates an APK targetting Android 1.6 (API level 4). On modern Android devices, you might encounter an `INSTALL_FAILED_DEPRECATED_SDK_VERSION` error when installing the software. In that case, add the `--bypass-low-target-sdk-block` argument to the `adb install` command.

As you might have guessed, the output apk is stored in `{root_of_repository}/bin/`.