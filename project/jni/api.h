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
int createContext(Context *ctx);
void freeContext(Context *ctx);

const char* getResponseBody(void);

int getChapters(Context *ctx, const char* id, size_t offset);
int getChapterImages(Context *ctx, const char* chapter_id);
int downloadChapter(Context *ctx, FILE *f,
	const char *domain, const char *hash, const char *file
);

#endif /* API_H */
