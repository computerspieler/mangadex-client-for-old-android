#include "api.h"

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "MangaDexJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

JNIEXPORT jstring
Java_fr_speilkoun_mangareader_sources_MangaDex_getChapterImages(
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
	
	freeContext(&ctx);

    return (*env)->NewStringUTF(env, getResponseBody());
}

JNIEXPORT jstring
Java_fr_speilkoun_mangareader_sources_MangaDex_getChapters(
    JNIEnv* env,
    jclass cls,
    jstring id,
	jint offset
)
{
	Context ctx;
	int code;

	createContext(&ctx);

    code = getChapters(&ctx, (*env)->GetStringUTFChars(env, id, 0), offset);
	if(code < 0) {
		LOGI("%d\n", code);
		return NULL;
	}
	
	freeContext(&ctx);

    return (*env)->NewStringUTF(env, getResponseBody());
}
