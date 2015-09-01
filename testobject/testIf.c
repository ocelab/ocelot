#include <stdio.h>
int test_if(int a, int b, int c){
	int flag;
	flag = 0;

	if (a == b) {
		return 2;
	} else {
		if (b == c) {
			return 1;
		} else {
			return 0;
		}
	}

	if (a == 0)
		flag = 1;
	flag=3;

	return flag;
}
