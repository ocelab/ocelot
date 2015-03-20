int sad(int a, int b, int c) {
	switch (a) {
	case 0:
		b = 2;
	case 2:
		b = 3;
		break;
	default:
		c = 0;
	}

	return a;
}
