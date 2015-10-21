#include <glib.h>
#include "stdlib.h"
#include "stddef.h"

#ifndef _Included_OcelotListsHeader
#define _Included_OcelotListsHeader
#ifdef __cplusplus
extern "C" {
#endif

#define _T_ocelot_list GArray

_T_ocelot_list* _f_ocelot_list_allocate(size_t);

void _f_ocelot_list_free(_T_ocelot_list*);

void _f_ocelot_list_append(_T_ocelot_list*, void*);
void* _f_ocelot_list_get(_T_ocelot_list*, int);
void _f_ocelot_list_shift(_T_ocelot_list*);


#define OCLIST_ALLOC(type) g_array_new(FALSE, FALSE, sizeof(type))
#define OCLIST_FREE(list) g_array_free(list, TRUE)

#define OCLIST_APPEND(list, value) g_array_append_val(list, value)
#define OCLIST_GET(list, index, type) g_array_index(list, type, index)
#define OCLIST_SHIFT(list) g_array_remove_index(list, 0)
#define OCLIST_SIZE(list) list->len

#ifdef __cplusplus
}
#endif
#endif
