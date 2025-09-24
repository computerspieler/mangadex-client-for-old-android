#include "api.h"

#include <jni.h>
#include <android/log.h>
#include <openssl/ssl.h>

#define LOG_TAG "MangaDexJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int initialized = 0;

JNIEXPORT void Java_fr_speilkoun_mangareader_OpenSSL_init(JNIEnv* env, jclass *cls)
{
	SSL_library_init();
	SSL_load_error_strings();
	OpenSSL_add_all_algorithms();

	init_api();
  
    initialized = 1;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    if(initialized)
	    deinit_api();
}
