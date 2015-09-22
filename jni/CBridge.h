#include <jni.h>
#include "ocelot.h"
#include "main.h"
#include "pointers.h"

_t_ocelot_array *_v_ocelot_pointers;

/* Header for class it_unisa_ocelot_simulator_CBridge */

#ifndef _Included_it_unisa_ocelot_simulator_CBridge
#define _Included_it_unisa_ocelot_simulator_CBridge
#ifdef __cplusplus
extern "C" {
#endif

#define TYPE_INT 0
#define TYPE_DOUBLE 1
#define TYPE_CHAR 2
#define TYPE_LONG 3
#define TYPE_UNSIGNEDINT 4
#define TYPE_UNSIGNEDDOUBLE 5
#define TYPE_UNSIGNEDCHAR 6
#define TYPE_UNSIGNEDLONG 6

#define MAX_EVENTS_NUMBER 1000
#define TIMEOUT 30
#define TIMEOUT_GRANULARITY 100000

#define OCELOT_ERR_TOOMANYEVENTS 1
#define OCELOT_ERR_UNKNOWN 2

#define OCELOT_SIGNAL_NOTREADY 0
#define OCELOT_SIGNAL_CALL 1
#define OCELOT_SIGNAL_RESULT 1

#define MEMSET(field, value) (*(field)) = value
#define MEMGET(field) (*(field))


//#define LOCK_CHILD() flock(childControl, LOCK_EX)
//#define UNLOCK_CHILD() flock(childControl, LOCK_UN)
//#define LOCK_PARENT() flock(parentControl, LOCK_EX, 0)
//#define UNLOCK_PARENT() flock(parentControl, LOCK_UN, 0)
//#define WAIT_PASSAGE() flock(releaser, LOCK_EX, 0)
//#define RELEASE_PASSAGE() flock(releaser, LOCK_UN, 0)

#define LOCK_CHILD() _f_ocelot_lock(childControl)
#define UNLOCK_CHILD() _f_ocelot_unlock(childControl)
#define LOCK_PARENT() _f_ocelot_lock(parentControl)
#define UNLOCK_PARENT() _f_ocelot_unlock(parentControl)
#define WAIT_PASSAGE() _f_ocelot_lock(releaser)
#define RELEASE_PASSAGE() _f_ocelot_unlock(releaser)

#define OCELOT_SLEEP usleep(1000000/TIMEOUT_GRANULARITY)

typedef struct {
	volatile int *size;
	volatile _T_ocelot_event *events;
} _T_ocelot_return_memory;

typedef struct {
	volatile unsigned char *signal;

	volatile int *values;
	volatile int *arrays;
	volatile int *pointers;

	volatile double *data;
} _T_ocelot_call_memory;

/*
 * Class:     it_unisa_ocelot_simulator_CBridge
 * Method:    getEvents
 * Signature: (Lit/unisa/ocelot/simulator/EventsHandler;[Ljava/lang/Object;[[Ljava/lang/Object;[Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_getEvents(JNIEnv *, jobject, jobject, jdoubleArray, jobjectArray, jdoubleArray);
JNIEXPORT void JNICALL Java_it_unisa_ocelot_simulator_CBridge_initialize(JNIEnv *, jobject, jint, jint, jint);

volatile _T_ocelot_call_memory   _f_ocelot_shared_call(void*);
volatile _T_ocelot_return_memory _f_ocelot_shared_return(void*);

pid_t _f_ocelot_fork(JNIEnv*,int);

int _f_ocelot_alloc_process(JNIEnv*, int, int, int);
void _f_ocelot_on_signal(int);
int _f_ocelot_do_stuff(int,JNIEnv*,int,int,int,double*,double*,double*);
jdouble _f_ocelot_numval(JNIEnv*, jobject);
void _f_ocelot_init_arrays(JNIEnv*, int, double*, int*);
void _f_ocelot_throw_runtimeexception(JNIEnv*, char*);

void _f_ocelot_debug(char* info, int num);

void _f_ocelot_lock(int fd);

void _f_ocelot_unlock(int fd);

//#define OCELOT_ARGUMENT_VALUE(i) (*env)->GetObjectArrayElement(env, values, i)
//#define OCELOT_ARGUMENT_ARRAY(i) (*env)->GetObjectArrayElement(env, arrays, i)
//#define OCELOT_ARGUMENT_POINTER(i) (*env)->GetObjectArrayElement(env, pointers, i)
//#define OCELOT_NUM(object) _f_ocelot_numval(env, object)

#define OCELOT_ARGUMENT_VALUE(i) (*(values + i))
#define OCELOT_ARGUMENT_ARRAY(i) (*(arrays + i))
#define OCELOT_ARGUMENT_POINTER(i) (*(pointers + i))
#define OCELOT_NUM(object) object

#ifdef __cplusplus
}
#endif
#endif
