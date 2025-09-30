#include "api.h"

#include <jni.h>
#include <errno.h>
#include <android/log.h>
#include <openssl/ssl.h>

#define LOG_TAG "HttpParserJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define THROW_HTTP_EXCEPTION(env, msg) {				\
	jclass exception_cls = (*env)->FindClass(			\
		env,											\
		"fr/speilkoun/mangareader/utils/HTTPException"	\
	);													\
	(*env)->ThrowNew(env, exception_cls, "");			\
}

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

JNIEXPORT void
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
		THROW_HTTP_EXCEPTION(env, "Unable to open the desired file");
    }

	raw_domain = (*env)->GetStringUTFChars(env, domain, 0);
	createContext(&ctx, raw_domain);
    
    output = run_request_and_download_file(&ctx, f,
        raw_domain,
        (*env)->GetStringUTFChars(env, path, 0)
    );

	freeContext(&ctx);
	fclose(f);
	
	if(output < 0) {
		LOGI("Received this error code from run_request_and_download_file: %d\n", output);
		THROW_HTTP_EXCEPTION(env, "Got an error from run_request_and_download_file");
	}
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
	freeContext(&ctx);

	if(output < 0) {
		LOGE("Received this error code from run_request_and_get_json: %d\n", output);
		THROW_HTTP_EXCEPTION(env, "Got an error from run_request_and_get_json");
		return NULL;
	}
	
    return (*env)->NewStringUTF(env, getResponseBody());
}
