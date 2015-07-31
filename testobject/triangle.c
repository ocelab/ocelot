int triangle(int a, int b, int c) {
	if (a+b > c)
		if (a+c > b)
			if (b+c > a) {
				int flag;
				flag = 0;

				if (a == b)
					if (b == c)
						return 1;

				if (a == 0)
					flag = 1;

				if (flag && b == 0)
					return 2;

				return -1;
			}
	
	return -1;
}
