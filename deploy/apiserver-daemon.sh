#!/bin/bash

base_dir=$(cd `dirname $0`;pwd)
source ${base_dir}/common/common.sh

export SCHEDULER_OPTS="-Xms1024m -Xmx1024m"

case "$1" in
    "start")
      /bin/bash ${base_dir}/scheduler-daemon.sh web-server $@
    ;;
    "stop")
       /bin/bash ${base_dir}/scheduler-daemon.sh web-server $@
    ;;
    "status")
      /bin/bash ${base_dir}/scheduler-daemon.sh web-server $@
    ;;
    "restart")
      /bin/bash ${base_dir}/scheduler-daemon.sh web-server $@
    ;;
    *)
        log_error "Usage: sh `basename $0` <start|stop|status|restart> [debug]"
        exit 1
    ;;
esac




