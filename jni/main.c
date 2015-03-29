#include "main.h"
void gimp_hsv_to_rgb_int(int* hue, int* saturation, int* value)
{
    double h, s, v, h_temp;
    double f, p, q, t;
    int i;
    if (_f_ocelot_trace(*saturation == 0, _f_ocelot_eq_numeric(*saturation, 0), _f_ocelot_neq_numeric(*saturation, 0))){
        *hue = *value;
        *saturation = *value;
        *value = *value;
    }else{
        h = *hue;
        s = *saturation / 255.0;
        v = *value / 255.0;
        if (_f_ocelot_trace(h == 360, _f_ocelot_eq_numeric(h, 360), _f_ocelot_neq_numeric(h, 360)))
            h_temp = 0;
        else
            h_temp = h;

        h_temp = h_temp / 60.0;
        i = floor(h_temp);
        f = h_temp - i;
        p = v * (1.0 - s);
        q = v * (1.0 - (s * f));
        t = v * (1.0 - (s * (1.0 - f)));
        {
            _f_ocelot_trace_case(5, _f_ocelot_eq_numeric(i, 0), i == 0);
            _f_ocelot_trace_case(0, _f_ocelot_eq_numeric(i, 1), i == 1);
            _f_ocelot_trace_case(1, _f_ocelot_eq_numeric(i, 2), i == 2);
            _f_ocelot_trace_case(2, _f_ocelot_eq_numeric(i, 3), i == 3);
            _f_ocelot_trace_case(3, _f_ocelot_eq_numeric(i, 4), i == 4);
            _f_ocelot_trace_case(4, _f_ocelot_eq_numeric(i, 5), i == 5);
            _f_ocelot_trace_case(6, _f_ocelot_and(_f_ocelot_istrue(1), _f_ocelot_and(_f_ocelot_neq_numeric(i, 0), _f_ocelot_and(_f_ocelot_neq_numeric(i, 1), _f_ocelot_and(_f_ocelot_neq_numeric(i, 2), _f_ocelot_and(_f_ocelot_neq_numeric(i, 3), _f_ocelot_and(_f_ocelot_neq_numeric(i, 4), _f_ocelot_and(_f_ocelot_neq_numeric(i, 5), _f_ocelot_istrue(1)))))))), 1 && i != 0 && i != 1 && i != 2 && i != 3 && i != 4 && i != 5 && 1);
            switch (i){
                case 0:
                    *hue = (int)v * 255.0;
                    *saturation = (int)t * 255.0;
                    *value = (int)p * 255.0;
                    break;
                case 1:
                    *hue = (int)q * 255.0;
                    *saturation = (int)v * 255.0;
                    *value = (int)p * 255.0;
                    break;
                case 2:
                    *hue = (int)p * 255.0;
                    *saturation = (int)v * 255.0;
                    *value = (int)t * 255.0;
                    break;
                case 3:
                    *hue = (int)p * 255.0;
                    *saturation = (int)q * 255.0;
                    *value = (int)v * 255.0;
                    break;
                case 4:
                    *hue = (int)t * 255.0;
                    *saturation = (int)p * 255.0;
                    *value = (int)v * 255.0;
                    break;
                case 5:
                    *hue = (int)v * 255.0;
                    *saturation = (int)p * 255.0;
                    *value = (int)q * 255.0;
                    break;
                default:
                    break;
            }
        }}
}
