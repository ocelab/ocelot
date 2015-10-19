/*
 * Guidance_dt.h
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

/* data type size table */
static uint_T rtDataTypeSizes[] = {
  sizeof(real_T),
  sizeof(real32_T),
  sizeof(int8_T),
  sizeof(uint8_T),
  sizeof(int16_T),
  sizeof(uint16_T),
  sizeof(int32_T),
  sizeof(uint32_T),
  sizeof(boolean_T),
  sizeof(fcn_call_T),
  sizeof(int_T),
  sizeof(pointer_T),
  sizeof(action_T),
  2*sizeof(uint32_T)
};

/* data type name table */
static const char_T * rtDataTypeNames[] = {
  "real_T",
  "real32_T",
  "int8_T",
  "uint8_T",
  "int16_T",
  "uint16_T",
  "int32_T",
  "uint32_T",
  "boolean_T",
  "fcn_call_T",
  "int_T",
  "pointer_T",
  "action_T",
  "timer_uint32_pair_T"
};

/* data type transitions for block I/O structure */
static DataTypeTransition rtBTransitions[] = {
  { (char_T *)(&rtB.LimitNormalAccelerationDemand), 0, 0, 5 },

  { (char_T *)(&rtB.Mode), 3, 0, 1 },

  { (char_T *)(&rtB.DataTypeConversion1), 8, 0, 4 }
  ,

  { (char_T *)(&rtDW.u1SecHold_DSTATE), 0, 0, 7 },

  { (char_T *)(&rtDW.ToWorkspace1_PWORK.LoggedData), 11, 0, 1 },

  { (char_T *)(&rtDW.sfEvent), 6, 0, 1 },

  { (char_T *)(&rtDW.HitCrossing_MODE), 10, 0, 1 },

  { (char_T *)(&rtDW.is_active_c1_Guidance), 3, 0, 5 },

  { (char_T *)(&rtDW.Memory_PreviousInput), 8, 0, 2 }
};

/* data type transition table for block I/O structure */
static DataTypeTransitionTable rtBTransTable = {
  9U,
  rtBTransitions
};

/* data type transitions for Parameters structure */
static DataTypeTransition rtPTransitions[] = {
  { (char_T *)(&rtP.max_acc), 0, 0, 5 },

  { (char_T *)(&rtP.Memory_X0), 8, 0, 1 }
};

/* data type transition table for Parameters structure */
static DataTypeTransitionTable rtPTransTable = {
  2U,
  rtPTransitions
};

/* [EOF] Guidance_dt.h */
