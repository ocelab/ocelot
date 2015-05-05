#include <math.h>

int test_me(int a, int b, int c) {
	if (a < 0)
		return 0;

	while (b > c) {
		if (a > c) {
			c++;
			b -= c;
		} else {
			b -= a;
		}
	}

	c++;

	return 1;
}
