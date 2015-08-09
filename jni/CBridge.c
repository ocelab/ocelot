/*
 * ATTENTION:
 * The Java builder (it.unisa.ocelot.runnable.Build) will enrich this meta-c file with:
 * 1) The import if it_unisa_ocelot_simulator_CBridge.h file
 * 2) The definition of EXECUTE_OCELOT_TEST macro
 * Please, do not consider the absence of this part an error.
 */
#include <stdio.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>

/*
 * Class:     it_unisa_ocelot_simulator_CBridge
 * Method:    getEvents
 * Signature: (Lit/unisa/ocelot/simulator/EventsHandler;)V
 */

int *shm_return;
char* shared_return_id;
char* shared_call_id;

JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_initialize
		(JNIEnv *env, jobject self, jint values, jint arrays, jint pointers) {
	//***********************************************************************
	// SHARED MEMORY
	//***********************************************************************
	shared_return_id = "OCELOT RETURN MEM";
	int shmid;

	shm_unlink(shared_return_id);
	int size = sizeof(int) + sizeof(_T_ocelot_event) * MAX_EVENTS_NUMBER;

	/*
	 * Create the segment.
	 */
	shmid = shm_open(shared_return_id, O_CREAT | O_EXCL | O_RDWR, S_IRWXU | S_IRWXG);
	if (shmid < 0) {
		char message[100];
		sprintf(&message, "Unable to open RETURN shared memory. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		return;
	}

	ftruncate(shmid, size);

	/*
	 * Now we attach the segment to our data space.
	 */
	shm_return = (int *) mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, shmid, 0);
	if (shm_return == NULL) {
		_f_ocelot_throw_runtimeexception(env, "Unable to allocate RETURN shared memory.");
		return;
	}




	shared_call_id = "OCELOT CALL MEM";
	shm_unlink(shared_call_id);
	size = sizeof(int) + sizeof(int)*3 + sizeof(double)*values + sizeof(double)*arrays*OCELOT_ARRAYS_SIZE + sizeof(double)*pointers;
	/*
	 * Create the segment.
	 */
	shmid = shm_open(shared_call_id, O_CREAT | O_EXCL | O_RDWR, S_IRWXU | S_IRWXG);
	if (shmid < 0) {
		char message[100];
		sprintf(&message, "Unable to open CALL shared memory. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		return;
	}

	ftruncate(shmid, size);

	/*
	 * Now we attach the segment to our data space.
	 */
	shm_call = (int *) mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, shmid, 0);
	if (shm_call == NULL) {
		_f_ocelot_throw_runtimeexception(env, "Unable to allocate CALL shared memory.");
		return;
	}
}


JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_getEvents
		(JNIEnv *env, jobject self, jobject eventHandler, jobjectArray values, jobjectArray arrays, jobjectArray pointers) {
	//***********************************************************************
	// JNI STARTUP
	//***********************************************************************
	//Gets the class of the EventHandler instance
	jclass class = (*env)->GetObjectClass(env, eventHandler);

	//Gets the "add" method of the EventHandler class
	jmethodID addMethod = (*env)->GetMethodID(env, class, "add", "(IDD)V");
	jmethodID addCaseMethod = (*env)->GetMethodID(env, class, "addCase", "(IDZ)V");

	//***********************************************************************
	// FORK AND WAIT
	//***********************************************************************
	pid_t pid = fork();

	int status;

	if (pid == 0) {
		//The child process runs everything
		_f_ocelot_do_stuff(env, self, eventHandler, values, arrays, pointers, shm_return);
		exit(0);
		return;
	} else {
		//The parent process checks the status of the child periodically
		int times = TIMEOUT * TIMEOUT_GRANULARITY;
		while (times > 0) {
			pid_t update = waitpid(pid, &status, WNOHANG);
			if (update < 0) {
				_f_ocelot_throw_runtimeexception(env, "An unexpected error occured while waiting for the process to terminate!");
				return;
			} else if (update > 0) {
				if (status == OCELOT_ERR_TOOMANYEVENTS) {
					_f_ocelot_throw_runtimeexception(env, "Events overflow. Please, ensure that there is no infinite loop; try to increase MAX_EVENTS_NUMBER to solve this.");
					return;
				} else if (status != 0) {
					_f_ocelot_throw_runtimeexception(env, "An unexpected error occured in the native code!");
					return;
				} else {
					times = 0; //So that it is reduced to -1 before exit, and eventually doesn't cause an exception
				}
			}

			usleep(1000000/TIMEOUT_GRANULARITY);
			times--;
		}

		if (times == 0) {
			_f_ocelot_throw_runtimeexception(env, "Timeout!");
			return;
		}
	}

	int* shr_size = shm_return;
	_T_ocelot_event* shr_events = (_T_ocelot_event*)(shm_return+1);

	int i;
	for (i = 0; i < *shr_size; i++) {
		_T_ocelot_event event = *(shr_events+i);
		if (event.kind == OCELOT_KIND_STDEV) {
			(*env)->CallVoidMethod(env, eventHandler, addMethod, event.choice, event.distanceTrue, event.distanceFalse);
		} else {
			(*env)->CallVoidMethod(env, eventHandler, addCaseMethod, event.choice, event.distanceTrue, (jboolean)event.distanceFalse);
		}
	}

	shm_unlink(shared_return_id);
}

void _f_ocelot_fork() {
	pid_t pid = fork();

	//The child waits until the "Go" signal is sent in the memory.
	if (pid == 0) {

	}
}

void _f_ocelot_throw_runtimeexception(JNIEnv* env, char* message) {
	jclass exClass;
	char* className = "java/lang/RuntimeException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
		return;
	}

	(*env)->ThrowNew(env, exClass, message);
}

void _f_ocelot_do_stuff(JNIEnv *env, jobject self, jobject eventHandler, jobjectArray values, jobjectArray arrays, jobjectArray pointers, int* shm) {
	_f_ocelot_init();
	int octypes[] = OCELOT_TYPES;
	_f_ocelot_init_arrays(env, arrays, octypes);

	EXECUTE_OCELOT_TEST

	int* shr_size = shm;
	_T_ocelot_event* shr_events = ((_T_ocelot_event*)(((int*)shm)+1));

	if (_v_ocelot_events->len > MAX_EVENTS_NUMBER) {
		shm_unlink(shared_return_id);
		exit(OCELOT_ERR_TOOMANYEVENTS);
	}

	*shr_size = _v_ocelot_events->len;
	int i;
	for (i = 0; i < _v_ocelot_events->len; i++) {
		_T_ocelot_event event = g_array_index(_v_ocelot_events, _T_ocelot_event, i);

		memcpy(shr_events+i, &event, sizeof(_T_ocelot_event));
	}

	_f_ocelot_end();

	shm_unlink(shared_return_id);
	exit(0);
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
			char* _v_ocelot_pointers_char = (char*)(_v_ocelot_pointers[i]);

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

/******************************************
 * SHARED MEMORIES
 *
 * RETURN
 *  _____ _____________
 * |SIZE | EVENTS      |
 *  ¯¯¯¯¯ ¯¯¯¯¯¯¯¯¯¯¯¯¯
 *  Size: Number of events (max: MAX_EVENTS_NUMBER)
 *  Events: Array of events
 *
 * CALL
 *  ______ ______ ______ ________ _____________
 * |SIGNAL|VALUES|ARRAYS|POINTERS|DATA         |
 *  ¯¯¯¯¯¯ ¯¯¯¯¯¯ ¯¯¯¯¯¯ ¯¯¯¯¯¯¯¯ ¯¯¯¯¯¯¯¯¯¯¯¯¯
 *
 *  Signal: 0 -> Not ready; 1 -> Ready
 *  Values: Number of values
 *  Arrays: Number of arrays
 *  Pointers: Number of pointers
 *  Data: All the previously declared arrays (values, all the arrays, pointers)
 */
