#include <math.h>

int test_me(int a, int b, int c) {
	if (a < 0 || c < 0)
		return 0;

	while (b > c) {
		if (a > c) {
			c++;
			b -= c;
		} else {
			b -= a+1;
		}
		b = b+1-1;
	}

	c++;

	return 1;
}
