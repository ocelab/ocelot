int sad(int a, int b, int c) {
	if (a == b)
		if (b == c)
			return 0;

	if (a == b)
		return 1;
	else if (b == c)
		return 1;
	else if (c == a)
		return 1;

	return 2;
}
