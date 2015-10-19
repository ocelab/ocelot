typedef int gint;

int test(int a, int b, int c) {
	if (a == b)
		if (b == c)
			return -1;

	return 0;
}

int testDouble(double a) {
	if (a == 0.0)
		return 1;

	return 0;
}
