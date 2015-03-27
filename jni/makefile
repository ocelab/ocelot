CC = gcc

JAVA_HOME = /usr/lib/jvm/java-7-openjdk-amd64

SYSTEM_INCLUDE = /usr/include

GLIB2_INCUDE = -I/usr/lib/x86_64-linux-gnu/glib-2.0/include -I$(SYSTEM_INCLUDE)/glib-2.0 
JAVA_INCLUDE = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux 

CFLAGS = -shared -fpic

INCLUDES = $(GLIB2_INCUDE) $(JAVA_INCLUDE)
LIBS = -lglib-2.0 
SRCS = ocelot.c EN_CBridge.c main.c

MAIN = ../libTest.so

.PHONY: clean all
all:
	$(CC) $(CFLAGS) $(INCLUDES) -o $(MAIN) $(LFLAGS) $(SRCS) $(LIBS)
clean:
	$(RM) $(MAIN)
