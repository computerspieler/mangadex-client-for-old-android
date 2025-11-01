FROM android-1.6:latest

USER root
RUN apt install -y perl patch

USER user
ENV ANDROID_NDK_ROOT $NDKROOT
ENV PATH $PATH:$NDKROOT/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/bin
WORKDIR /opt/ndk/apps/prj

ENV CROSS_SYSROOT $NDKROOT/platforms/android-4/arch-arm
ENV PREFIX /opt/ndk/apps/prj/jni/openssl

ENTRYPOINT ["/bin/bash"]
