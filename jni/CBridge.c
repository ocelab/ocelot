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
		(JNIEnv *env, jobject self, jobject eventHandler, jobjectArray values, jobjectArray arrays, jobjectArray pointers) {
	//Gets the class of the EventHandler instance
	jclass class = (*env)->GetObjectClass(env, eventHandler);

	//Gets the "add" method of the EventHandler class
	jmethodID addMethod = (*env)->GetMethodID(env, class, "add", "(IDD)V");
	jmethodID addCaseMethod = (*env)->GetMethodID(env, class, "addCase", "(IDZ)V");

	_f_ocelot_init();
	int octypes[] = OCELOT_TYPES;
	_f_ocelot_init_arrays(env, arrays, octypes);

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

	_f_ocelot_end();
}

#define OCARRTRANS(type, val) ((type*)(_v_ocelot_pointers[i]))[j] = val;

void _f_ocelot_init_arrays(JNIEnv* env, jobjectArray array, int* types) {
	jsize length = (*env)->GetArrayLength(env, array);
	_v_ocelot_pointers = (_t_ocelot_array*)malloc(sizeof(_t_ocelot_array)*length);
	int i, j;
	for (i = 0; i < length; i++) {
		jobjectArray variableArray = (jobjectArray) (*env)->GetObjectArrayElement(env, array, i);
		jsize variableArrayLength = (*env)->GetArrayLength(env, variableArray);

		for (j = 0; j < variableArrayLength; j++) {
			int valueInt = (int)_f_ocelot_numval(env, (jobject)(*env)->GetObjectArrayElement(env, variableArray, j));
			double valueDouble = _f_ocelot_numval(env, (jobject)(*env)->GetObjectArrayElement(env, variableArray, j));
			char valueChar = (char)_f_ocelot_numval(env, (jobject)(*env)->GetObjectArrayElement(env, variableArray, j));
			int* _v_ocelot_pointers_int = (int*)(_v_ocelot_pointers[i]);
			double* _v_ocelot_pointers_double = _v_ocelot_pointers[i];
			double* _v_ocelot_pointers_char = (char*)(_v_ocelot_pointers[i]);

			switch (types[j]) {
			case TYPE_INT:
				_v_ocelot_pointers_int[j] = valueInt;
				break;
			case TYPE_DOUBLE:
				_v_ocelot_pointers_double[j] = valueDouble;
				break;
			case TYPE_CHAR:
				_v_ocelot_pointers_char[j] = valueChar;
				break;
			default:
				_v_ocelot_pointers_double[j] = valueDouble;
				break;
			}
		}
	}
}

jdouble _f_ocelot_numval(JNIEnv* env, jobject object) {
	jclass class = (*env)->FindClass(env, "java/lang/Double");

	jmethodID doubleValue = (*env)->GetMethodID(env, class, "doubleValue", "()D");

	return (*env)->CallDoubleMethod(env, object, doubleValue);
}
