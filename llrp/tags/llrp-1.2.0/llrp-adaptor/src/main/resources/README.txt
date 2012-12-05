Accada LLRP GUI Client
======================================

HOW TO COMPILE THE XSD:
the AdaptorManager.xsd provides a mapping for the AdaptorManagement how to store and 
retrieve a configuration.

you can compile the xsd for jaxb with xjc
run: $JAVA_HOME/bin/xjc -p org.fosstrak.llrp.adaptor.storage AdaptorManager.xsd