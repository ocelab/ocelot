
# OCELOT [![Build Status](https://travis-ci.com/ocelab/ocelot.svg?branch=master)](https://travis-ci.com/ocelab/ocelot)

OCELOT is a testing tool capable of generating automatically test data for a C function. It uses JNI in order to call dynamically the C function that will be tested.

OCELOT provides two main runnable classes:
* it.unisa.ocelot.runnable.Build - Instruments the C code, sets up all C source files, creates a makefile and compiles everything. Currently works only on Linux distributions.
* it.unisa.ocelot.runnable.Execute - Runs the a genetic algorithm in order to find the input that can cover a branch in the CFG of the function


# Notes

OCELOT currently requires GLib-2.0.
OCELOT also uses (for some specific algorithms) lp-solve. Please, see the notes in order to install properly the library. 

# Installing notes for lp-solve

http://lpsolve.sourceforge.net/5.5/Java/README.html#install
