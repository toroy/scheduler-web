#!/bin/bash


base_dir=$(cd `dirname $0`;pwd)
root_dir=${base_dir}/../
logs_dir=${base_dir}/../../logs/monitor/
monitor_log=${logs_dir}/web-server-monitor.`date +'%Y%m'`.log

source ${root_dir}/common/common.sh
create_dir ${logs_dir}


pid=`fetch_pid 'scheduler-web-server.jar'`
if [[ -z "${pid}" ]];then
    log_warn "web server is down , try start up now ..." >> ${monitor_log}
     /bin/bash ${root_dir}/apiserver-daemon.sh "start" >> ${monitor_log}
else
   log_info "WebServer is  running, skip it..." >> ${monitor_log}
fi