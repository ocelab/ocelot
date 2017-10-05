typedef struct {
    int a,
    int b
} testStruct;

testStruct testFunc(int a, int b, double c, testStruct *d) {
    if (a > b) {
        if (b < c) {
            d.a = 1000;
        }

        d.b = 1000;
    }

    return d;
}