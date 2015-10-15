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

#define NODEBUGINFO -324354

#define LOCKNAME_C "/tmp/.ocelot_c%d.lock"
#define LOCKNAME_P "/tmp/.ocelot_p%d.lock"
#define LOCKNAME_R "/tmp/.ocelot_r%d.lock"

//#define LOGGER

#ifdef LOGGER
#define LOG_SEPARATOR(logger) fputs("########################\n\n", logger)
#define LOG_EVENT(event, logger) fprintf(logger, "Event:\n");\
						 fprintf(logger, "Choice: %d\n", event.choice);\
						 fprintf(logger, "distanceTrue: %f\n", event.distanceTrue);\
						 fprintf(logger, "distanceFalse: %f\n", event.distanceFalse);\
						 fputs("---------------\n", logger)
#else
#define LOG_SEPARATOR(logger) {}
#define LOG_EVENT(event, logger) {}

#endif

/*
 * Class:     it_unisa_ocelot_simulator_CBridge
 * Method:    getEvents
 * Signature: (Lit/unisa/ocelot/simulator/EventsHandler;)V
 */

volatile void *shm_return;
volatile void *shm_call;

int shared_return_id;
int shared_call_id;

JNIEnv* jnienv;

int childControl;
int parentControl;
int releaser;

pid_t child_processes;

FILE* loggerParent;
FILE* loggerChild;

JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_privInit
		(JNIEnv *env, jclass cbridge, jint values, jint arrays, jint pointers) {
	int i;

	_f_ocelot_debug("HEY, YOU CALLED ME", NODEBUGINFO);
#ifdef LOGGER
	loggerParent = fopen("OCELOT_LOGGER_P", "w");
	loggerChild = fopen("OCELOT_LOGGER_C", "w");
	fputs("Logger session started.\n", loggerParent);
	fputs("Logger session started.\n", loggerChild);
#endif
	LOG_SEPARATOR(loggerParent);
	LOG_SEPARATOR(loggerChild);

	child_processes = 0;

	int size_call = sizeof(int) + sizeof(int)*3 + sizeof(double)*(int)values + sizeof(double)*(int)arrays*OCELOT_ARRAYS_SIZE + sizeof(double)*(int)pointers;
	int size_return = sizeof(int) + sizeof(_T_ocelot_event) * MAX_EVENTS_NUMBER;

	int pid = _f_ocelot_alloc_process(env, i, size_call, size_return);
	if (pid < 0) {
		char message[100];
		if (pid == -1)
			sprintf(message, "Unable to allocate memory for process %d. Error: %d", i, errno);
		else if (pid == -2)
			sprintf(message, "Unable to gain locks for process %d. Error: %d", i, errno);
		else
			sprintf(message, "Unable to allocate the process %d. Unknown Error.", i);
		_f_ocelot_throw_runtimeexception(env, message);
		goto fail;
	} else {
		_f_ocelot_debug("PID: %d", pid);

		child_processes = pid;
		_f_ocelot_debug("PARENT: Locked child %d", i);
		LOCK_CHILD();
	}

	return;

fail:
	if (child_processes != 0) {
		kill(child_processes, SIGKILL);
	}
	return;
}

int _f_ocelot_alloc_process(JNIEnv* env, int coreId, int size_call, int size_return) {
	int good;
	int tries = 100;

	do {
		tries--;
		good = 1;

		key_t return_id = (int)(rand()*9999);
		key_t call_id = return_id + 1;

		/*
		 * Create the segment.
		 */
		shared_return_id = shmget(return_id, size_return, IPC_CREAT|IPC_EXCL|0666);
		if (shared_return_id < 0) {
			good = 0;
			continue;
		}

		shared_call_id = shmget(call_id, size_call, IPC_CREAT|IPC_EXCL|0666);
		if (shared_call_id < 0) {
			good = 0;
			shmctl(shared_return_id, IPC_RMID, NULL);
			continue;
		}

	} while (!good && tries > 0);

	if (!good){
		return -1;
	}

	char nameC[50];
	char nameP[50];
	char nameR[50];

	sprintf(nameC, LOCKNAME_C,coreId);
	sprintf(nameP, LOCKNAME_P,coreId);
	sprintf(nameR, LOCKNAME_R,coreId);

	FILE* fileCC = fopen (nameC, "w");
	FILE* filePC = fopen (nameP, "w");
	FILE* fileRC = fopen (nameR, "w");

	childControl = fileno(fileCC);
	parentControl = fileno(filePC);
	releaser = fileno(fileRC);

	if (childControl < 0 || parentControl < 0 || releaser < 0) {
		fclose(fileCC);
		fclose(filePC);
		fclose(fileRC);
		return -2;
	}

	return _f_ocelot_fork(env, coreId);
}

JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_memoryDump(JNIEnv *env, jobject self) {
	jclass cbridge = (*env)->GetObjectClass(env, self);

	jfieldID coreIdField = (*env)->GetFieldID(env, cbridge, "coreId", "I");
	jint coreId = (*env)->GetIntField(env, self, coreIdField);

	int i;
	shm_call = shmat(shared_call_id, 0, 0);
	shm_return = shmat(shared_return_id, 0, SHM_RDONLY);

	volatile _T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);
	volatile _T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);

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
	//***********************************************************************
	// JNI STARTUP
	//***********************************************************************
	//Gets the class of the EventHandler instance
	jclass eventHandlerClass = (*env)->GetObjectClass(env, eventHandler);
	jclass cbridge = (*env)->GetObjectClass(env, self);

	jfieldID coreIdField = (*env)->GetFieldID(env, cbridge, "coreId", "I");
	jint coreId = (*env)->GetIntField(env, self, coreIdField);

	//Has the lock on the final passage of the child, when he wants to restart the cycle
	WAIT_PASSAGE();
	_f_ocelot_debug("PARENT: Locked releaser\n",NODEBUGINFO);

	//Gets the "add" method of the EventHandler class
	jmethodID addMethod = (*env)->GetMethodID(env, eventHandlerClass, "add", "(IDD)V");
	jmethodID addCaseMethod = (*env)->GetMethodID(env, eventHandlerClass, "addCase", "(IDZ)V");

	//TODO ATTENTION: modify here in order to make everything work properly with multithreading
	// In particular, modify so that shm_call used here is the shm_call specific of coreId process

	shm_call = shmat(shared_call_id, 0, 0);
	shm_return = shmat(shared_return_id, 0, SHM_RDONLY);

	if (shm_call < 0 || shm_return < 0) {
		char message[100];
		sprintf(message, "OCELOT ERROR: Unable to attach one of the shared memory segments. Error: %d", errno);
		_f_ocelot_throw_runtimeexception(env, message);
		goto close;
	}

	volatile _T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);

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
	_f_ocelot_debug("PARENT: unlocking child...\n",NODEBUGINFO);
	UNLOCK_CHILD();

	_f_ocelot_debug("PARENT: Locking parent...\n",NODEBUGINFO);
	//Waits the moment when the child finishes and releases the lock of the parent
	LOCK_PARENT();
	_f_ocelot_debug("PARENT: Locked parent\n",NODEBUGINFO);

	//Releases the passage. The child waits for the control of the parent.
	RELEASE_PASSAGE();
	_f_ocelot_debug("PARENT: Released passage\n",NODEBUGINFO);

	//Locks the child once again: the child is waiting the release of "PASSAGE"
	LOCK_CHILD();
	_f_ocelot_debug("PARENT: Locked child\n",NODEBUGINFO);

	int status;
	int signal;
	pid_t update = waitpid(child_processes, &status, WNOHANG);
	signal = MEMGET(call_memory.signal);

	if (update < 0) {
		_f_ocelot_throw_runtimeexception(env, "OCELOT ERROR: An unexpected error occured while waiting for the process to terminate!");

		//Respawn the process
		child_processes = _f_ocelot_fork(env, coreId);
		goto finally;
	} else if (update > 0) {
		char message[1000];
		if (WIFEXITED(status)) {
			int exit_status = WEXITSTATUS(status);
			sprintf(message, "OCELOT ERROR: Child process exited unexpectedly. Status: %d", exit_status);
		} else if (WIFSIGNALED(status)) {
			int term_signal = WTERMSIG(status);
			if (!WCOREDUMP(status))
				sprintf(message, "OCELOT ERROR: Child process terminated by a signal. Signal: %d", term_signal);
			else
				sprintf(message, "OCELOT ERROR: Child process terminated by a signal and produced a core dump. Signal: %d", term_signal);
		} else {
			sprintf(message, "OCELOT ERROR: Unknown status. Status: %d", status);
		}

		_f_ocelot_throw_runtimeexception(env, message);

		//Respawn the process
		child_processes = _f_ocelot_fork(env, coreId);

		goto finally;
	} else {
		if (signal == OCELOT_SIGNAL_RESULT) {
			//times = 0; //Break the cycle
		} else if (signal < 0) {
			if (status == OCELOT_ERR_TOOMANYEVENTS) {
				_f_ocelot_throw_runtimeexception(env, "OCELOT ERROR: Events overflow. Please, ensure that there is no infinite loop; try to increase MAX_EVENTS_NUMBER to solve this.");
				goto finally;
			} else {
				_f_ocelot_throw_runtimeexception(env, "OCELOT ERROR: An unexpected error occurred in the native code!");
				goto finally;
			}
		} else if (signal != OCELOT_SIGNAL_CALL) {
			char msg[1000];
			sprintf(msg, "OCELOT ERROR: An unexpected signal in the native code: %d!", signal);
			_f_ocelot_throw_runtimeexception(env, msg);
			goto finally;
		}
	}


	volatile _T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);
	for (i = 0; i < MEMGET(return_memory.size); i++) {
		volatile _T_ocelot_event event = MEMGET(return_memory.events+i);
		LOG_EVENT(event, loggerParent);
		if (event.kind == OCELOT_KIND_STDEV) {
			(*env)->CallVoidMethod(env, eventHandler, addMethod, event.choice, event.distanceTrue, event.distanceFalse);
		} else {
			(*env)->CallVoidMethod(env, eventHandler, addCaseMethod, event.choice, event.distanceTrue, (jboolean)event.distanceFalse);
		}
	}
	LOG_SEPARATOR(loggerParent);

	_f_ocelot_debug("PARENT: All done!\n",NODEBUGINFO);
finally:
	MEMSET(call_memory.signal, OCELOT_SIGNAL_NOTREADY);

	//Releases the lock of the parent. Now the child is in its initial state (like the parent)
	UNLOCK_PARENT();
	_f_ocelot_debug("PARENT: Unlocked parent!\n",NODEBUGINFO);
close:
	if (shmdt(shm_call) < 0) {
		_f_ocelot_throw_runtimeexception(env, "Unable to detach call memory");
	}
	if (shmdt(shm_return) < 0) {
		_f_ocelot_throw_runtimeexception(env, "Unable to detach return memory");
	}
}

/*
 * Utility that returns a useful representation of the shared call memory.
 */
volatile _T_ocelot_call_memory _f_ocelot_shared_call(volatile void* shm) {
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
volatile _T_ocelot_return_memory _f_ocelot_shared_return(volatile void* shm) {
	_T_ocelot_return_memory result;

	result.size = (int*)shm;
	result.events = (_T_ocelot_event*)(result.size+1);

	return result;
}


/*
 * Forks and creates a child. This function describes the behavoir of the child, who waits for a signal in call memory
 * and, when received, starts the normal procedure.
 */
pid_t _f_ocelot_fork(JNIEnv* env, int coreId) {
	pid_t pid = fork();
	jnienv = env;

	if (pid == 0) {
		pid_t parent_pid = getppid();
		shm_call = shmat(shared_call_id, 0, 0);
		shm_return = shmat(shared_return_id, 0, 0);

		//Gains control over the parent
		_f_ocelot_debug("CHILD%d: Locking parent...\n",coreId);
		LOCK_PARENT();
		_f_ocelot_debug("CHILD%d: Locked parent!\n",coreId);

		//Waits the parent to release the control of the child
		LOCK_CHILD();
		_f_ocelot_debug("CHILD%d: Locked child!\n",coreId);
		while (kill(parent_pid, 0) == 0) {
			//Note: the check of parent status has to be performed right before the call. This is
			//because it could happen that the resources are released only because the parent
			//process died.
			_f_ocelot_on_signal(coreId);
			//Control of self not needed anymore

			//Releases the control for the parent
			UNLOCK_PARENT();
			_f_ocelot_debug("CHILD%d: Unlocked parent!\n",coreId);

			_f_ocelot_debug("CHILD%d: Waiting passage...\n",coreId);
			//Waits for the parent to complete the reading
			WAIT_PASSAGE();

			UNLOCK_CHILD();
			_f_ocelot_debug("CHILD%d: Unlocked child!\n",coreId);


			//Gains control over the parent
			_f_ocelot_debug("CHILD%d: Locking parent...\n",coreId);
			LOCK_PARENT();
			_f_ocelot_debug("CHILD%d: Locked parent!\n",coreId);

			RELEASE_PASSAGE();
			_f_ocelot_debug("CHILD%d: Passage done!\n",coreId);

			//Waits that the parent releases the control of self
			LOCK_CHILD();
			_f_ocelot_debug("CHILD%d: Locked child!\n",coreId);
		}

		_f_ocelot_debug("CHILD KILLED!", NODEBUGINFO);

		shmdt(shm_call);
		shmdt(shm_return);

		shmctl(shared_call_id, IPC_RMID, NULL);
		shmctl(shared_return_id, IPC_RMID, NULL);

		exit(0);
	}

	return pid;
}

void _f_ocelot_on_signal(int coreId) {
	volatile _T_ocelot_call_memory call_memory = _f_ocelot_shared_call(shm_call);

	volatile int valuesN = MEMGET(call_memory.values);
	volatile int arraysN = MEMGET(call_memory.arrays);
	volatile int pointersN = MEMGET(call_memory.pointers);

	volatile double* values = call_memory.data;
	volatile double* arrays = call_memory.data + valuesN;
	volatile double* pointers = call_memory.data + valuesN + arraysN*OCELOT_ARRAYS_SIZE;

	int result = _f_ocelot_do_stuff(coreId, jnienv, valuesN, arraysN, pointersN, values, arrays, pointers);

	switch (result) {
	case 0:
		MEMSET(call_memory.signal, OCELOT_SIGNAL_RESULT);
		break;
	default:
		MEMSET(call_memory.signal, -result);
		break;
	}
}

int _f_ocelot_do_stuff(int coreId, JNIEnv* env, int valuesN, int arraysN, int pointersN,
		volatile double* values, volatile double* arrays, volatile double* pointers) {

	_f_ocelot_init();
	int octypes[] = OCELOT_TYPES;
	_f_ocelot_init_arrays(env, arraysN, arrays, octypes);

	EXECUTE_OCELOT_TEST

	if (OCLIST_SIZE(_v_ocelot_events) > MAX_EVENTS_NUMBER) {
		_f_ocelot_end();
		return OCELOT_ERR_TOOMANYEVENTS;
	}

	volatile _T_ocelot_return_memory return_memory = _f_ocelot_shared_return(shm_return);
	MEMSET(return_memory.size, OCLIST_SIZE(_v_ocelot_events));
	int i;
	for (i = 0; i < OCLIST_SIZE(_v_ocelot_events); i++) {
		_T_ocelot_event event = OCLIST_GET(_v_ocelot_events, i, _T_ocelot_event);
		LOG_EVENT(event, loggerChild);
		memcpy(return_memory.events+i, &event, sizeof(_T_ocelot_event));
	}

	LOG_SEPARATOR(loggerChild);

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
			unsigned int valueUInt = (unsigned int)array[index];
			unsigned char valueUChar = (unsigned char)array[index];

			int* _v_ocelot_pointers_int = (int*)(_v_ocelot_pointers[i]);
			double* _v_ocelot_pointers_double = _v_ocelot_pointers[i];
			char* _v_ocelot_pointers_char = (char*)(_v_ocelot_pointers[i]);
			unsigned int* _v_ocelot_pointers_uint = (unsigned int*)(_v_ocelot_pointers[i]);
			unsigned char* _v_ocelot_pointers_uchar = (unsigned char*)(_v_ocelot_pointers[i]);

			switch (types[i]) {
			case TYPE_INT:
				_v_ocelot_pointers_int[j] = valueInt;
				break;
			case TYPE_DOUBLE:
				_v_ocelot_pointers_double[j] = valueDouble;
				break;
			case TYPE_CHAR:
				_v_ocelot_pointers_char[j] = valueChar;
				break;
			case TYPE_UNSIGNEDINT:
				_v_ocelot_pointers_uint[j] = valueUInt;
				break;
			case TYPE_UNSIGNEDCHAR:
				_v_ocelot_pointers_uchar[j] = valueUChar;
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
	if (num != NODEBUGINFO)
		fprintf(stderr, info, num);
	else
		fprintf(stderr, info);
}

void _f_ocelot_lock(int fd) {
	struct flock f1;

	f1.l_type = F_WRLCK;
	f1.l_whence = SEEK_SET;
	f1.l_start = 0;
	f1.l_len = 0;
	f1.l_pid = getpid();

	fcntl(fd, F_SETLKW, &f1);
}

void _f_ocelot_unlock(int fd) {
	struct flock f1;

	f1.l_type = F_UNLCK;
	f1.l_whence = SEEK_SET;
	f1.l_start = 0;
	f1.l_len = 0;
	f1.l_pid = getpid();

	fcntl(fd, F_SETLKW, &f1);
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
