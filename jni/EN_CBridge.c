#include "CBridge.h"
#define EXECUTE_OCELOT_TEST GimpColor __arg0;\
int  __str0 = (int )OCELOT_numeric(OCELOT_ARG(0));\
__arg0.red = &__str0;\
int  __str1 = (int )OCELOT_numeric(OCELOT_ARG(1));\
__arg0.green = &__str1;\
int  __str2 = (int )OCELOT_numeric(OCELOT_ARG(2));\
__arg0.blue = &__str2;\
OCELOT_TESTFUNCTION (__arg0);

/*
 * ATTENTION:
 * The Java builder (it.unisa.ocelot.runnable.Build) will enrich this meta-c file with:
 * 1) The import if it_unisa_ocelot_simulator_CBridge.h file
 * 2) The definition of EXECUTE_OCELOT_TEST macro
 * Please, do not consider the absence of this part an error.
 */
#include <stdio.h>

/*
 * Class:     it_unisa_ocelot_simulator_CBridge
 * Method:    getEvents
 * Signature: (Lit/unisa/ocelot/simulator/EventsHandler;)V
 */
JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_getEvents
		(JNIEnv *env, jobject self, jobject eventHandler, jobjectArray arguments) {
	//Gets the class of the EventHandler instance
	jclass class = (*env)->GetObjectClass(env, eventHandler);

	//Gets the "add" method of the EventHandler class
	jmethodID addMethod = (*env)->GetMethodID(env, class, "add", "(IDD)V");
	jmethodID addCaseMethod = (*env)->GetMethodID(env, class, "addCase", "(IDZ)V");

	_f_ocelot_init();

	EXECUTE_OCELOT_TEST

	int i;
	for (i = 0; i < _v_ocelot_events->len; i++) {
		_T_ocelot_event event = g_array_index(_v_ocelot_events, _T_ocelot_event, i);
		if (event.kind == OCELOT_KIND_STDEV) {
			(*env)->CallVoidMethod(env, eventHandler, addMethod, event.choice, event.distanceTrue, event.distanceFalse);
		} else {
			(*env)->CallVoidMethod(env, eventHandler, addCaseMethod, event.choice, event.distanceTrue, (jboolean)event.distanceFalse);
		}
	}
}

jdouble _f_ocelot_numval(JNIEnv* env, jobject object) {
	jclass class = (*env)->FindClass(env, "java/lang/Double");

	jmethodID doubleValue = (*env)->GetMethodID(env, class, "doubleValue", "()D");

	return (*env)->CallDoubleMethod(env, object, doubleValue);
}
