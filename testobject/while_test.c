double whileTest(double a, double b, double c)
{
	double a_1 = 5;
	double a_2 = 7;
	double a_3 = 9;

	while (a > a_1)
		a -= 1;
	while (a_2 < 700)
		a_2 += c;
	while (b > a_3)
		b -= a_3;
	return a;
}


double cdg (double a, double b, double c)
{
	if (a > 10){
		a++;
	} else {
		if(b < 20)
			return(-1);
		else
			b--;
	}

	if(c > 10){
		c++;
	} else {
		c--;
	}
	a++;
}
