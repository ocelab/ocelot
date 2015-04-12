#include <stdio.h>
#include <math.h>

#define OCELOT_TESTFUNCTION gimp_rgb_to_hsv_int
typedef int gint;
typedef double gdouble;

typedef struct {
	gint* red;
	gint* green;
	gint* blue;
} GimpColor;

void OCELOT_TESTFUNCTION (GimpColor);
