#include "api.h"

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "MangaDexJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int initialized = 0;

JNIEXPORT void Java_fr_speilkoun_mangareader_OpenSSL_init(JNIEnv* env, jclass *cls)
{
	init_api();
  
    initialized = 1;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    if(initialized)
	    deinit_api();
}

JNIEXPORT jstring
Java_fr_speilkoun_mangareader_OpenSSL_getChapterImages(
    JNIEnv* env,
    jclass cls,
    jstring id
)
{
	Context ctx;
	int code;

	createContext(&ctx);

    code = getChapterImages(&ctx, (*env)->GetStringUTFChars(env, id, 0));
	if(code != 200)
		LOGI("%d\n", code);
	else
		LOGI("%s\n", getResponseBody());

	freeContext(&ctx);

    return (*env)->NewStringUTF(env, getResponseBody());
}
