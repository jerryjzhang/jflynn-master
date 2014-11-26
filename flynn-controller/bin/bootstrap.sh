#!/bin/sh

main_class="com.tencent.jflynn.boot.JFlynnMain"

# get the path of this app
app_path=`cd "$(dirname "$0")"; cd ..; pwd`

run_cmd="nohup java -cp $app_path/classes -Djava.ext.dirs=$app_path/lib ${main_class}"
echo "start command: $run_cmd"
echo "start..."

$run_cmd