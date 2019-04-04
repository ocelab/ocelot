#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#ifndef STRBUF_DEFAULT_SIZE
#define STRBUF_DEFAULT_SIZE 1023
#endif
#ifndef STRBUF_DEFAULT_INCREMENT
#define STRBUF_DEFAULT_INCREMENT -2
#endif
#include "ocelot.h"
typedef struct 
{
    char *buf;
    int size;
    int length;
    int increment;
    int dynamic;
    int reallocs;
    int debug;
} strbuf_t;
extern strbuf_t *strbuf_new(int len);
extern void strbuf_init(strbuf_t *s, int len);
extern void strbuf_set_increment(strbuf_t *s, int increment);
extern void strbuf_free(strbuf_t *s);
extern char *strbuf_free_to_string(strbuf_t *s, int *len);
extern void strbuf_resize(strbuf_t *s, int len);
static int strbuf_empty_length(strbuf_t *s);
static int strbuf_length(strbuf_t *s);
static char *strbuf_string(strbuf_t *s, int *len);
static void strbuf_ensure_empty_length(strbuf_t *s, int len);
static char *strbuf_empty_ptr(strbuf_t *s);
static void strbuf_extend_length(strbuf_t *s, int len);
extern void strbuf_append_fmt(strbuf_t *s, int len, const char *fmt, ...);
extern void strbuf_append_fmt_retry(strbuf_t *s, const char *format, ...);
static void strbuf_append_mem(strbuf_t *s, const char *c, int len);
extern void strbuf_append_string(strbuf_t *s, const char *str);
static void strbuf_append_char(strbuf_t *s, const char c);
static void strbuf_ensure_null(strbuf_t *s);
inline static void strbuf_reset(strbuf_t *s)
{
    s->length = 0;
}

inline static int strbuf_allocated(strbuf_t *s)
{
    return s->buf != NULL;
}

inline static int strbuf_empty_length(strbuf_t *s)
{
    return s->size - s->length - 1;
}

inline static void strbuf_ensure_empty_length(strbuf_t *s, int len)
{
    if(len > strbuf_empty_length(s))
        strbuf_resize(s, s->length + len);

}
inline static char *strbuf_empty_ptr(strbuf_t *s)
{
    return s->buf + s->length;
}

inline static void strbuf_extend_length(strbuf_t *s, int len)
{
    s->length += len;
}

inline static int strbuf_length(strbuf_t *s)
{
    return s->length;
}

inline static void strbuf_append_char(strbuf_t *s, const char c)
{
    strbuf_ensure_empty_length(s, 1);
    s->buf[s->length++] = c;
}

inline static void strbuf_append_char_unsafe(strbuf_t *s, const char c)
{
    s->buf[s->length++] = c;
}

inline static void strbuf_append_mem(strbuf_t *s, const char *c, int len)
{
    strbuf_ensure_empty_length(s, len);
    memcpy(s->buf + s->length, c, len);
    s->length += len;
}

inline static void strbuf_append_mem_unsafe(strbuf_t *s, const char *c, int len)
{
    memcpy(s->buf + s->length, c, len);
    s->length += len;
}

inline static void strbuf_ensure_null(strbuf_t *s)
{
    s->buf[s->length] = 0;
}

inline static char *strbuf_string(strbuf_t *s, int *len)
{
    if(len)
        *len = s->length;

    return s->buf;
}
static void die(const char *fmt, ...)
{
    va_list arg;
    va_start(arg, fmt);
    vfprintf(stderr, fmt, arg);
    va_end(arg);
    fprintf(stderr, "\n");
    exit(-1);
}

void strbuf_init(strbuf_t *s, int len)
{
    int size;
    if(len <= 0)
        size = 1023;

    else
        size = len + 1;

    s->buf = NULL;
    s->size = size;
    s->length = 0;
    s->increment = -2;
    s->dynamic = 0;
    s->reallocs = 0;
    s->debug = 0;
    s->buf = malloc(size);
    if(!s->buf)
        die("Out of memory");

    strbuf_ensure_null(s);
}
strbuf_t *strbuf_new(int len)
{
    strbuf_t *s;
    s = malloc(sizeof (strbuf_t));
    if(!s)
        die("Out of memory");

    strbuf_init(s, len);
    s->dynamic = 1;
    return s;
}
void strbuf_set_increment(strbuf_t *s, int increment)
{
    if(increment == 0 || increment == -1)
        die("BUG: Invalid string increment");

    s->increment = increment;
}
inline static void debug_stats(strbuf_t *s)
{
    if(s->debug){
        fprintf(stderr, "strbuf(%lx) reallocs: %d, length: %d, size: %d\n", (long )s, s->reallocs, s->length, s->size);
    }
}

void strbuf_free(strbuf_t *s)
{
    debug_stats(s);
    if(s->buf){
        free(s->buf);
        s->buf = NULL;
    }
    if(s->dynamic)
        free(s);

}
char *strbuf_free_to_string(strbuf_t *s, int *len)
{
    char *buf;
    debug_stats(s);
    strbuf_ensure_null(s);
    buf = s->buf;
    if(len)
        *len = s->length;

    if(s->dynamic)
        free(s);

    return buf;
}
static int calculate_new_size(strbuf_t *s, int len)
{
    int reqsize, newsize;
    if(len <= 0)
        die("BUG: Invalid strbuf length requested");

    reqsize = len + 1;
    if(s->size > reqsize)
        return reqsize;

    newsize = s->size;
    if(s->increment < 0){
        while(newsize < reqsize)
            newsize *= -s->increment;

    }else{
        newsize = ((newsize + s->increment - 1) / s->increment) * s->increment;
    }
    return newsize;
}

void strbuf_resize(strbuf_t *s, int len)
{
    int newsize;
    newsize = calculate_new_size(s, len);
    if(s->debug > 1){
        fprintf(stderr, "strbuf(%lx) resize: %d => %d\n", (long )s, s->size, newsize);
    }
    s->size = newsize;
    s->buf = realloc(s->buf, s->size);
    if(!s->buf)
        die("Out of memory");

    s->reallocs++;
}
void strbuf_append_string(strbuf_t *s, const char *str)
{
    int space, i;
    space = strbuf_empty_length(s);
    for(i = 0;str[i];i++){
        if(space < 1){
            strbuf_resize(s, s->length + 1);
            space = strbuf_empty_length(s);
        }
        s->buf[s->length] = str[i];
        s->length++;
        space--;
    }

}

void strbuf_append_fmt(strbuf_t *s, int len, const char *fmt, ...)
{
    va_list arg;
    int fmt_len;
    strbuf_ensure_empty_length(s, len);
    va_start(arg, fmt);
    fmt_len = vsnprintf(s->buf + s->length, len, fmt, arg);
    va_end(arg);
    if(_f_ocelot_trace(fmt_len < 0, _f_ocelot_lt_numeric(fmt_len, 0), _f_ocelot_ge_numeric(fmt_len, 0)))
        die("BUG: Unable to convert number");

    s->length += fmt_len;
}
void strbuf_append_fmt_retry(strbuf_t *s, const char *fmt, ...)
{
    va_list arg;
    int fmt_len, try;
    int empty_len;
    for(try = 0;try++){
        va_start(arg, fmt);
        empty_len = strbuf_empty_length(s);
        fmt_len = vsnprintf(s->buf + s->length, empty_len + 1, fmt, arg);
        va_end(arg);
        if(fmt_len <= empty_len)
            break;

        if(try > 0)
            die("BUG: length of formatted string changed");

        strbuf_resize(s, s->length + fmt_len);
    }
    s->length += fmt_len;
}

