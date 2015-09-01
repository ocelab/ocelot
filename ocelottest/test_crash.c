int test(int a) {
	if (a == 23) {
		int* c;
		c = 0;
		return *c; //SEGMENTATION FAULT
	}

	return 0;
}
