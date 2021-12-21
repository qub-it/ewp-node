#!/bin/bash
#Execute this command inside docker dev env
keytool -v -importkeystore -srckeystore src/main/resources/keystore/localhost.p12 -srcstoretype PKCS12 -srcstorepass p@ssw0rd -destkeystore  $JAVA_HOME/lib/security/cacerts  -deststoretype JKS -deststorepass changeit -noprompt
