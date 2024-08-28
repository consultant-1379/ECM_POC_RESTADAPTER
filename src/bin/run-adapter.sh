#!/bin/bash
 
LIB_PATH=./libs
CLASSPATH=.:$LIB_PATH/gson-1.7.1.jar:$LIB_PATH/ecmrest-client-0.1.jar:$LIB_PATH/logging.properties
JAVA_MAIN_CLASS=com.ericsson.oss.poc.service.RestAdapter
export appconfig.properties=$LIB_PATH/appconfig.properties
echo "java -cp ${CLASSPATH} -Dappconfig.properties=$LIB_PATH/appconfig.properties  $JAVA_MAIN_CLASS myImg myImg card31"
exec java -cp ${CLASSPATH}  -Dappconfig.properties=$LIB_PATH/appconfig.properties $JAVA_MAIN_CLASS myImg myImg card31
