PKCS12 format passphrase: ericsson

NOTE: No need to build the tool to use it. The jar file is stored under /proj/sc-tools/bin/supreme.

Usage:
 - Generate OR/AND install certificates for default targets (all NFs,simulators and some ADP services are currently covered) 
 - Generate OR/AND install multiple custom certificates as defined in a properties file.
 - In any case if the defined CA exists, it is used to sign the new certificate. If it does not exist, it is created and then used to sign the certificate.
 - The installation can be done either to yang provider or secret.
 - cert-info.log: when cert/CA is created, a log file is produced under the same directory with many useful info about the certificate
 - API: the api package can be used for usage by CI, AAT etc.
 - The generated certificates MUST maintain their names, otherwise the tool won't work properly.
 - For a readable format of the output, you can use the /proj/sc-tools/bin/jl tool
 
How to create new version:
- Set the new version on src/main/java/com/ericsson/supreme/Supreme.java log.info
- Set the new version on pom.xml
- Set the new version on Makefile
- Set the new version on CHANGELOG.md and add brief description
- Create new version by running "make clean all publish"

New features/bugs can be handled by the Avengers team.