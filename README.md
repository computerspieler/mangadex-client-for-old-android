
# How to build OpenSSL ?

- `docker build -t android-1.6-ssl .`
- `docker run -v ".:/opt/ndk/apps/openssl" -i -t android-1.6-ssl -c '/bin/bash'`

Apply some patches

- `./Configure --prefix=$PREFIX android-arm -DOPENSSL_NO_AUTOLOAD_CONFIG -DOPENSSL_NO_HW`
- `make`
- `make install`

# How to build the project ?

`docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6 -c 'build prj debug'`

