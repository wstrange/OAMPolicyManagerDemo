OAMPolicyManagerDemo
====================

A Demo of the OAM 11g R2 REST policy management APIs. Uses Jersey client and JAXB.

This is NetBeans Project. It should just open and build.

The OAM policy schema is used to generate JAXB bindings for the various policy objects.  
This schema can be found in your deployment directory for OAM (perform a find ..../domains/IAM -name \*.xsd -print) to locate it. 

A copy of the schema is included in the project file - but this may change with subsequent releases.

To run the project, edit Main.java and change the various parameters for your OAM installation. 

app.txt provides sample input. 

The scripts/ directory contains some useful curl scripts to help when you are 
debugging. 

The demo as configured expects there to be an application domain called "OAMApplication", 
a Host Identifier called "ohs1" and and an Authentication Policy called "DefaultAuthenticationPolicy".
The default data store for LDAP is "OUD".  (yes - these should not be hard coded :-) ). Edit to suit your environment.
