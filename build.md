# How to build this app
First, clone [this repository and retrieve the android-1.6_r1 branch](https://github.com/computerspieler/docker-build-env/tree/android-1.6_r1), and follow the instructions in the `README.md` file.

Then, to create and run the build environment for the dependancies:
```bash
docker build -t android-1.6-ssl .
docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6-ssl
```

### OpenSSL
Thanks to: https://mta.openssl.org/pipermail/openssl-users/2022-June/015221.html

First, download **OpenSSL 3.5.4**, and apply the `openssl.patch`.
Then, start the build environment as shown above, and run the following commands:

```bash
cd openssl
./Configure --prefix=$PREFIX no-threads no-autoload-config no-tests no-jitter no-seed no-idea no-bf no-cast no-md2 no-asm android-arm --debug -march=armv4t
make
make install_sw install_ssldirs
mkdir -p $PREFIX/../../libs/armeabi/
cp $PREFIX/lib/libssl.so $PREFIX/lib/libcrypto.so $PREFIX/../../libs/armeabi/
```

### libjpeg
Thanks to: https://warpedtimes.wordpress.com/2010/02/03/building-open-source-libraries-with-android-ndk/

```bash
cd jpeg-9f
./configure --host=arm-eabi --prefix=$PREFIX -disable-libtool-lock CFLAGS="-nostdlib" LIBS="-lc -lm" LDFLAGS="-Wl,-rpath-link=/opt/ndk/build/platforms/android-4/arch-arm/usr/lib/ -L/opt/ndk/build/platforms/android-4/arch-arm/usr/lib" CPP=/opt/ndk/build/prebuilt/linux-x86/arm-eabi-4.2.1/bin/arm-eabi-cpp CPPFLAGS="-I/opt/ndk/build/platforms/android-4/arch-arm/usr/include"
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
adb install -r project/bin/MainActivity-debug.apk
```

As you might have guessed, the output apk is stored in `{root_of_repository}/project/bin/`.