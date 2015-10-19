/*
 * Guidance.c
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

#include <math.h>
#include "Guidance.h"
#include "Guidance_private.h"
#include "Guidance_dt.h"

/* user code (top of parameter file) */
const int_T gblNumToFiles = 0;
const int_T gblNumFrFiles = 0;
const int_T gblNumFrWksBlocks = 0;
const char *gblSlvrJacPatternFileName =
  "Guidance_rsim_rtw\\Guidance_Jpattern.mat";

/* Root inports information  */
const int_T gblNumRootInportBlks = 4;
const int_T gblNumModelInputs = 4;
extern rtInportTUtable *gblInportTUtables;
extern const char *gblInportFileName;
const int_T gblInportDataTypeIdx[] = { 0, 0, 0, 8 };

const int_T gblInportDims[] = { 1, 1, 1, 1, 1, 1, 1, 1 } ;

const int_T gblInportComplex[] = { 0, 0, 0, 0 };

const int_T gblInportInterpoFlag[] = { 0, 0, 0, 0 };

const int_T gblInportContinuous[] = { 0, 0, 1, 0 };

#include "simstruc.h"
#include "fixedpoint.h"

/* Named constants for Chart: '<S1>/Guidance Processor (Updated @100Hz)' */
#define Guidance_CALL_EVENT            (-1)
#define Guidance_IN_Abort              ((uint8_T)1U)
#define Guidance_IN_Armed              ((uint8_T)1U)
#define Guidance_IN_Blind_Range        ((uint8_T)2U)
#define Guidance_IN_Inert              ((uint8_T)2U)
#define Guidance_IN_NO_ACTIVE_CHILD    ((uint8_T)0U)
#define Guidance_IN_Radar_Guided       ((uint8_T)3U)
#define Guidance_IN_Target_Lock        ((uint8_T)4U)
#define Guidance_IN_Target_Search      ((uint8_T)5U)
#define Guidance_entry_to_Target_Search (11)
#define Guidance_event_Timeout         (0)

/* Block signals (auto storage) */
B rtB;

/* Block states (auto storage) */
DW rtDW;

/* Previous zero-crossings (trigger) states */
PrevZCX rtPrevZCX;

/* External inputs (root inport signals with auto storage) */
ExtU rtU;

/* External outputs (root outports fed by signals with auto storage) */
ExtY rtY;

/* Parent Simstruct */
static SimStruct model_S;
SimStruct *const rtS = &model_S;

/* Forward declaration for local functions */
static void Guidance_Fuze(void);

/* Function for Chart: '<S1>/Guidance Processor (Updated @100Hz)' */
static void Guidance_Fuze(void)
{
  /* During 'Fuze': '<S3>:7' */
  switch (rtDW.is_Fuze) {
   case Guidance_IN_Armed:
    /* During 'Armed': '<S3>:9' */
    if (rtDW.sfEvent == Guidance_entry_to_Target_Search) {
      /* Transition: '<S3>:18' */
      rtDW.is_Fuze = Guidance_IN_Inert;

      /* Entry 'Inert': '<S3>:8' */
      rtB.Fuze = false;
    }
    break;

   case Guidance_IN_Inert:
    /* During 'Inert': '<S3>:8' */
    if (rtDW.sfEvent == Guidance_event_Timeout) {
      /* Transition: '<S3>:19' */
      rtDW.is_Fuze = Guidance_IN_Armed;

      /* Entry 'Armed': '<S3>:9' */
      rtB.Fuze = true;
    } else {
      if ((rtDW.is_Guidance == Guidance_IN_Radar_Guided) && (rtU.Rm < 1000.0)) {
        /* Transition: '<S3>:20' */
        rtDW.is_Fuze = Guidance_IN_Armed;

        /* Entry 'Armed': '<S3>:9' */
        rtB.Fuze = true;
      }
    }
    break;

   default:
    /* Unreachable state, for coverage only */
    rtDW.is_Fuze = Guidance_IN_NO_ACTIVE_CHILD;
    break;
  }
}

/* Initial conditions for root system: '<Root>' */
void MdlInitialize(void)
{
  /* InitializeConditions for UnitDelay: '<S1>/0.01 Sec Hold' */
  rtDW.u1SecHold_DSTATE = rtP.u1SecHold_InitialCondition;

  /* InitializeConditions for Chart: '<S1>/Guidance Processor (Updated @100Hz)' */
  rtDW.sfEvent = Guidance_CALL_EVENT;
  rtDW.is_active_Fuze = 0U;
  rtDW.is_Fuze = Guidance_IN_NO_ACTIVE_CHILD;
  rtDW.is_active_Guidance = 0U;
  rtDW.is_Guidance = Guidance_IN_NO_ACTIVE_CHILD;
  rtDW.is_active_c1_Guidance = 0U;
  rtDW.Acquire_time = 0.0;
  rtDW.incr = 0.0;
  rtB.Detonate = false;
  rtB.Fuze = false;
  rtB.Sigma_d = 0.0;
  rtB.Mode = 0U;
  rtB.az_fix = -9.81;
}

/* Start for root system: '<Root>' */
void MdlStart(void)
{
  /* Start for Enabled SubSystem: '<S1>/Fuze' */
  rtDW.Fuze_MODE = false;

  /* End of Start for SubSystem: '<S1>/Fuze' */

  /* InitializeConditions for Enabled SubSystem: '<S1>/Fuze' */
  /* InitializeConditions for Memory: '<S2>/Memory' */
  rtDW.Memory_PreviousInput = rtP.Memory_X0;

  /* InitializeConditions for Derivative: '<S2>/Derivative' */
  rtDW.TimeStampA = (rtInf);
  rtDW.TimeStampB = (rtInf);

  /* End of InitializeConditions for SubSystem: '<S1>/Fuze' */
  /* Start for ToWorkspace: '<S1>/To Workspace1' */
  {
    static int_T rt_ToWksWidths[] = { 1 };

    static int_T rt_ToWksNumDimensions[] = { 1 };

    static int_T rt_ToWksDimensions[] = { 1 };

    static boolean_T rt_ToWksIsVarDims[] = { 0 };

    static void *rt_ToWksCurrSigDims[] = { (NULL) };

    static int_T rt_ToWksCurrSigDimsSize[] = { 4 };

    static BuiltInDTypeId rt_ToWksDataTypeIds[] = { SS_UINT8 };

    static int_T rt_ToWksComplexSignals[] = { 0 };

    static int_T rt_ToWksFrameData[] = { 0 };

    static const char_T *rt_ToWksLabels[] = { "" };

    static RTWLogSignalInfo rt_ToWksSignalInfo = {
      1,
      rt_ToWksWidths,
      rt_ToWksNumDimensions,
      rt_ToWksDimensions,
      rt_ToWksIsVarDims,
      rt_ToWksCurrSigDims,
      rt_ToWksCurrSigDimsSize,
      rt_ToWksDataTypeIds,
      rt_ToWksComplexSignals,
      rt_ToWksFrameData,

      { rt_ToWksLabels },
      (NULL),
      (NULL),
      (NULL),

      { (NULL) },

      { (NULL) },
      (NULL),
      (NULL)
    };

    static const char_T rt_ToWksBlockName[] = "Guidance/Guidance/To Workspace1";
    rtDW.ToWorkspace1_PWORK.LoggedData = rt_CreateStructLogVar(
      ssGetRTWLogInfo(rtS),
      0.0,
      ssGetTFinal(rtS),
      ssGetStepSize(rtS),
      (&ssGetErrorStatus(rtS)),
      "Mode",
      1,
      0,
      1,
      0.01,
      &rt_ToWksSignalInfo,
      rt_ToWksBlockName);
    if (rtDW.ToWorkspace1_PWORK.LoggedData == (NULL))
      return;
  }

  MdlInitialize();
}

/* Outputs for root system: '<Root>' */
void MdlOutputs(int_T tid)
{
  int32_T b_previousEvent;
  real_T *lastU;
  ZCEventType zcEvent;
  real_T rtb_Derivative;

  /* Read data from the mat file of inport block */
  if (gblInportFileName != (NULL)) {
    real_T time = ssGetT(rtS);
    int_T currTimeIdx;
    int_T i;

    /*
     *  Read in data from mat file for root inport Sigmadot
     */
    if (gblInportTUtables[0].nTimePoints > 0) {
      {
        currTimeIdx = rt_getTimeIdx(gblInportTUtables[0].time, time,
          gblInportTUtables[0].nTimePoints,
          gblInportTUtables[0].currTimeIdx,
          0,
          0);
        gblInportTUtables[0].currTimeIdx = currTimeIdx;
        for (i = 0; i < 1; i++) {
          if (currTimeIdx == -7) {
            rtU.Sigmadot = 0.0;
          } else {
            real_T* realPtr1 = (real_T*)gblInportTUtables[0].ur +
              i*gblInportTUtables[0].nTimePoints +currTimeIdx;
            rtU.Sigmadot = *realPtr1;
          }
        }
      }
    }

    /*
     *  Read in data from mat file for root inport Vc
     */
    if (gblInportTUtables[1].nTimePoints > 0) {
      {
        currTimeIdx = rt_getTimeIdx(gblInportTUtables[1].time, time,
          gblInportTUtables[1].nTimePoints,
          gblInportTUtables[1].currTimeIdx,
          0,
          0);
        gblInportTUtables[1].currTimeIdx = currTimeIdx;
        for (i = 0; i < 1; i++) {
          if (currTimeIdx == -7) {
            rtU.Vc = 0.0;
          } else {
            real_T* realPtr1 = (real_T*)gblInportTUtables[1].ur +
              i*gblInportTUtables[1].nTimePoints +currTimeIdx;
            rtU.Vc = *realPtr1;
          }
        }
      }
    }

    /*
     *  Read in data from mat file for root inport Rm
     */
    if (gblInportTUtables[2].nTimePoints > 0) {
      {
        currTimeIdx = rt_getTimeIdx(gblInportTUtables[2].time, time,
          gblInportTUtables[2].nTimePoints,
          gblInportTUtables[2].currTimeIdx,
          0,
          0);
        gblInportTUtables[2].currTimeIdx = currTimeIdx;
        for (i = 0; i < 1; i++) {
          if (currTimeIdx == -7) {
            rtU.Rm = 0.0;
          } else {
            real_T* realPtr1 = (real_T*)gblInportTUtables[2].ur +
              i*gblInportTUtables[2].nTimePoints +currTimeIdx;
            rtU.Rm = *realPtr1;
          }
        }
      }
    }

    /*
     *  Read in data from mat file for root inport Inport1
     */
    if (gblInportTUtables[3].nTimePoints > 0) {
      {
        currTimeIdx = rt_getTimeIdx(gblInportTUtables[3].time, time,
          gblInportTUtables[3].nTimePoints,
          gblInportTUtables[3].currTimeIdx,
          0,
          0);
        gblInportTUtables[3].currTimeIdx = currTimeIdx;
        for (i = 0; i < 1; i++) {
          if (currTimeIdx == -7) {
            rtU.Inport1 = false;
          } else {
            boolean_T* realPtr1 = (boolean_T*)gblInportTUtables[3].ur +
              i*gblInportTUtables[3].nTimePoints +currTimeIdx;
            rtU.Inport1 = *realPtr1;
          }
        }
      }
    }
  }

  /* end read inport data from file */

  /* Chart: '<S1>/Guidance Processor (Updated @100Hz)' incorporates:
   *  Inport: '<Root>/Inport1'
   *  Inport: '<Root>/Rm'
   *  UnitDelay: '<S1>/0.01 Sec Hold'
   */
  /* Gateway: Guidance/Guidance Processor
     (Updated @100Hz) */
  rtDW.sfEvent = Guidance_CALL_EVENT;

  /* During: Guidance/Guidance Processor
     (Updated @100Hz) */
  if (rtDW.is_active_c1_Guidance == 0U) {
    /* Entry: Guidance/Guidance Processor
       (Updated @100Hz) */
    rtDW.is_active_c1_Guidance = 1U;

    /* Entry Internal: Guidance/Guidance Processor
       (Updated @100Hz) */
    rtDW.is_active_Guidance = 1U;

    /* Entry Internal 'Guidance': '<S3>:1' */
    /* Transition: '<S3>:10' */
    rtB.az_fix = -9.81;
    if (rtDW.is_Guidance != Guidance_IN_Target_Search) {
      rtDW.is_Guidance = Guidance_IN_Target_Search;

      /* Entry 'Target_Search': '<S3>:2' */
      rtB.Mode = 1U;
      rtB.Sigma_d = 0.0;
      rtDW.incr = -100.0;
      rtDW.Acquire_time = ssGetTaskTime(rtS,1);
      b_previousEvent = rtDW.sfEvent;
      rtDW.sfEvent = Guidance_entry_to_Target_Search;
      if (rtDW.is_active_Fuze != 0U) {
        Guidance_Fuze();
      }

      rtDW.sfEvent = b_previousEvent;
    }

    rtDW.is_active_Fuze = 1U;

    /* Entry Internal 'Fuze': '<S3>:7' */
    /* Transition: '<S3>:17' */
    if (rtDW.is_Fuze != Guidance_IN_Inert) {
      rtDW.is_Fuze = Guidance_IN_Inert;

      /* Entry 'Inert': '<S3>:8' */
      rtB.Fuze = false;
    }
  } else {
    if (rtDW.is_active_Guidance != 0U) {
      /* During 'Guidance': '<S3>:1' */
      switch (rtDW.is_Guidance) {
       case Guidance_IN_Abort:
        /* During 'Abort': '<S3>:4' */
        break;

       case Guidance_IN_Blind_Range:
        /* During 'Blind_Range': '<S3>:6' */
        break;

       case Guidance_IN_Radar_Guided:
        /* During 'Radar_Guided': '<S3>:5' */
        if (!rtU.Inport1) {
          /* Transition: '<S3>:11' */
          if (rtDW.is_Guidance == Guidance_IN_Radar_Guided) {
            /* Exit 'Radar_Guided': '<S3>:5' */
            rtB.az_fix = rtDW.u1SecHold_DSTATE;
            rtDW.is_Guidance = Guidance_IN_NO_ACTIVE_CHILD;
          }

          if (rtDW.is_Guidance != Guidance_IN_Target_Search) {
            rtDW.is_Guidance = Guidance_IN_Target_Search;

            /* Entry 'Target_Search': '<S3>:2' */
            rtB.Mode = 1U;
            rtB.Sigma_d = 0.0;
            rtDW.incr = -100.0;
            rtDW.Acquire_time = ssGetTaskTime(rtS,1);
            b_previousEvent = rtDW.sfEvent;
            rtDW.sfEvent = Guidance_entry_to_Target_Search;
            if (rtDW.is_active_Fuze != 0U) {
              Guidance_Fuze();
            }

            rtDW.sfEvent = b_previousEvent;
          }
        } else {
          if (rtU.Rm < 200.0) {
            /* Transition: '<S3>:16' */
            if (rtDW.is_Guidance == Guidance_IN_Radar_Guided) {
              /* Exit 'Radar_Guided': '<S3>:5' */
              rtB.az_fix = rtDW.u1SecHold_DSTATE;
              rtDW.is_Guidance = Guidance_IN_NO_ACTIVE_CHILD;
            }

            if (rtDW.is_Guidance != Guidance_IN_Blind_Range) {
              rtDW.is_Guidance = Guidance_IN_Blind_Range;

              /* Entry 'Blind_Range': '<S3>:6' */
              rtB.Mode = 4U;
            }
          }
        }
        break;

       case Guidance_IN_Target_Lock:
        /* During 'Target_Lock': '<S3>:3' */
        if (ssGetTaskTime(rtS,1) - rtDW.Acquire_time > 0.2) {
          /* Transition: '<S3>:15' */
          rtDW.is_Guidance = Guidance_IN_Radar_Guided;

          /* Entry 'Radar_Guided': '<S3>:5' */
          rtB.Mode = 3U;
        }
        break;

       case Guidance_IN_Target_Search:
        /* During 'Target_Search': '<S3>:2' */
        if (rtU.Inport1) {
          /* Transition: '<S3>:14' */
          rtDW.is_Guidance = Guidance_IN_Target_Lock;

          /* Entry 'Target_Lock': '<S3>:3' */
          rtB.Mode = 2U;
          rtDW.Acquire_time = ssGetTaskTime(rtS,1);
        } else if (ssGetTaskTime(rtS,1) - rtDW.Acquire_time > 7.0) {
          /* Transition: '<S3>:13' */
          rtDW.is_Guidance = Guidance_IN_NO_ACTIVE_CHILD;
          b_previousEvent = rtDW.sfEvent;
          rtDW.sfEvent = Guidance_event_Timeout;
          if (rtDW.is_active_Fuze != 0U) {
            Guidance_Fuze();
          }

          rtDW.sfEvent = b_previousEvent;
          rtDW.is_Guidance = Guidance_IN_Abort;

          /* Entry 'Abort': '<S3>:4' */
          rtB.Detonate = true;
        } else {
          rtB.Sigma_d += 0.01 * rtDW.incr;
          if ((rtB.Sigma_d > 30.0) || (rtB.Sigma_d < -30.0)) {
            /* Transition: '<S3>:12' */
            rtDW.incr = -rtDW.incr;
          }
        }
        break;

       default:
        /* Unreachable state, for coverage only */
        rtDW.is_Guidance = Guidance_IN_NO_ACTIVE_CHILD;
        break;
      }
    }

    if (rtDW.is_active_Fuze != 0U) {
      Guidance_Fuze();
    }
  }

  /* End of Chart: '<S1>/Guidance Processor (Updated @100Hz)' */

  /* DataTypeConversion: '<S1>/Data Type Conversion1' */
  rtB.DataTypeConversion1 = rtB.Detonate;

  /* Outputs for Enabled SubSystem: '<S1>/Fuze' incorporates:
   *  EnablePort: '<S2>/Enable'
   */
  if (rtB.Fuze) {
    if (!rtDW.Fuze_MODE) {
      rtDW.Fuze_MODE = true;
    }
  } else {
    if (rtDW.Fuze_MODE) {
      rtDW.Fuze_MODE = false;
    }
  }

  if (rtDW.Fuze_MODE) {
    /* Stop: '<S2>/Stop Simulation' incorporates:
     *  Memory: '<S2>/Memory'
     */
    if (rtDW.Memory_PreviousInput) {
      ssSetStopRequested(rtS, 1);
    }

    /* End of Stop: '<S2>/Stop Simulation' */

    /* Derivative: '<S2>/Derivative' incorporates:
     *  Inport: '<Root>/Rm'
     */
    if ((rtDW.TimeStampA >= ssGetT(rtS)) && (rtDW.TimeStampB >= ssGetT(rtS))) {
      rtb_Derivative = 0.0;
    } else {
      rtb_Derivative = rtDW.TimeStampA;
      lastU = &rtDW.LastUAtTimeA;
      if (rtDW.TimeStampA < rtDW.TimeStampB) {
        if (rtDW.TimeStampB < ssGetT(rtS)) {
          rtb_Derivative = rtDW.TimeStampB;
          lastU = &rtDW.LastUAtTimeB;
        }
      } else {
        if (rtDW.TimeStampA >= ssGetT(rtS)) {
          rtb_Derivative = rtDW.TimeStampB;
          lastU = &rtDW.LastUAtTimeB;
        }
      }

      rtb_Derivative = (rtU.Rm - *lastU) / (ssGetT(rtS) - rtb_Derivative);
    }

    /* End of Derivative: '<S2>/Derivative' */

    /* DataTypeConversion: '<S2>/Data Type Conversion' incorporates:
     *  Constant: '<S4>/Constant'
     *  Logic: '<S2>/Logical Operator'
     *  RelationalOperator: '<S4>/Compare'
     */
    rtB.DataTypeConversion = (rtB.DataTypeConversion1 || (rtb_Derivative >=
      rtP.Constant_Value));

    /* HitCross: '<S2>/Hit  Crossing' */
    zcEvent = rt_ZCFcn(RISING_ZERO_CROSSING,&rtPrevZCX.HitCrossing_Input_ZCE,
                       (rtB.DataTypeConversion - rtP.HitCrossing_Offset));
    if (rtDW.HitCrossing_MODE == 0) {
      if (zcEvent != NO_ZCEVENT) {
        rtB.HitCrossing = !rtB.HitCrossing;
        rtDW.HitCrossing_MODE = 1;
      } else {
        if (rtB.HitCrossing && (rtB.DataTypeConversion != rtP.HitCrossing_Offset))
        {
          rtB.HitCrossing = false;
        }
      }
    } else {
      if (rtB.DataTypeConversion != rtP.HitCrossing_Offset) {
        rtB.HitCrossing = false;
      }

      rtDW.HitCrossing_MODE = 0;
    }

    /* End of HitCross: '<S2>/Hit  Crossing' */

    /* Outputs for Triggered SubSystem: '<S2>/Miss distance' incorporates:
     *  TriggerPort: '<S5>/Trigger'
     */
    if (rtB.HitCrossing && (rtPrevZCX.Missdistance_Trig_ZCE != POS_ZCSIG)) {
      /* Inport: '<S5>/In1' incorporates:
       *  Inport: '<Root>/Rm'
       */
      rtB.In1 = rtU.Rm;
    }

    rtPrevZCX.Missdistance_Trig_ZCE = rtB.HitCrossing;

    /* End of Outputs for SubSystem: '<S2>/Miss distance' */
  }

  /* End of Outputs for SubSystem: '<S1>/Fuze' */

  /* Outport: '<Root>/Outport2' incorporates:
   *  DataTypeConversion: '<S1>/Data Type Conversion2'
   */
  rtY.Outport2 = rtB.Mode;

  /* MultiPortSwitch: '<S1>/Multiport Switch' incorporates:
   *  Gain: '<S1>/Proportional Navigation Gain'
   *  Inport: '<Root>/Sigmadot'
   *  Inport: '<Root>/Vc'
   *  Product: '<S1>/Demands'
   */
  switch (rtB.Mode) {
   case 1:
    rtb_Derivative = rtB.az_fix;
    break;

   case 2:
    rtb_Derivative = rtB.az_fix;
    break;

   case 3:
    rtb_Derivative = rtU.Vc * rtU.Sigmadot * rtP.ProportionalNavigationGain_Gain;
    break;

   default:
    rtb_Derivative = rtB.az_fix;
    break;
  }

  /* End of MultiPortSwitch: '<S1>/Multiport Switch' */

  /* Saturate: '<S1>/Limit Normal Acceleration Demand ' */
  if (rtb_Derivative > rtP.max_acc) {
    rtB.LimitNormalAccelerationDemand = rtP.max_acc;
  } else if (rtb_Derivative < -rtP.max_acc) {
    rtB.LimitNormalAccelerationDemand = -rtP.max_acc;
  } else {
    rtB.LimitNormalAccelerationDemand = rtb_Derivative;
  }

  /* End of Saturate: '<S1>/Limit Normal Acceleration Demand ' */

  /* ToWorkspace: '<S1>/To Workspace1' */
  {
    double locTime = ssGetT(rtS);
    rt_UpdateStructLogVar((StructLogVar *)rtDW.ToWorkspace1_PWORK.LoggedData,
                          &locTime, &rtB.Mode);
  }

  /* Outport: '<Root>/Sigma_d' */
  rtY.Sigma_d = rtB.Sigma_d;

  /* Outport: '<Root>/Az_d' */
  rtY.Az_d = rtB.LimitNormalAccelerationDemand;

  /* Outport: '<Root>/Miss' */
  rtY.Miss = rtB.In1;
  UNUSED_PARAMETER(tid);
}

/* Update for root system: '<Root>' */
void MdlUpdate(int_T tid)
{
  real_T *lastU;

  /* Update for UnitDelay: '<S1>/0.01 Sec Hold' */
  rtDW.u1SecHold_DSTATE = rtB.LimitNormalAccelerationDemand;

  /* Update for Enabled SubSystem: '<S1>/Fuze' incorporates:
   *  Update for EnablePort: '<S2>/Enable'
   */
  if (rtDW.Fuze_MODE) {
    /* Update for Memory: '<S2>/Memory' */
    rtDW.Memory_PreviousInput = rtB.HitCrossing;

    /* Update for Derivative: '<S2>/Derivative' incorporates:
     *  Update for Inport: '<Root>/Rm'
     */
    if (rtDW.TimeStampA == (rtInf)) {
      rtDW.TimeStampA = ssGetT(rtS);
      lastU = &rtDW.LastUAtTimeA;
    } else if (rtDW.TimeStampB == (rtInf)) {
      rtDW.TimeStampB = ssGetT(rtS);
      lastU = &rtDW.LastUAtTimeB;
    } else if (rtDW.TimeStampA < rtDW.TimeStampB) {
      rtDW.TimeStampA = ssGetT(rtS);
      lastU = &rtDW.LastUAtTimeA;
    } else {
      rtDW.TimeStampB = ssGetT(rtS);
      lastU = &rtDW.LastUAtTimeB;
    }

    *lastU = rtU.Rm;

    /* End of Update for Derivative: '<S2>/Derivative' */
  }

  /* End of Update for SubSystem: '<S1>/Fuze' */
  UNUSED_PARAMETER(tid);
}

/* Termination for root system: '<Root>' */
void MdlTerminate(void)
{
}

/* Function to initialize sizes */
void MdlInitializeSizes(void)
{
  ssSetNumContStates(rtS, 0);          /* Number of continuous states */
  ssSetNumY(rtS, 4);                   /* Number of model outputs */
  ssSetNumU(rtS, 4);                   /* Number of model inputs */
  ssSetDirectFeedThrough(rtS, 1);      /* The model is direct feedthrough */
  ssSetNumSampleTimes(rtS, 2);         /* Number of sample times */
  ssSetNumBlocks(rtS, 27);             /* Number of blocks */
  ssSetNumBlockIO(rtS, 12);            /* Number of block outputs */
  ssSetNumBlockParams(rtS, 6);         /* Sum of parameter "widths" */
}

/* Function to initialize sample times. */
void MdlInitializeSampleTimes(void)
{
  /* task periods */
  ssSetSampleTime(rtS, 0, 0.0);
  ssSetSampleTime(rtS, 1, 0.01);

  /* task offsets */
  ssSetOffsetTime(rtS, 0, 0.0);
  ssSetOffsetTime(rtS, 1, 0.0);
}

/* Function to register the model */
SimStruct * Guidance(void)
{
  static struct _ssMdlInfo mdlInfo;
  (void) memset((char *)rtS, 0,
                sizeof(SimStruct));
  (void) memset((char *)&mdlInfo, 0,
                sizeof(struct _ssMdlInfo));
  ssSetMdlInfoPtr(rtS, &mdlInfo);

  /* timing info */
  {
    static time_T mdlPeriod[NSAMPLE_TIMES];
    static time_T mdlOffset[NSAMPLE_TIMES];
    static time_T mdlTaskTimes[NSAMPLE_TIMES];
    static int_T mdlTsMap[NSAMPLE_TIMES];
    static int_T mdlSampleHits[NSAMPLE_TIMES];

    {
      int_T i;
      for (i = 0; i < NSAMPLE_TIMES; i++) {
        mdlPeriod[i] = 0.0;
        mdlOffset[i] = 0.0;
        mdlTaskTimes[i] = 0.0;
        mdlTsMap[i] = i;
        mdlSampleHits[i] = 1;
      }
    }

    ssSetSampleTimePtr(rtS, &mdlPeriod[0]);
    ssSetOffsetTimePtr(rtS, &mdlOffset[0]);
    ssSetSampleTimeTaskIDPtr(rtS, &mdlTsMap[0]);
    ssSetTPtr(rtS, &mdlTaskTimes[0]);
    ssSetSampleHitPtr(rtS, &mdlSampleHits[0]);
  }

  ssSetSolverMode(rtS, SOLVER_MODE_SINGLETASKING);

  /*
   * initialize model vectors and cache them in SimStruct
   */

  /* block I/O */
  {
    ssSetBlockIO(rtS, ((void *) &rtB));
    (void) memset(((void *) &rtB), 0,
                  sizeof(B));

    {
      rtB.LimitNormalAccelerationDemand = 0.0;
      rtB.Sigma_d = 0.0;
      rtB.az_fix = 0.0;
      rtB.DataTypeConversion = 0.0;
      rtB.In1 = 0.0;
    }
  }

  /* external inputs */
  {
    ssSetU(rtS, ((void*) &rtU));
    (void) memset((void *)&rtU, 0,
                  sizeof(ExtU));
    rtU.Sigmadot = 0.0;
    rtU.Vc = 0.0;
    rtU.Rm = 0.0;
  }

  /* external outputs */
  {
    ssSetY(rtS, &rtY);
    rtY.Sigma_d = 0.0;
    rtY.Az_d = 0.0;
    rtY.Miss = 0.0;
    rtY.Outport2 = 0.0;
  }

  /* parameters */
  ssSetDefaultParam(rtS, (real_T *) &rtP);

  /* states (dwork) */
  {
    void *dwork = (void *) &rtDW;
    ssSetRootDWork(rtS, dwork);
    (void) memset(dwork, 0,
                  sizeof(DW));
    rtDW.u1SecHold_DSTATE = 0.0;
    rtDW.Acquire_time = 0.0;
    rtDW.incr = 0.0;
    rtDW.TimeStampA = 0.0;
    rtDW.LastUAtTimeA = 0.0;
    rtDW.TimeStampB = 0.0;
    rtDW.LastUAtTimeB = 0.0;
  }

  /* data type transition information */
  {
    static DataTypeTransInfo dtInfo;
    (void) memset((char_T *) &dtInfo, 0,
                  sizeof(dtInfo));
    ssSetModelMappingInfo(rtS, &dtInfo);
    dtInfo.numDataTypes = 14;
    dtInfo.dataTypeSizes = &rtDataTypeSizes[0];
    dtInfo.dataTypeNames = &rtDataTypeNames[0];

    /* Block I/O transition table */
    dtInfo.B = &rtBTransTable;

    /* Parameters transition table */
    dtInfo.P = &rtPTransTable;
  }

  /* Model specific registration */
  ssSetRootSS(rtS, rtS);
  ssSetVersion(rtS, SIMSTRUCT_VERSION_LEVEL2);
  ssSetModelName(rtS, "Guidance");
  ssSetPath(rtS, "Guidance");
  ssSetTStart(rtS, 0.0);
  ssSetTFinal(rtS, 20.0);
  ssSetStepSize(rtS, 0.01);
  ssSetFixedStepSize(rtS, 0.01);

  /* Setup for data logging */
  {
    static RTWLogInfo rt_DataLoggingInfo;
    ssSetRTWLogInfo(rtS, &rt_DataLoggingInfo);
  }

  /* Setup for data logging */
  {
    rtliSetLogXSignalInfo(ssGetRTWLogInfo(rtS), (NULL));
    rtliSetLogXSignalPtrs(ssGetRTWLogInfo(rtS), (NULL));
    rtliSetLogT(ssGetRTWLogInfo(rtS), "tout");
    rtliSetLogX(ssGetRTWLogInfo(rtS), "");
    rtliSetLogXFinal(ssGetRTWLogInfo(rtS), "");
    rtliSetLogVarNameModifier(ssGetRTWLogInfo(rtS), "rt_");
    rtliSetLogFormat(ssGetRTWLogInfo(rtS), 0);
    rtliSetLogMaxRows(ssGetRTWLogInfo(rtS), 0);
    rtliSetLogDecimation(ssGetRTWLogInfo(rtS), 1);
    rtliSetLogY(ssGetRTWLogInfo(rtS), "");
    rtliSetLogYSignalInfo(ssGetRTWLogInfo(rtS), (NULL));
    rtliSetLogYSignalPtrs(ssGetRTWLogInfo(rtS), (NULL));
  }

  {
    static struct _ssStatesInfo2 statesInfo2;
    ssSetStatesInfo2(rtS, &statesInfo2);
  }

  {
    static ssPeriodicStatesInfo periodicStatesInfo;
    ssSetPeriodicStatesInfo(rtS, &periodicStatesInfo);
  }

  /* previous zero-crossing states */
  {
    ZCSigState *zc = (ZCSigState *) &rtPrevZCX;
    ssSetPrevZCSigState(rtS, zc);
  }

  /* previous zero-crossing states */
  {
    rtPrevZCX.Missdistance_Trig_ZCE = POS_ZCSIG;
    rtPrevZCX.HitCrossing_Input_ZCE = UNINITIALIZED_ZCSIG;
  }

  ssSetChecksumVal(rtS, 0, 2859345741U);
  ssSetChecksumVal(rtS, 1, 3641636079U);
  ssSetChecksumVal(rtS, 2, 239375723U);
  ssSetChecksumVal(rtS, 3, 1104493695U);
  return rtS;
}
