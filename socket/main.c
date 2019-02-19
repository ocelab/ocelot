#include <string.h>
#include "ocelot.h"
void case4(char **str)
{
    if(_f_ocelot_trace(str == 0, _f_ocelot_eq_pointer(str, 0), _f_ocelot_neq_pointer(str, 0)))
        return;

    if(_f_ocelot_reg_fcall_numeric(strcmp(str, "Hello"), 3) && _f_ocelot_trace(_f_ocelot_get_fcall() == 0, _f_ocelot_eq_numeric(_f_ocelot_get_fcall(), 0), _f_ocelot_neq_numeric(_f_ocelot_get_fcall(), 0))){
        return;
    }else{
        return;
    }
}

