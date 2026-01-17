LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libssl
LOCAL_SRC_FILES := $(LOCAL_PATH)/openssl/lib/libssl.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := libcrypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/openssl/lib/libcrypto.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := libcurl
LOCAL_SRC_FILES := $(LOCAL_PATH)/openssl/lib/libcurl.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := http-parser
LOCAL_SRC_FILES := http-parser.c
LOCAL_CFLAGS := -I$(LOCAL_PATH)/openssl/include
LOCAL_LDLIBS := -ldl -llog
# Please don't change the order of these 
LOCAL_STATIC_LIBRARIES := libcurl libssl libcrypto

include $(BUILD_SHARED_LIBRARY)
