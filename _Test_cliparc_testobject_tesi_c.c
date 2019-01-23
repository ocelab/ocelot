#include <stdlib.h>
#include <check.h>
Suite * ocelot_generated_d085eed4(void)
{
Suite *s;
TCase *temp_tc;

s = suite_create("ocelot_generated_d085eed4");

return s;
}

int main(void) {
int number_failed;
Suite *s;
SRunner *sr;

s = ocelot_generated_d085eed4();
sr = srunner_create(s);

srunner_run_all(sr, CK_NORMAL);
number_failed = srunner_ntests_failed(sr);
srunner_free(sr);
return (number_failed == 0) ? EXIT_SUCCESS : EXIT_FAILURE;
}