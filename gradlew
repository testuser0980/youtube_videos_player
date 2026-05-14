#!/bin/sh

##############################################################################
#  Gradle wrapper script
##############################################################################

APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_BASE_NAME=$(basename "$0")

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" -Xmx512m -Xms256m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
