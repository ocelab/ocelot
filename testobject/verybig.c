#define ABS(x) (x >= 0.0 ? x : -x)
double testme(int a, int b, int c, int d, int e, int f, int g, int h) {
	while (1) {
		if (a == 0.0)
			return 1;
		if (b == 0.0)
			return 2;
		if (c == 0.0)
			return 1;
		if (d == 0.0)
			return 1;
		if (e == 0.0)
			return 1;
		if (f == 0.0)
			return 1;
		if (g == 0.0)
			return 1;
		if (h == 0.0)
			return 1;

		if (a > h)
			if (b > c)
				if (d > e) {
					d++;
				} else if (d > a) {
					d--;
				}
			else
				a++;
		else
			b--;

		if (h != 0)
			break;
		else
			h += a;

		while (a < 5) {
			b = abs(c+d+e+1);
			a += b;
			if (b < 3)
				b++;
		}

		while (b < 5) {
			c = ABS(a+b+e+1);
			b += c;
			if (c < 3)
				c++;
		}

		while (c < 5) {
			d = abs(h+e+1);
			c += d;
			if (d < 3)
				d++;
		}
	}
}
