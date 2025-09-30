LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := http-parser
LOCAL_SRC_FILES := http-parser.c api.c
LOCAL_CFLAGS := -fpic -I$(LOCAL_PATH)/openssl/include
LOCAL_LDLIBS := -ldl -llog -Wl,--whole-archive $(LOCAL_PATH)/openssl/lib/libssl.a $(LOCAL_PATH)/openssl/lib/libcrypto.a -Wl,--no-whole-archive

include $(BUILD_SHARED_LIBRARY)
