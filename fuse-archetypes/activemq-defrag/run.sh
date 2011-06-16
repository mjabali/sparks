#!/bin/bash
echo example usage:
echo  ./run.sh /opt/FUSE/AMQ/apache-activemq-5.4.2-fuse-00-00/data/kahadb

M2=~/.m2/repository
AMQV=5.4.2-fuse-03-09

echo M2=$M2
echo AMQV=$AMQV

CLASSPATH=$M2/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar
CLASSPATH=$CLASSPATH:$M2/org/apache/activemq/activemq-all/$AMQV/activemq-all-$AMQV.jar
CLASSPATH=$CLASSPATH:$M2/org/apache/activemq/kahadb/$AMQV/kahadb-$AMQV.jar
CLASSPATH=$CLASSPATH:$M2/commons-cli/commons-cli/1.2/commons-cli-1.2.jar
CLASSPATH=$CLASSPATH:$M2/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar
CLASSPATH=$CLASSPATH:$M2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar
CLASSPATH=$CLASSPATH:target/classes

echo $CLASSPATH

java -cp $CLASSPATH com.fusesource.activemq.utils.Main $*

