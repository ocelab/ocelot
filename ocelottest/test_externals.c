typedef int gint;

gint c;
gint *d;

gint test() {
	gint a = 0;

	if (a + c + d[0] + d[1] + d[2] < 10)
		return 0;

	return a;
}
