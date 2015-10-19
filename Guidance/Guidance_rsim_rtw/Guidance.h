/*
 * Guidance.h
 *
 * Code generation for model "Guidance".
 *
 * Model version              : 1.396
 * Simulink Coder version : 8.8 (R2015a) 09-Feb-2015
 * C source code generated on : Wed Jun 10 17:42:01 2015
 *
 * Target selection: rsim.tlc
 * Note: GRT includes extra infrastructure and instrumentation for prototyping
 * Embedded hardware selection: 32-bit Generic
 * Emulation hardware selection:
 *    Differs from embedded hardware (MATLAB Host)
 * Code generation objectives: Unspecified
 * Validation result: Not run
 */

#ifndef RTW_HEADER_Guidance_h_
#define RTW_HEADER_Guidance_h_
#include <stddef.h>
#include <string.h>
#ifndef Guidance_COMMON_INCLUDES_
# define Guidance_COMMON_INCLUDES_
#include <stdlib.h>
#include "rtwtypes.h"
#include "simstruc.h"
#include "fixedpoint.h"
#include "rsim.h"
#include "rt_logging.h"
#include "dt_info.h"
#endif                                 /* Guidance_COMMON_INCLUDES_ */

#include "Guidance_types.h"

/* Shared type includes */
#include "multiword_types.h"
#include "rtGetInf.h"
#include "rt_nonfinite.h"
#include "rt_zcfcn.h"
#include "rt_defines.h"
#define MODEL_NAME                     Guidance
#define NSAMPLE_TIMES                  (2)                       /* Number of sample times */
#define NINPUTS                        (4)                       /* Number of model inputs */
#define NOUTPUTS                       (4)                       /* Number of model outputs */
#define NBLOCKIO                       (12)                      /* Number of data output port signals */
#define NUM_ZC_EVENTS                  (2)                       /* Number of zero-crossing events */
#ifndef NCSTATES
# define NCSTATES                      (0)                       /* Number of continuous states */
#elif NCSTATES != 0
# error Invalid specification of NCSTATES defined in compiler command
#endif

#ifndef rtmGetDataMapInfo
# define rtmGetDataMapInfo(rtm)        (NULL)
#endif

#ifndef rtmSetDataMapInfo
# define rtmSetDataMapInfo(rtm, val)
#endif

/* Block signals (auto storage) */
typedef struct {
  real_T LimitNormalAccelerationDemand;/* '<S1>/Limit Normal Acceleration Demand ' */
  real_T Sigma_d;                      /* '<S1>/Guidance Processor (Updated @100Hz)' */
  real_T az_fix;                       /* '<S1>/Guidance Processor (Updated @100Hz)' */
  real_T DataTypeConversion;           /* '<S2>/Data Type Conversion' */
  real_T In1;                          /* '<S5>/In1' */
  uint8_T Mode;                        /* '<S1>/Guidance Processor (Updated @100Hz)' */
  boolean_T DataTypeConversion1;       /* '<S1>/Data Type Conversion1' */
  boolean_T Detonate;                  /* '<S1>/Guidance Processor (Updated @100Hz)' */
  boolean_T Fuze;                      /* '<S1>/Guidance Processor (Updated @100Hz)' */
  boolean_T HitCrossing;               /* '<S2>/Hit  Crossing' */
} B;

/* Block states (auto storage) for system '<Root>' */
typedef struct {
  real_T u1SecHold_DSTATE;             /* '<S1>/0.01 Sec Hold' */
  real_T Acquire_time;                 /* '<S1>/Guidance Processor (Updated @100Hz)' */
  real_T incr;                         /* '<S1>/Guidance Processor (Updated @100Hz)' */
  real_T TimeStampA;                   /* '<S2>/Derivative' */
  real_T LastUAtTimeA;                 /* '<S2>/Derivative' */
  real_T TimeStampB;                   /* '<S2>/Derivative' */
  real_T LastUAtTimeB;                 /* '<S2>/Derivative' */
  struct {
    void *LoggedData;
  } ToWorkspace1_PWORK;                /* '<S1>/To Workspace1' */

  int32_T sfEvent;                     /* '<S1>/Guidance Processor (Updated @100Hz)' */
  int_T HitCrossing_MODE;              /* '<S2>/Hit  Crossing' */
  uint8_T is_active_c1_Guidance;       /* '<S1>/Guidance Processor (Updated @100Hz)' */
  uint8_T is_Guidance;                 /* '<S1>/Guidance Processor (Updated @100Hz)' */
  uint8_T is_active_Guidance;          /* '<S1>/Guidance Processor (Updated @100Hz)' */
  uint8_T is_Fuze;                     /* '<S1>/Guidance Processor (Updated @100Hz)' */
  uint8_T is_active_Fuze;              /* '<S1>/Guidance Processor (Updated @100Hz)' */
  boolean_T Memory_PreviousInput;      /* '<S2>/Memory' */
  boolean_T Fuze_MODE;                 /* '<S1>/Fuze' */
} DW;

/* Zero-crossing (trigger) state */
typedef struct {
  ZCSigState HitCrossing_Input_ZCE;    /* '<S2>/Hit  Crossing' */
  ZCSigState Missdistance_Trig_ZCE;    /* '<S2>/Miss distance' */
} PrevZCX;

/* External inputs (root inport signals with auto storage) */
typedef struct {
  real_T Sigmadot;                     /* '<Root>/Sigmadot' */
  real_T Vc;                           /* '<Root>/Vc' */
  real_T Rm;                           /* '<Root>/Rm' */
  boolean_T Inport1;                   /* '<Root>/Inport1' */
} ExtU;

/* External outputs (root outports fed by signals with auto storage) */
typedef struct {
  real_T Sigma_d;                      /* '<Root>/Sigma_d' */
  real_T Az_d;                         /* '<Root>/Az_d' */
  real_T Miss;                         /* '<Root>/Miss' */
  real_T Outport2;                     /* '<Root>/Outport2' */
} ExtY;

/* Parameters (auto storage) */
struct P_ {
  real_T max_acc;                      /* Variable: max_acc
                                        * Referenced by: '<S1>/Limit Normal Acceleration Demand '
                                        */
  real_T ProportionalNavigationGain_Gain;/* Expression: -3.5
                                          * Referenced by: '<S1>/Proportional Navigation Gain'
                                          */
  real_T Constant_Value;               /* Expression: 0
                                        * Referenced by: '<S4>/Constant'
                                        */
  real_T HitCrossing_Offset;           /* Expression: 0
                                        * Referenced by: '<S2>/Hit  Crossing'
                                        */
  real_T u1SecHold_InitialCondition;   /* Expression: 0
                                        * Referenced by: '<S1>/0.01 Sec Hold'
                                        */
  boolean_T Memory_X0;                 /* Computed Parameter: Memory_X0
                                        * Referenced by: '<S2>/Memory'
                                        */
};

extern P rtP;                          /* parameters */

/* External data declarations for dependent source files */
extern const char *RT_MEMORY_ALLOCATION_ERROR;
extern B rtB;                          /* block i/o */
extern DW rtDW;                        /* states (dwork) */
extern PrevZCX rtPrevZCX;              /* prev zc states*/
extern ExtU rtU;                       /* external inputs */
extern ExtY rtY;                       /* external outputs */

/* Simulation Structure */
extern SimStruct *const rtS;

/*-
 * The generated code includes comments that allow you to trace directly
 * back to the appropriate location in the model.  The basic format
 * is <system>/block_name, where system is the system number (uniquely
 * assigned by Simulink) and block_name is the name of the block.
 *
 * Note that this particular code originates from a subsystem build,
 * and has its own system numbers different from the parent model.
 * Refer to the system hierarchy for this subsystem below, and use the
 * MATLAB hilite_system command to trace the generated code back
 * to the parent model.  For example,
 *
 * hilite_system('aero_guidance/Guidance')    - opens subsystem aero_guidance/Guidance
 * hilite_system('aero_guidance/Guidance/Kp') - opens and selects block Kp
 *
 * Here is the system hierarchy for this model
 *
 * '<Root>' : 'aero_guidance'
 * '<S1>'   : 'aero_guidance/Guidance'
 * '<S2>'   : 'aero_guidance/Guidance/Fuze'
 * '<S3>'   : 'aero_guidance/Guidance/Guidance Processor (Updated @100Hz)'
 * '<S4>'   : 'aero_guidance/Guidance/Fuze/Closest Approach'
 * '<S5>'   : 'aero_guidance/Guidance/Fuze/Miss distance'
 */

/* user code (bottom of header file) */
extern const int_T gblNumToFiles;
extern const int_T gblNumFrFiles;
extern const int_T gblNumFrWksBlocks;
extern rtInportTUtable *gblInportTUtables;
extern const char *gblInportFileName;
extern const int_T gblNumRootInportBlks;
extern const int_T gblNumModelInputs;
extern const int_T gblInportDataTypeIdx[];
extern const int_T gblInportDims[];
extern const int_T gblInportComplex[];
extern const int_T gblInportInterpoFlag[];
extern const int_T gblInportContinuous[];

#endif                                 /* RTW_HEADER_Guidance_h_ */
