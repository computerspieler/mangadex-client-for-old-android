#include "api.h"

int main(void) {
	Context ctx;
	int code;
	
	init_api();
	
	createContext(&ctx);
	printf("=== getChapters ===\n");
	code = getChapters(&ctx, "f98660a1-d2e2-461c-960d-7bd13df8b76d", 0);
	if(code != 200)
		printf("%d\n", code);
	else
		printf("%s\n", getResponseBody());
	freeContext(&ctx);

	createContext(&ctx);
	printf("=== getChapterImages ===\n");
	code = getChapterImages(&ctx, "a54c491c-8e4c-4e97-8873-5b79e59da210");
	if(code != 200)
		printf("%d\n", code);
	else
		printf("%s\n", getResponseBody());
	freeContext(&ctx);

	deinit_api();
	return 0;
}
