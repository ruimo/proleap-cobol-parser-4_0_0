#!/bin/sh
export CLASSPATH=target/classes
mvn exec:java -Dexec.mainClass="io.proleap.cobol.SimpleCobolTreeViewer" -Dexec.args="$1"
