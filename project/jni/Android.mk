LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := manga-reader
LOCAL_SRC_FILES := manga-reader.c api.c mangadex.c utils.c
LOCAL_CFLAGS := -I$(LOCAL_PATH)/openssl/include
LOCAL_LDLIBS := -ldl -llog -Wl,--whole-archive $(LOCAL_PATH)/openssl/lib/libssl.a $(LOCAL_PATH)/openssl/lib/libcrypto.a -Wl,--no-whole-archive

include $(BUILD_SHARED_LIBRARY)
