#include <stdlib.h>
#include <check.h>
START_TEST(ocelot_testcase1)
{



case2(__ptr0,__ptr1);

/* REPLACE THE ASSERTION BELOW */
ck_assert_str_eq("OK", "OK");
}
END_TEST


Suite * ocelot_generated_94572cbd(void)
{
Suite *s;
TCase *temp_tc;

s = suite_create("ocelot_generated_94572cbd");

temp_tc = tcase_create("ocelot_testcase1");
tcase_add_test(temp_tc, ocelot_testcase1);
suite_add_tcase(s, temp_tc);

return s;
}

int main(void) {
int number_failed;
Suite *s;
SRunner *sr;

s = ocelot_generated_94572cbd();
sr = srunner_create(s);

srunner_run_all(sr, CK_NORMAL);
number_failed = srunner_ntests_failed(sr);
srunner_free(sr);
return (number_failed == 0) ? EXIT_SUCCESS : EXIT_FAILURE;
}