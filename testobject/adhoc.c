int adhoc(int a, int b) {
	while(a < 100) {
		if (b < 10) {
			if (b > 0) {
				label2:
				a += 1;
				continue;
			}
		}

		if (a == 14) {
			a++;
			continue;
		} else{
			a += 12;
			goto label2;
		}
	}
}
