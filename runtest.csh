#! /bin/csh -f
#
# Short csh script to setup and run a JUnit test ($Revision: 1.13 $)
#
# Assumes that the program is being run from the "java" build directory.
# Do "ant tests" first to build the necessary classes
#

# run the command
java -noverify \
    -Dsun.java2d.noddraw \
    -Dapple.laf.useScreenMenuBar=true \
    -Dlog4j.ignoreTCL=true \
    -Djava.library.path=.:lib/ \
    -cp .:classes:lib/junit.jar:lib/jdom.jar:lib/jlfgr-1_0.jar \
    $1 $2 $3
