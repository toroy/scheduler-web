## ClubFactory Scheduler Engine

### 服务部署
- 准备工作
    - jdk8及以上
    - maven 3.5及以上
    - git
- 安装步骤
    1. 下载源码，进入到deploy目录
    2. 执行build.sh脚本进行源码编译
        - 可通过--env选项指定需要选择的SpringBoot profile,不指定的话默认选中dev
            - 例如： ./build.sh --env prod 
    3. 执行install.sh进行服务部署：
        - 可通过--install_dir选项指定服务安装目录，不指定的话默认安装在项目根目录下的dist目录
            - 例如: ./install.sh --install_dir /opt/
    4. 进入安装目录的sbin目录下
        - 通过apiserver-daemon.sh <start|stop|status|restart>脚本启动日志传输服务
        - **注意：** 启动脚本支持传递main方法的命令行参数，直接在启动命令后面传递即可
            - 例如：./apiserver-daemon.sh start --spring.profiles.active=test
       