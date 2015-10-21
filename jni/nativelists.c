#include "lists.h"
#include "ocelot.h"
#include <string.h>


_T_ocelot_list* _f_ocelot_list_allocate(size_t size) {
	_T_ocelot_list *list = (_T_ocelot_list*)malloc(sizeof(_T_ocelot_list));
	list->size = 0;
	list->root = NULL;
	list->element_size = size;

	return list;
}

_T_ocelot_list_node* _f_ocelot_list_allocate_node(void* element, size_t size) {
	_T_ocelot_list_node* node = (_T_ocelot_list_node*)malloc(sizeof(_T_ocelot_list_node));
	node->next = NULL;
	node->value = malloc(size);
	memcpy(node->value, element, size);

	return node;
}

void _f_ocelot_list_free(_T_ocelot_list *list) {
	if (list->root != NULL)
		_f_ocelot_list_free_node(list->root);

	free(list);
}

void _f_ocelot_list_free_node(_T_ocelot_list_node *node) {
	if (node->next != NULL) {
		_f_ocelot_list_free_node(node->next);
	}

	free(node->value);
	free(node);
}

_T_ocelot_list_node* _f_ocelot_list_last_node(_T_ocelot_list *list) {
	_T_ocelot_list_node *current_node = list->root;
	if (list->size == 0)
		return NULL;

	while (current_node->next != NULL) {
		current_node = current_node->next;
	}

	return current_node;
}

void _f_ocelot_list_append(_T_ocelot_list *list, void* value) {
	if (list->size == 0) {
		list->root = _f_ocelot_list_allocate_node(value, list->element_size);
		list->size = 1;
	} else {
		_T_ocelot_list_node* node = _f_ocelot_list_last_node(list);
		node->next = _f_ocelot_list_allocate_node(value, list->element_size);
		list->size++;
	}
}

void* _f_ocelot_list_get(_T_ocelot_list *list, int index) {
	if (index >= list->size)
		return NULL;

	int i;

	_T_ocelot_list_node *current_node = list->root;
	for (i = 0; i < index; i++)
		current_node = current_node->next;

	return current_node->value;
}

void _f_ocelot_list_shift(_T_ocelot_list *list) {
	if (list->size == 0)
		return;

	_T_ocelot_list_node *prev_root = list->root;
	list->root = list->root->next;

	free(prev_root->value);
	free(prev_root);
	list->size--;
}
