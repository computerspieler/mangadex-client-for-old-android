#include "api.h"

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <openssl/bio.h>
#include <openssl/ssl.h>
#include <openssl/err.h>

#define HTTPS_PORT "443"
#define USER_AGENT "manga-reader-for-old-android"
#define BUFFER_SIZE 4096

#include <android/log.h>

#define LOG_TAG "MangaDexJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static char *BUFFER;
static int INTERNAL_BUFFER_SIZE;

void init_api(void) {
    BUFFER = NULL;
    INTERNAL_BUFFER_SIZE = 0;
}

void deinit_api() {
    if(BUFFER)
        free(BUFFER);
    
    BUFFER = NULL;
    INTERNAL_BUFFER_SIZE = 0;
}

static int prepareBuffer(int size) {
    if(!BUFFER)
        BUFFER = malloc(size);
    else if(INTERNAL_BUFFER_SIZE < size)
        BUFFER = realloc(BUFFER, size);

    INTERNAL_BUFFER_SIZE = size;
    return (BUFFER != NULL);
}

const char* getResponseBody(void) {
    return BUFFER;
}

void freeContext(Context *ctx) {
	BIO_free_all(ctx->bio);
	SSL_CTX_free(ctx->ctx);
}

int createContext(Context *ctx, const char *domain) {
	SSL* ssl;
    const SSL_METHOD *method;
    char hostname_buffer[BUFFER_SIZE+1];
    size_t bytes;

    bytes = snprintf(hostname_buffer, BUFFER_SIZE,
        "%s:" HTTPS_PORT,
        domain
    );
    hostname_buffer[bytes] = 0;

    method = TLS_client_method();

	ctx->ctx = SSL_CTX_new(method);
	if (!ctx->ctx) {
		ERR_print_errors_fp(stderr);
		return 1;
	}

	ctx->bio = BIO_new_ssl_connect(ctx->ctx);
	if (!ctx->bio) {
        ERR_print_errors_fp(stderr);
        SSL_CTX_free(ctx->ctx);
        return 1;
    }

	BIO_set_conn_hostname(ctx->bio, hostname_buffer);

	BIO_get_ssl(ctx->bio, &ssl);
	if(!ssl) {
		LOGE("Can't get SSL pointer\n");
        freeContext(ctx);
        return 1;
	}

	if (!SSL_set_tlsext_host_name(ssl, domain)) {
        ERR_print_errors_fp(stderr);
        SSL_free(ssl);
        SSL_CTX_free(ctx->ctx);
        return 1;
    }

	if(BIO_do_connect(ctx->bio) <= 0) {
		ERR_print_errors_fp(stderr);
		return 1;
	}

	if (BIO_do_handshake(ctx->bio) <= 0) {
        ERR_print_errors_fp(stderr);
        freeContext(ctx);
        return 1;
    }

    BIO_should_read(ctx->bio);

	LOGI("Connected with %s encryption\n", SSL_get_cipher(ssl));

	return 0;
}

#define BUILD_GET_REQUEST(path, domain) \
    "GET " path " HTTP/1.1\r\n"         \
    "Host: " domain "\r\n"              \
    "User-Agent: " USER_AGENT "\r\n"    \
    "Accept: */*\r\n"                   \
    "Connection: keep-alive\r\n\r\n"

int run_request_and_get_json(Context *ctx, const char* domain, const char *path) {
    char buffer[BUFFER_SIZE+1];
    int error_code;
    int bytes, output_size, copied_length;
    char *body_start;

    bytes = snprintf(
        buffer,
        BUFFER_SIZE,
        BUILD_GET_REQUEST("%s", "%s"),
        path, domain
    );

    if(bytes == BUFFER_SIZE)
        return -5;

    if(BIO_write(ctx->bio, buffer, bytes) < 0) {
		ERR_print_errors_fp(stderr);
        return -1;
    }

    output_size = 0;
    if ((bytes = BIO_read(ctx->bio, buffer, BUFFER_SIZE)) <= 0) {
		ERR_print_errors_fp(stdout);
        return -2;
    }

    buffer[bytes] = 0;
    if(strncmp(buffer, "HTTP/1.1 200 OK", strlen("HTTP/1.1 200 OK")) != 0) {
        LOGE("Invalid response\n");
        error_code = -5;
        sscanf(buffer, "HTTP/1.1 %d", &error_code);
        return -error_code;
    }
    
    body_start = strstr(buffer, "content-length: ");
    if(body_start) {
        body_start += strlen("content-length: ");
        sscanf(body_start, "%d", &output_size);
        LOGI("Found content-length: %d", output_size);
    } else
        body_start = buffer;
    
    body_start = strstr(body_start, "\r\n\r\n");
    assert(body_start);

    body_start += 4;    // Skip the substring
    assert((body_start - buffer) < BUFFER_SIZE);

    // We need to do this because the content-length
    // is not always specified
    if(!output_size) {
        sscanf(body_start, "%X\r\n", &output_size);
    }
    
    LOGI("Length: %d", output_size);
    if(*body_start != '{') {
        body_start = strstr(body_start, "\r\n");
        if(!body_start)
            return -3;
        body_start += 2;
    }
    
    if(*body_start != '{' || output_size == 0)
        return -4;
    prepareBuffer(output_size+1);
    BUFFER[output_size] = 0;
    
    copied_length = bytes - (body_start - buffer);
    memcpy(BUFFER, body_start, copied_length);

    while ((bytes = BIO_read(ctx->bio, BUFFER + copied_length, output_size - copied_length)) > 0) {
        copied_length += bytes;
    }

    // Consume the rest
    while ((bytes = BIO_read(ctx->bio, buffer, BUFFER_SIZE)) > 0);

    return output_size;
}

int run_request_and_download_file(Context *ctx, FILE *f, const char *domain, const char *path)
{
    int bytes, initialised;
    size_t bytes_written;
    char buffer[BUFFER_SIZE+1];
    char *body_start;

    bytes = snprintf(
        buffer,
        BUFFER_SIZE,
        BUILD_GET_REQUEST("%s", "%s"),
        path,
        domain
    );

    if(BIO_write(ctx->bio, buffer, bytes) < 0) {
		ERR_print_errors_fp(stderr);
        return -1;
    }

    initialised = 0;
    bytes_written = 0;
    while ((bytes = BIO_read(ctx->bio, buffer, BUFFER_SIZE)) > 0) {
        
        // If this is the first read, then we must skip the html header
        if(!initialised) {
            initialised = 1;

            body_start = strstr(buffer, "\r\n\r\n");
            assert(body_start);

            body_start += 4;    // Skip the substring
            assert((body_start - buffer) < BUFFER_SIZE);

            bytes -= (int) (body_start - buffer);
        } else
            body_start = buffer;
        
        bytes_written += bytes;
        if(fwrite(body_start, sizeof(char), bytes, f) == bytes)
            continue;
        // TODO: Handle invalid writes
    }
    return (int) bytes_written;
}
