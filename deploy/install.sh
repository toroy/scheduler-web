#!/bin/bash

base_dir=$(cd `dirname $0`;pwd)
project_root_dir=${base_dir}/../
dist_dir=${project_root_dir}/dist
common_dir=${base_dir}/common

source ${common_dir}/common.sh


install_dir=""
if [[ $# = 1 ]]; then
    echo -e "unknown param $1"
    echo -e "usage: sh `basename $0` --install_dir [install_dir]"
    echo -e "       --install_dir：指定安装服务的根目录"
    echo -e "       eg: ./`basename $0` --install_dir /opt/scheduler"
    exit 1
fi
while [[ $# -ge 2 ]];do
    case "$1" in
            "--install_dir") install_dir=$2; shift 2;;
            *) echo -e "unknown param $1"
               echo -e "usage: sh `basename $0` --install_dir [install_dir]"
               echo -e "       --install_dir：指定安装服务的根目录"
               echo -e "       eg: ./`basename $0` --install_dir /opt/scheduler"
               exit 1 ; break;;
    esac
done

if [[ -z ${install_dir} ]]; then
    install_dir="${dist_dir}"
fi


install_libs=${install_dir}/scheduler-web/libs
install_sbin=${install_dir}/scheduler-web/sbin
service_monitor=${install_sbin}/monitor
create_dir ${install_libs}
create_dir ${install_sbin}/common
create_dir ${service_monitor}

cp ${project_root_dir}/scheduler-web-server/target/scheduler-web-server.jar ${install_libs}
cp -r ${common_dir}/* ${install_sbin}/common/
cp ${base_dir}/*daemon.sh ${install_sbin}/
cp ${base_dir}/monitor/* ${service_monitor}


cd ${install_dir}  && tar -czvf scheduler-web-server.gz scheduler-web/ && cd ${base_dir}