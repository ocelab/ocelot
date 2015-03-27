void _f_ocelot_testfunction(int* red, int* green, int* blue);

void _f_ocelot_testfunction(int* red, int* green, int* blue)
{
    int r, g, b;
    double h, s, l;
    int min, max;
    int delta;
    r = *red;
    g = *green;
    b = *blue;
    if (_f_ocelot_trace(r > g, _f_ocelot_gt_numeric((double)r, (double)g), _f_ocelot_ge_numeric((double)g, (double)r))){
        max = (r > b ? r : b);
        min = (g < b ? g : b);
    }else{
        max = (g > b ? g : b);
        min = (r < b ? r : b);
    }
    l = (max + min) / 2.0;
    if (_f_ocelot_trace(max == min, _f_ocelot_eq_numeric((double)max, (double)min), _f_ocelot_neq_numeric((double)max, (double)min))){
        s = 0.0;
        h = 0.0;
    }else{
        delta = (max - min);
        if (_f_ocelot_trace(l < 128, _f_ocelot_gt_numeric((double)128, (double)l), _f_ocelot_ge_numeric((double)l, (double)128)))
            s = 255 * (double)delta / (double)(max + min);
        else
            s = 255 * (double)delta / (double)(511 - max - min);

        if (_f_ocelot_trace(r == max, _f_ocelot_eq_numeric((double)r, (double)max), _f_ocelot_neq_numeric((double)r, (double)max)))
            h = (g - b) / (double)delta;
        else
            if (_f_ocelot_trace(g == max, _f_ocelot_eq_numeric((double)g, (double)max), _f_ocelot_neq_numeric((double)g, (double)max)))
                h = 2 + (b - r) / (double)delta;
            else
                h = 4 + (r - g) / (double)delta;

        h = h * 42.5;
        if (_f_ocelot_trace(h < 0, _f_ocelot_gt_numeric((double)0, (double)h), _f_ocelot_ge_numeric((double)h, (double)0)))
            h += 255;
        else
            if (_f_ocelot_trace(h > 255, _f_ocelot_gt_numeric((double)h, (double)255), _f_ocelot_ge_numeric((double)255, (double)h)))
                h -= 255;
    }
    *red = (int)h;
    *green = (int)s;
    *blue = (int)l;
}
