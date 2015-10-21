#include "stdlib.h"
#include "stddef.h"

#ifndef _Included_OcelotListsHeader
#define _Included_OcelotListsHeader
#ifdef __cplusplus
extern "C" {
#endif

typedef struct node {
	struct node * next;
    void* value;
} _T_ocelot_list_node;

typedef struct {
	_T_ocelot_list_node* root;
	size_t element_size;
	int size;
} _T_ocelot_list;

_T_ocelot_list* _f_ocelot_list_allocate(size_t);
_T_ocelot_list_node* _f_ocelot_list_allocate_node(void*, size_t);

void _f_ocelot_list_free(_T_ocelot_list*);
void _f_ocelot_list_free_node(_T_ocelot_list_node*);

_T_ocelot_list_node* _f_ocelot_list_last_node(_T_ocelot_list*);

void _f_ocelot_list_append(_T_ocelot_list*, void*);
void* _f_ocelot_list_get(_T_ocelot_list*, int);
void _f_ocelot_list_shift(_T_ocelot_list*);


#define OCLIST_ALLOC(type) _f_ocelot_list_allocate(sizeof(type))
#define OCLIST_FREE(list) _f_ocelot_list_free(list)

#define OCLIST_APPEND(list, value) _f_ocelot_list_append(list, &(value))
#define OCLIST_GET(list, index, type) *((type*)_f_ocelot_list_get(list, index))
#define OCLIST_SHIFT(list) _f_ocelot_list_shift(list)
#define OCLIST_SIZE(list) list->size

#ifdef __cplusplus
}
#endif
#endif
