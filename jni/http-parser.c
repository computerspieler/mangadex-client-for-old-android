#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

#include <curl/curl.h>
#include <curl/easy.h>

#define LOG_TAG "HttpParserJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define THROW_HTTP_EXCEPTION(env, msg) {				\
	jclass exception_cls = (*env)->FindClass(			\
		env,											\
		"fr/speilkoun/mangareader/utils/HTTPException"	\
	);													\
	(*env)->ThrowNew(env, exception_cls, msg);			\
}

#define USER_AGENT "manga-reader-for-old-android"

static int initialized = 0;
/* This is needed by libssl */
const unsigned char __clz_tab[] = {
	0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5,
	    5, 5, 5, 5, 5, 5, 5, 5,
	6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
	    6, 6, 6, 6, 6, 6, 6, 6,
	7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
	    7, 7, 7, 7, 7, 7, 7, 7,
	7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
	    7, 7, 7, 7, 7, 7, 7, 7,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	    8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	    8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	    8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	    8, 8, 8, 8, 8, 8, 8, 8,
};

static size_t common_buffer_size = 0;
static size_t common_buffer_filled = 0;
static char *common_buffer = NULL;

JNIEXPORT void Java_fr_speilkoun_mangareader_utils_HTTP_init(JNIEnv* env, jclass *cls)
{
	CURLcode result;
	
	common_buffer_size = 1024;
	common_buffer = (char*) malloc(common_buffer_size * sizeof(common_buffer_size));
	common_buffer_filled = 0;

	result = curl_global_init(CURL_GLOBAL_ALL);
	if(result)
	    THROW_HTTP_EXCEPTION(env, "Could not initilize Curl");

    initialized = 1;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
	if(!initialized) 
		return;
	curl_global_cleanup();

	free(common_buffer);
	common_buffer = NULL;
	common_buffer_size = 0;
}

size_t write_chunk_to_file(void *data, size_t size, size_t nmemb, void *userdata)
{
	FILE *fp = (FILE*) userdata;
	return fwrite(data, size, nmemb, fp);
}

JNIEXPORT void
Java_fr_speilkoun_mangareader_utils_HTTP_rawDownloadFile(
    JNIEnv* env,
    jclass cls,
    jstring output_path,
    jstring url
)
{
	CURL *curl;
	CURLcode result;
	long res_status;
	FILE *fp;

	fp = fopen((*env)->GetStringUTFChars(env, output_path, 0), "wb");
	if(!fp) {
		LOGE("Could not open the output file: %s", strerror(errno));
		THROW_HTTP_EXCEPTION(env, "Could not open the output file");
	}

	curl = curl_easy_init();
  	if(!curl) 
		THROW_HTTP_EXCEPTION(env, "Could not instantiate a curl instance");
    
	LOGI("Querying %s", (*env)->GetStringUTFChars(env, url, 0));
	curl_easy_setopt(curl, CURLOPT_URL, (*env)->GetStringUTFChars(env, url, 0));
	curl_easy_setopt(curl, CURLOPT_CA_CACHE_TIMEOUT, 604800L);
	curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_chunk_to_file);
	curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);
	curl_easy_setopt(curl, CURLOPT_USERAGENT, USER_AGENT);
	
	curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);

	result = curl_easy_perform(curl);
	curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &res_status);
	curl_easy_cleanup(curl);
	fclose(fp);
	
	if(result != CURLE_OK) {
		LOGE("Got an error from Curl: %s", curl_easy_strerror(result));
		THROW_HTTP_EXCEPTION(env, curl_easy_strerror(result));
		return NULL;
	}

	if(res_status != 200) {
		LOGE("Got the following HTTP code: %ld", res_status);
		THROW_HTTP_EXCEPTION(env, "Got an invalid HTTP code");
		return NULL;
	}
}

size_t write_chunk_to_shared_buffer(void *data, size_t size, size_t nmemb, void *userdata)
{
	size_t real_size = size * nmemb;
	if(common_buffer_filled+real_size >= common_buffer_size) {
		common_buffer_size += real_size;
		common_buffer = realloc(common_buffer, common_buffer_size * sizeof(char));
		if(!common_buffer) {
			LOGE("Could not expand the temp buffer");
			return 0;
		}
	}

	memcpy(common_buffer + common_buffer_filled, data, real_size);
	common_buffer_filled += real_size;
	return real_size;
}

JNIEXPORT jstring
Java_fr_speilkoun_mangareader_utils_HTTP_getJSON(
    JNIEnv* env,
    jclass cls,
    jstring url
)
{
	CURL *curl;
	CURLcode result;
	long res_status;

	common_buffer_filled = 0;
	curl = curl_easy_init();
  	if(!curl) 
		THROW_HTTP_EXCEPTION(env, "Could not instantiate a curl instance");
    
	LOGI("Querying %s", (*env)->GetStringUTFChars(env, url, 0));
	curl_easy_setopt(curl, CURLOPT_URL, (*env)->GetStringUTFChars(env, url, 0));
	curl_easy_setopt(curl, CURLOPT_CA_CACHE_TIMEOUT, 604800L);
	curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_chunk_to_shared_buffer);
	curl_easy_setopt(curl, CURLOPT_USERAGENT, USER_AGENT);
	
	curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);

	result = curl_easy_perform(curl);
	curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &res_status);
	curl_easy_cleanup(curl);
	
	if(result != CURLE_OK) {
		LOGE("Got an error from Curl: %s", curl_easy_strerror(result));
		THROW_HTTP_EXCEPTION(env, curl_easy_strerror(result));
		return NULL;
	}

	if(res_status != 200) {
		LOGE("Got the following HTTP code: %ld", res_status);
		THROW_HTTP_EXCEPTION(env, "Got an invalid HTTP code");
		return NULL;
	}
	
	common_buffer[common_buffer_filled] = 0;
	return (*env)->NewStringUTF(env, common_buffer);
}
