LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := http-parser
LOCAL_SRC_FILES := http-parser.c api.c
LOCAL_CFLAGS := -I$(LOCAL_PATH)/openssl/include
LOCAL_LDLIBS := -ldl -llog -Wl,--whole-archive \
	-L$(LOCAL_PATH)/openssl/lib \
	-lssl-custom \
	-lcrypto-custom \
	-Wl,--no-whole-archive

include $(BUILD_SHARED_LIBRARY)
