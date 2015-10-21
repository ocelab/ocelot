#include "lists.h"
#include "ocelot.h"
#include <string.h>


_T_ocelot_list* _f_ocelot_list_allocate(size_t size) {
	return g_array_new(FALSE, FALSE, size);
}

void _f_ocelot_list_free(_T_ocelot_list *list) {
	g_array_free(list, TRUE);
}

void _f_ocelot_list_append(_T_ocelot_list *list, void* value) {
	g_array_append_val(list, value);
}

void _f_ocelot_list_shift(_T_ocelot_list *list) {
	g_array_remove_index(list, 0);
}
