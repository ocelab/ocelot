int sad(int a, int b, int c) {
	while (1) {
		if (a == 0)
			continue;
		a++;
		--b;
		if (b == -1)
			break;
	}

	return a;
}
