#include "api.h"
#include "linux/stddef.h"

#include <jni.h>
#include <errno.h>
#include <android/log.h>
#include <openssl/ssl.h>

#define LOG_TAG "HttpParserJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int initialized = 0;

JNIEXPORT void Java_fr_speilkoun_mangareader_utils_HTTP_init(JNIEnv* env, jclass *cls)
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

JNIEXPORT jboolean
Java_fr_speilkoun_mangareader_utils_HTTP_rawDownloadFile(
    JNIEnv* env,
    jclass cls,
    jstring output_path,
    jstring domain,
    jstring path
)
{
	Context ctx;
	int output;
    FILE *f;
	const char *raw_domain;

    f = fopen((*env)->GetStringUTFChars(env, output_path, 0), "wb");
    if(!f) {
        LOGE("Could not open the output path: %s", strerror(errno));
        return JNI_FALSE;
    }

	raw_domain = (*env)->GetStringUTFChars(env, domain, 0);
	createContext(&ctx, raw_domain);
    
    output = run_request_and_download_file(&ctx, f,
        raw_domain,
        (*env)->GetStringUTFChars(env, path, 0)
    );
	if(output < 0) {
		LOGI("%d\n", output);
		return JNI_FALSE;
	}
	
	freeContext(&ctx);
    fclose(f);

    return JNI_TRUE;
}


JNIEXPORT jstring
Java_fr_speilkoun_mangareader_utils_HTTP_getJSON(
    JNIEnv* env,
    jclass cls,
    jstring domain,
    jstring path
)
{
	Context ctx;
	int output;
	const char *raw_domain;

	raw_domain = (*env)->GetStringUTFChars(env, domain, 0);
	createContext(&ctx, raw_domain);
    
    output = run_request_and_get_json(&ctx,
        raw_domain,
        (*env)->GetStringUTFChars(env, path, 0)
    );
	if(output < 0) {
		LOGI("%d\n", output);
		return NULL;
	}
	
	freeContext(&ctx);

    return (*env)->NewStringUTF(env, getResponseBody());
}
