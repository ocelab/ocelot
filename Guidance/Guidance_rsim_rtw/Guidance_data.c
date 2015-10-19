/*
 * Guidance_data.c
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

#include "Guidance.h"
#include "Guidance_private.h"

/* Block parameters (auto storage) */
P rtP = {
  392.40000000000003,                  /* Variable: max_acc
                                        * Referenced by: '<S1>/Limit Normal Acceleration Demand '
                                        */
  -3.5,                                /* Expression: -3.5
                                        * Referenced by: '<S1>/Proportional Navigation Gain'
                                        */
  0.0,                                 /* Expression: 0
                                        * Referenced by: '<S4>/Constant'
                                        */
  0.0,                                 /* Expression: 0
                                        * Referenced by: '<S2>/Hit  Crossing'
                                        */
  0.0,                                 /* Expression: 0
                                        * Referenced by: '<S1>/0.01 Sec Hold'
                                        */
  0                                    /* Computed Parameter: Memory_X0
                                        * Referenced by: '<S2>/Memory'
                                        */
};
