#!/usr/bin/env bash

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif type -p java > /dev/null 2>&1; then
    JAVA_CMD=$(type -p java)
elif [[ -x "/usr/bin/java" ]];  then
    JAVA_CMD="/usr/bin/java"
else
    echo "Unable to find Java"
    exit 1
fi

FILE_PATH="$0"
while [ -h "$FILE_PATH" ]; do
  ls=`ls -ld "$FILE_PATH"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    FILE_PATH="$link"
  else
    FILE_PATH=`dirname "$FILE_PATH"`/"$link"
  fi
done

FHSP_HOME=`cd $(dirname "$FILE_PATH")/.. > /dev/null; pwd`
LOG_DIR=${FHSP_HOME}/logs
PID_FILE=${FHSP_HOME}/fhsp.pid
JAR_FILE=fhsp.jar

[[ -d "$LOG_DIR" ]] || mkdir ${LOG_DIR} || exit $?

isRunning() {
  ps -p "$1" &> /dev/null
}

start() {
  [[ -f ${PID_FILE} ]] && PID=`cat ${PID_FILE}` && isRunning ${PID} && { echo "Already started."; return 0; }
  ${JAVA_CMD} -jar ${FHSP_HOME}/${JAR_FILE} >> ${LOG_DIR}/$(date '+%Y-%m-%d').log 2>&1 &
  PID=$!
  echo ${PID} > ${PID_FILE}
}

stop() {
  [[ -f ${PID_FILE} ]] || { echo "Not running."; return 0; }
  PID=`cat ${PID_FILE}`
  isRunning ${PID} || { echo "Not running ${PID}."; rm -f ${PID_FILE}; return 0; }
  kill ${PID} &> /dev/null || { echo "Unable to kill process $PID"; return 1; }
  for i in $(seq 1 10); do
    isRunning ${PID} || { echo "Stopped [$PID]"; rm -f ${PID_FILE}; return 0; }
    sleep 1
  done
  echo "Unable to kill process $PID"
  return 1;
}

case "$1" in
start)
  start "$@"; exit $?;;
stop)
  stop "$@"; exit $?;;
*)
  echo "Usage: $0 {start|stop}"; exit 1;
esac

exit 0
