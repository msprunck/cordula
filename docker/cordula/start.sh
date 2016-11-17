#!/bin/sh

/usr/bin/java -Dlog4j.debug -Dlog4j.configuration=file:/log4j.properties \
              -jar /cordula.jar --host 0.0.0.0 --port 3000 "$@"
