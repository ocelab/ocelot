typedef int gint;

int test() {
	char a[] = {1,2,3};
	switch (a[0]) {
	case 'a':
	case 'b':
		return 0;
	default:
		return 1;
	}

	return 0;
}
