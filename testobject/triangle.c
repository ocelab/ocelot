int test_me(int a, int b, int c) {
	int flag;
	flag = 0;

	if (a == b)
		if (b == c)
			return 1;

	if (a == 0)
		flag = 1;

	if (a == 0 && b == 0)
		return 2;
	
	return -1;
}
