FROM android-1.6:latest

USER root
CMD apt install -y perl

USER user
ENV ANDROID_NDK_ROOT $NDKROOT
ENV PATH $PATH:/opt/ndk/build/prebuilt/linux-x86/arm-eabi-4.2.1/bin/
WORKDIR /opt/ndk/apps/prj/openssl

ENV CROSS_SYSROOT /opt/ndk/build/platforms/android-4/arch-arm
ENV PREFIX /opt/ndk/apps/prj/project/jni/openssl

ENTRYPOINT ["/bin/bash", "-c"]

#./Configure android-arm --prefix=$PREFIX
#docker run -v ".:/opt/ndk/apps/prj" -i -t android-1.6-ssl
