#!/bin/bash
# Run the policy manager demo 

# cmd line args: [-r]  [filename]
# -r remove policies instead of creating
# filename - input file (default is app.txt)
#  
java -jar OAMPolicyManagerDemo.jar $*
