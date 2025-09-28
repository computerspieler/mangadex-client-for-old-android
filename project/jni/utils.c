#include "api.h"

#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "UtilsJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

JNIEXPORT jboolean
Java_fr_speilkoun_mangareader_utils_FileDownloader_rawDownloadFile(
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

    f = fopen((*env)->GetStringUTFChars(env, output_path, 0), "wb");
    if(!f) {
        LOGE("Could not open the output path: %s", strerror(errno));
        return JNI_FALSE;
    }

	createContext(&ctx);
    
    output = downloadFile(&ctx, f,
        (*env)->GetStringUTFChars(env, domain, 0),
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
