#!/usr/bin/python

from subprocess import Popen, call
import subprocess
import os
testCases = "ABCDEFGH"

print os.getcwd()


for testCase in testCases:
	print "test case: " + testCase
	#cmd = "ant main -Darg0=Test{} -Derr.out=./output/out{}.txt".format(testCase,testCase).
	f = open("./output/out{}.txt".format(testCase),"w")
	cmd = "java -cp Project_1/Project_1.jar:examples/ ist.meic.pa.KeyConstructors Test{0} 2> output/out{0}.txt".format(testCase)
	#print cmd
	p = Popen(cmd.split(), stderr=subprocess.PIPE)
	output = p.communicate()[1]
	#print output
	f.write(output)
	f.close()
	cmd = "diff examples/out{0}.txt output/out{0}.txt".format(testCase)
	print "diff:"
	call(cmd.split(" "))
	print ""
