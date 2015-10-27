typedef int gint;

int abs(int x) {
	if (x < 0)
		return -x;
	else
		return x;
}

gint test(int x) {
	scanf("%d", &x);
	if (abs(x) < -1) {
		return 0;
	}

	switch(abs(x)) {
	case 0:
	case 1:
	case 2:
	case 3:
	default:
		return -11;
	}

	return x;
}
