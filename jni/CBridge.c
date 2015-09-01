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
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <semaphore.h>

/*
 * Class:     it_unisa_ocelot_simulator_CBridge
 * Method:    getEvents
 * Signature: (Lit/unisa/ocelot/simulator/EventsHandler;)V
 */

void *shm_return;
void *shm_call;

int shared_return_id;
int shared_call_id;

JNIEnv* jnienv;

int childControl;
int parentControl;
int releaser;

pid_t child_processes[OCELOT_CORES];

JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_initialize
		(JNIEnv *env, jobject self, jint values, jint arrays, jint pointers) {
	//***********************************************************************
	// SHARED MEMORY
	//***********************************************************************
	key_t return_id = (int)(rand()*9999);
	key_t call_id = shared_return_id + 1;
	int size;

	/*
	 * Create the segment.
	 */

	size = sizeof(int) + sizeof(_T_ocelot_event) * MAX_EVENTS_NUMBER;
	shared_return_id = shmget(return_id, size, IPC_CREAT|IPC_EXCL|0666);
	if (shared_return_id < 0) {
		char message[100];
		sprintf(&message, "Unable to open RETURN shared memory. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		return;
	}

	size = sizeof(int) + sizeof(int)*3 + sizeof(double)*(int)values + sizeof(double)*(int)arrays*OCELOT_ARRAYS_SIZE + sizeof(double)*(int)pointers;
	shared_call_id = shmget(call_id, size, IPC_CREAT|IPC_EXCL|0666);
	if (shared_call_id < 0) {
		char message[100];
		sprintf(&message, "Unable to open CALL shared memory. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		return;
	}


	FILE* fileCC = fopen ("/tmp/.ocelot_c.lock", "w");
	FILE* filePC = fopen ("/tmp/.ocelot_p.lock", "w");
	FILE* fileRC = fopen ("/tmp/.ocelot_r.lock", "w");

    //childControl = open("/tmp/.ocelot_c.lock", O_CREAT | O_RDWR, 0666);
    //parentControl = open("/tmp/.ocelot_p.lock", O_CREAT | O_RDWR, 0666);
    //releaser = open("/tmp/.ocelot_r.lock", O_CREAT | O_RDWR, 0666);

	childControl = fileno(fileCC);
	parentControl = fileno(filePC);
	releaser = fileno(fileRC);

    if (childControl < 0 || parentControl < 0 || releaser < 0) {
    	char message[100];
		sprintf(&message, "Unable set the locks. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		return;
    }

	child_processes[0] = _f_ocelot_fork(env);

	LOCK_CHILD;
}

JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_memoryDump(JNIEnv *env, jobject self) {
	int i;
	shm_call = shmat(shared_call_id, 0, 0);
	shm_return = shmat(shared_return_id, 0, SHM_RDONLY);

	_T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);
	_T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);

	fprintf(stderr, "------------ START MEMORY DUMP ------------\n");
	fprintf(stderr, "*** CALL MEMORY ***\n");
	fprintf(stderr, "SIGNAL: %d\n", MEMGET(call_memory.signal));
	fprintf(stderr, "VALUES: %d\n", MEMGET(call_memory.values));
	fprintf(stderr, "ARRAYS: %d\n", MEMGET(call_memory.arrays));
	fprintf(stderr, "POINTERS: %d\n", MEMGET(call_memory.pointers));
	fprintf(stderr, "DATA:\n");
	char* data = (char*)call_memory.data;
	for (i = 0; i < (MEMGET(call_memory.values))+(MEMGET(call_memory.arrays))*OCELOT_ARRAYS_SIZE+(MEMGET(call_memory.pointers));i++) {
		fprintf(stderr, "%d ", *(data+i));
	}

	fprintf(stderr, "\n\n");

	fprintf(stderr, "*** RETURN MEMORY ***\n");
	fprintf(stderr, "SIZE: %d\n", MEMGET(return_memory.size));

	data = (char*)return_memory.events;
	for (i = 0; i < (MEMGET(return_memory.size))*sizeof(_T_ocelot_event); i++) {
		fprintf(stderr, "%d ", *(data+i));
	}
	fprintf(stderr, "\n\n");
	fprintf(stderr, "------------  END MEMORY DUMP  ------------\n");

close:
	shmdt(shm_call);
	shmdt(shm_return);
}


JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_getEvents
		(JNIEnv *env, jobject self,
				jobject eventHandler, jdoubleArray values, jobjectArray arrays, jdoubleArray pointers) {

	//Has the lock on the final passage of the child, when he wants to restart the cycle
	WAIT_PASSAGE;
	_f_ocelot_debug("PARENT: Locked releaser\n",0);
	//***********************************************************************
	// JNI STARTUP
	//***********************************************************************
	//Gets the class of the EventHandler instance
	jclass eventHandlerClass = (*env)->GetObjectClass(env, eventHandler);
	jclass cbridge = (*env)->GetObjectClass(env, self);

	jfieldID coreIdField = (*env)->GetFieldID(env, cbridge, "coreId", "I");
	jint coreId = (*env)->GetIntField(env, self, coreIdField);

	//TODO ATTENTION: modify here in order to make everything work properly with multithreading
	coreId = 0;

	//Gets the "add" method of the EventHandler class
	jmethodID addMethod = (*env)->GetMethodID(env, eventHandlerClass, "add", "(IDD)V");
	jmethodID addCaseMethod = (*env)->GetMethodID(env, eventHandlerClass, "addCase", "(IDZ)V");

	//TODO ATTENTION: modify here in order to make everything work properly with multithreading
	// In particular, modify so that shm_call used here is the shm_call specific of coreId process

	shm_call = shmat(shared_call_id, 0, 0);
	shm_return = shmat(shared_return_id, 0, SHM_RDONLY);

	if (shm_call < 0 || shm_return < 0) {
		char message[100];
		sprintf(&message, "Unable to attach one of the shared memory segments. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, &message);
		goto close;
	}

	_T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);

	int lenValues = (*env)->GetArrayLength(env, values);
	int lenArrays = (*env)->GetArrayLength(env, arrays);
	int lenPointers = (*env)->GetArrayLength(env, pointers);
	MEMSET(call_memory.values, lenValues);
	MEMSET(call_memory.arrays, lenArrays);
	MEMSET(call_memory.pointers, lenPointers);

	(*env)->GetDoubleArrayRegion(env, values, 0, MEMGET(call_memory.values),
			call_memory.data);
	int i;
	for (i = 0; i < MEMGET(call_memory.arrays); i++) {
		jdoubleArray currentArray = (jdoubleArray)(*env)->GetObjectArrayElement(env, arrays, i);

		(*env)->GetDoubleArrayRegion(env, currentArray, 0, OCELOT_ARRAYS_SIZE,
				call_memory.data + MEMGET(call_memory.values) + OCELOT_ARRAYS_SIZE*i);
	}

	(*env)->GetDoubleArrayRegion(env, pointers, 0, MEMGET(call_memory.pointers),
			call_memory.data + MEMGET(call_memory.values) + MEMGET(call_memory.arrays)*OCELOT_ARRAYS_SIZE);

	MEMSET(call_memory.signal, OCELOT_SIGNAL_CALL);

	//Releases the lock for the child, which can start its computation
	_f_ocelot_debug("PARENT: unlocking child...\n",0);
	UNLOCK_CHILD;

	_f_ocelot_debug("PARENT: Locking parent...\n",0);
	//Waits the moment when the child finishes and releases the lock of the parent
	LOCK_PARENT;
	_f_ocelot_debug("PARENT: Locked parent\n",0);

	//Locks the child once again: the child is waiting the release of "PASSAGE"
	LOCK_CHILD;
	_f_ocelot_debug("PARENT: Locked child\n",0);

	//Releases the passage. The child waits for the control of the parent.
	RELEASE_PASSAGE;
	_f_ocelot_debug("PARENT: Released passage\n",0);

	int status;
	int signal;
	pid_t update = waitpid(child_processes[coreId], &status, WNOHANG);
	signal = MEMGET(call_memory.signal);

	if (update < 0) {
		_f_ocelot_throw_runtimeexception(env, "An unexpected error occured while waiting for the process to terminate!");

		//Respawn the process
		child_processes[coreId] = _f_ocelot_fork(env);
		goto finally;
	} else if (update > 0) {
		_f_ocelot_throw_runtimeexception(env, "An unexpected error occured in the native code!");

		//Respawn the process
		child_processes[coreId] = _f_ocelot_fork(env);

		goto finally;
	} else {
		if (signal == OCELOT_SIGNAL_RESULT) {
			//times = 0; //Break the cycle
		} else if (signal < 0) {
			if (status == OCELOT_ERR_TOOMANYEVENTS) {
				_f_ocelot_throw_runtimeexception(env, "Events overflow. Please, ensure that there is no infinite loop; try to increase MAX_EVENTS_NUMBER to solve this.");
				goto finally;
			} else {
				_f_ocelot_throw_runtimeexception(env, "An unexpected error occured in the native code!");
				goto finally;
			}
		} else if (signal != OCELOT_SIGNAL_CALL) {
			char msg[1000];
			sprintf(msg, "An unexpected signal in the native code: %d!", signal);
			_f_ocelot_throw_runtimeexception(env, msg);
			goto finally;
		}
	}


	_T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);
	for (i = 0; i < MEMGET(return_memory.size); i++) {
		_T_ocelot_event event = MEMGET(return_memory.events+i);
		if (event.kind == OCELOT_KIND_STDEV) {
			(*env)->CallVoidMethod(env, eventHandler, addMethod, event.choice, event.distanceTrue, event.distanceFalse);
		} else {
			(*env)->CallVoidMethod(env, eventHandler, addCaseMethod, event.choice, event.distanceTrue, (jboolean)event.distanceFalse);
		}
	}

	_f_ocelot_debug("PARENT: All done!\n",0);
finally:
	MEMSET(call_memory.signal, OCELOT_SIGNAL_NOTREADY);

	//Releases the lock of the parent. Now the child is in its initial state (like the parent)
	UNLOCK_PARENT;
	_f_ocelot_debug("PARENT: Unlocked parent!\n",0);
close:
	shmdt(shm_call);
	shmdt(shm_return);
}

/*
 * Utility that returns a useful representation of the shared call memory.
 */
_T_ocelot_call_memory _f_ocelot_shared_call(void* shm) {
	_T_ocelot_call_memory result;

	result.signal = (unsigned char*)shm;
	result.values = (int*)(result.signal + 1);
	result.arrays = (result.values + 1);
	result.pointers = (result.arrays + 1);
	result.data = (double*)(result.pointers+1);

	return result;
}

/*
 * Utility that returns a useful representation of the shared return memory.
 */
_T_ocelot_return_memory _f_ocelot_shared_return(void* shm) {
	_T_ocelot_return_memory result;

	result.size = (int*)shm;
	result.events = (_T_ocelot_event*)(result.size+1);

	return result;
}


/*
 * Forks and creates a child. This function describes the behavoir of the child, who waits for a signal in call memory
 * and, when received, starts the normal procedure.
 */
pid_t _f_ocelot_fork(JNIEnv* env) {
	pid_t pid = fork();
	jnienv = env;

	if (pid == 0) {
		pid_t parent_pid = getppid();
		shm_call = shmat(shared_call_id, 0, 0);
		shm_return = shmat(shared_return_id, 0, 0);

		//Gains control over the parent
		_f_ocelot_debug("CHILD: Locking parent...\n",0);
		LOCK_PARENT;
		_f_ocelot_debug("CHILD: Locked parent!\n",0);

		//Waits the parent to release the control of the child
		LOCK_CHILD;
		_f_ocelot_debug("CHILD: Locked child!\n",0);
		while (kill(parent_pid, 0) == 0) {
			//Note: the check of parent status has to be performed right before the call. This is
			//because it could happen that the resources are released only because the parent
			//process died.
			_f_ocelot_on_signal(0);
			//Control of self not needed anymore
			UNLOCK_CHILD;
			_f_ocelot_debug("CHILD: Unlocked child!\n",0);

			//Releases the control for the parent
			UNLOCK_PARENT;
			_f_ocelot_debug("CHILD: Unlocked parent!\n",0);

			_f_ocelot_debug("CHILD: Waiting passage...\n",0);
			//Waits for the parent to complete the reading
			WAIT_PASSAGE;
			RELEASE_PASSAGE;
			_f_ocelot_debug("CHILD: Passage done!\n",0);

			//Gains control over the parent
			_f_ocelot_debug("CHILD: Locking parent...\n",0);
			LOCK_PARENT;
			_f_ocelot_debug("CHILD: Locked parent!\n",0);

			//Waits that the parent releases the control of self
			LOCK_CHILD;
			_f_ocelot_debug("CHILD: Locked child!\n",0);
		}

		shmdt(shm_call);
		shmdt(shm_return);

		shmctl(shared_call_id, IPC_RMID, NULL);
		shmctl(shared_return_id, IPC_RMID, NULL);

		exit(0);
	}

	return pid;
}

void _f_ocelot_on_signal(int signum) {
	_T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);

	int valuesN = MEMGET(call_memory.values);
	int arraysN = MEMGET(call_memory.arrays);
	int pointersN = MEMGET(call_memory.pointers);

	double* values = call_memory.data;
	double* arrays = call_memory.data + valuesN;
	double* pointers = call_memory.data + valuesN + arraysN*OCELOT_ARRAYS_SIZE;

	int result = _f_ocelot_do_stuff(jnienv, valuesN, arraysN, pointersN, values, arrays, pointers);

	switch (result) {
	case 0:
		MEMSET(call_memory.signal, OCELOT_SIGNAL_RESULT);
		break;
	default:
		MEMSET(call_memory.signal, -result);
		break;
	}
}

int _f_ocelot_do_stuff(JNIEnv* env, int valuesN, int arraysN, int pointersN,
		double* values, double* arrays, double* pointers) {

	_f_ocelot_init();
	int octypes[] = OCELOT_TYPES;
	_f_ocelot_init_arrays(env, arraysN, arrays, octypes);

	EXECUTE_OCELOT_TEST

	if (_v_ocelot_events->len > MAX_EVENTS_NUMBER) {
		_f_ocelot_end();
		return OCELOT_ERR_TOOMANYEVENTS;
	}

	_T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);
	MEMSET(return_memory.size, _v_ocelot_events->len);
	int i;
	for (i = 0; i < _v_ocelot_events->len; i++) {
		_T_ocelot_event event = g_array_index(_v_ocelot_events, _T_ocelot_event, i);

		memcpy(return_memory.events+i, &event, sizeof(_T_ocelot_event));
	}

	_f_ocelot_end();

	return 0;
}

#define OCARRTRANS(type, val) ((type*)(_v_ocelot_pointers[i]))[j] = val;

void _f_ocelot_init_arrays(JNIEnv* env, int arrayN, double* array, int* types) {
	_v_ocelot_pointers = (_t_ocelot_array*)malloc(sizeof(_t_ocelot_array)*arrayN);
	int i, j;
	for (i = 0; i < arrayN; i++) {
		for (j = 0; j < OCELOT_ARRAYS_SIZE; j++) {
			int index = i*OCELOT_ARRAYS_SIZE + j;

			int valueInt = (int)array[index];
			double valueDouble = array[index];
			char valueChar = (char)array[index];

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

void _f_ocelot_throw_runtimeexception(JNIEnv* env, char* message) {
	jclass exClass;
	char* className = "java/lang/RuntimeException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
		return;
	}

	(*env)->ThrowNew(env, exClass, message);
}

void _f_ocelot_debug(char* info, int num) {
	return;
	if (num != 0)
		fprintf(stderr, info, num);
	else
		fprintf(stderr, info);
}

/****************************************************************************************
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
 *  Signal: 0 -> Not ready; 1 -> Call can be performed; 2 -> All completed
 *  Values: Number of values
 *  Arrays: Number of arrays
 *  Pointers: Number of pointers
 *  Data: All the previously declared arrays (values, all the arrays, pointers)
 ***************************************************************************************/
