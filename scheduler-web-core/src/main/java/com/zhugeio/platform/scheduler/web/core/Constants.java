package com.zhugeio.platform.scheduler.web.core;


/**
 * @author xiejiajun
 */
public interface Constants {

    /**
     * fs.defaultFS
     */
    String FS_DEFAULT_FS = "fs.defaultFS";


    /**
     * fs s3a endpoint
     * eg: s3.us-west-2.amazonaws.com
     */
    String FS_S3A_ENDPOINT = "fs.s3a.endpoint";


    /**
     * fs oss 相关
     */
    String FS_OSS_ENDPOINT = "fs.oss.endpoint";

    String FS_OSS_ACCESS_KEY_ID = "fs.oss.accessKeyId";

    String FS_OSS_ACCESS_KEY_SECRET = "fs.oss.accessKeySecret";

    String FS_OSS_SECURITY_TOKEN = "fs.oss.securityToken";

    String FS_OSS_MULTIPART_DOWNLOAD_SIZE = "fs.oss.multipart.download.size";


    /**
     * fs s3a access key
     */
    String FS_S3A_ACCESS_KEY = "fs.s3a.access.key";

    /**
     * fs s3a secret key
     */
    String FS_S3A_SECRET_KEY = "fs.s3a.secret.key";

    /**
     * s3 证书provider
     */
    String FS_S3A_CREDENTIALS_PROVIDER = "fs.s3a.aws.credentials.provider";

    /**
     * 默认s3证书provider
     */
    String FS_S3A_DEFAULT_CREDENTIALS_PROVIDER = "com.amazonaws.auth.DefaultAWSCredentialsProviderChain";


    /**
     * dfs configuration
     * dfs.super.user
     */
    String DFS_SUPER_USER = "dfs.super.user";

    /**
     * dfs configuration
     * data.storage.dfs.base-path
     */
    String DATA_STORAGE_DFS_BASE_PATH = "data.storage.dfs.base-path";

    /**
     * dfs configuration
     * data.storage.dfs.base-path
     */
    String DATA_STORAGE_DFS_FILE_PARAM_PATH = "data.storage.dfs.file-param-path";

    /**
     * 文件上传时本地临时存储路径
     */
    String DATA_UPLOAD_BASEDIR_PATH = "data.upload.basedir.path";

    /**
     * 文件下载时本地临时存储路径
     */
    String DATA_DOWNLOAD_BASEDIR_PATH = "data.download.basedir.path";

    /**
     * scheduler.env.path
     */
    String SCHEDULER_ENV_PATH = "scheduler.env.path";


    /**
     * resource.view.suffixes
     */
    String RESOURCE_VIEW_SUFFIXES = "resource.view.suffixes";

    /**
     * 允许上传的文件后缀
     */
    String RESOURCE_UPLOAD_SUFFIXES = "resource.upload.suffixes";

    String RESOURCE_DEFAULT_ALLOWED_EXTS = "jar,py,sh,sql,hql,xml,properties,conf,cfg,json";

    /**
     * resource.storage.dfs.type
     */
    String RESOURCE_STORAGE_DFS_TYPE = "resource.storage.dfs.type";


    /**
     * comma ,
     */
    String COMMA = ",";

    /**
     * COLON :
     */
    String COLON = ":";

    /**
     * SINGLE_SLASH /
     */
    String SINGLE_SLASH = "/";

    /**
     * DOUBLE_SLASH //
     */
    String DOUBLE_SLASH = "//";

    /**
     * SEMICOLON ;
     */
    String SEMICOLON = ";";

    /**
     * EQUAL SIGN
     */
    String EQUAL_SIGN = "=";

    /**
     * UNDER LINE
     */
    String UNDER_LINE = "_";



    /**
     * date format of yyyy-MM-dd HH:mm:ss
     */
    String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    /**
     * date format of yyyyMMddHHmmss
     */
    String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * http connect time out
     */
    int HTTP_CONNECT_TIMEOUT = 60 * 1000;


    /**
     * http connect request time out
     */
    int HTTP_CONNECTION_REQUEST_TIMEOUT = 60 * 1000;

    /**
     * httpclient soceket time out
     */
    int SOCKET_TIMEOUT = 60 * 1000;


    /**
     * UTF-8
     */
    String UTF_8 = "UTF-8";


    /**
     * java.security.krb5.conf
     */
    String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    /**
     * java.security.krb5.conf.path
     */
    String JAVA_SECURITY_KRB5_CONF_PATH = "java.security.krb5.conf.path";

    /**
     * hadoop.security.authentication
     */
    String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";

    /**
     * hadoop.kerberos.authentication.enable
     */
    String HADOOP_KERBEROS_AUTHENTICATION_ENABLE = "hadoop.kerberos.authentication.enable";


    /**
     * loginUserFromKeytab user
     */
    String LOGIN_USER_KEY_TAB_USERNAME = "login.user.keytab.username";


    /**
     * loginUserFromKeytab path
     */
    String LOGIN_USER_KEY_TAB_PATH = "login.user.keytab.path";

    /**
     * kerberos
     */
    String KERBEROS = "kerberos";


    /**
     * 脚本上传时允许上传的最大bytes
     */
    int maxScriptSize = 1024 * 1024 * 1024;

    int maxFileParamSize = 1024 * 1024 * 1024;

    /**
     * 手机号脱敏字符
     */
    String PHONE_NO_MASK_STR = "****";
    String MINI_PHONE_NO_MASK_STR = "***";

    String TOKEN_KEY = "token";

    String DEFAULT_TOKEN_KEY = "bigdata";
}
