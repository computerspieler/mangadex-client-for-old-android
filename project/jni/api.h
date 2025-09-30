#ifndef API_H
#define API_H

#include <stdio.h>
#include <openssl/bio.h>
#include <openssl/ssl.h>

struct Context {
	BIO* bio;
	SSL_CTX* ctx;
};
typedef struct Context Context;

void init_api(void);
void deinit_api(void);
int createContext(Context *ctx, const char *domain);
void freeContext(Context *ctx);

const char* getResponseBody(void);

int run_request_and_get_json(Context *ctx, const char* domain, const char *path);
int run_request_and_download_file(Context *ctx, FILE *f, const char *domain, const char *path);

#endif /* API_H */
