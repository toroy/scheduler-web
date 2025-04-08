package com.clubfactory.platform.scheduler.web.server.config;

import com.clubfactory.platform.scheduler.web.core.Constants;
import com.clubfactory.platform.scheduler.web.core.enums.ScriptStorageFSType;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author xiejiajun
 */
@Setter
@Component
public class CommonProperties {

    private final String OSS = "OSS";

    /**
     * 环境变量
     */
    @Value("${scheduler.env.path}")
    private String schedulerEnv;

    /**
     * 文件在线阅览支持
     */
    @Value("${resource.view.suffixes}")
    private String supportViewSuffixes;

    @Value("${resource.upload.suffixes}")
    private String allowUploadExts;

    /**
     * 文件上传下载相关
     */
    @Value("${data.upload.basedir.path}")
    private String uploadBaseDir;

    @Value("${data.download.basedir.path}")
    private String downloadBaseDir;

    /**
     * job资源存储相关
     */
    @Value("${resource.storage.dfs.type}")
    private String resStorageFSType;

    @Value("${dfs.super.user}")
    private String dfsSuperUser;

    @Value("${data.storage.dfs.base-path}")
    private String dfsStorageBaseDir;

    @Value("${fs.defaultFS}")
    private String defaultFS;

    @Value("${fs.s3a.endpoint}")
    private String s3aEndpoint;

    @Value("${fs.s3a.access.key}")
    private String s3aAccessKey;

    @Value("${fs.s3a.secret.key}")
    private String s3aSecretKey;

    @Value("${hadoop.kerberos.authentication.enable}")
    private String kerberosEnable;

    @Value("${java.security.krb5.conf.path}")
    private String krb5ConfPath;

    @Value("${login.user.keytab.username}")
    private String kerberosUser;

    @Value("${login.user.keytab.path}")
    private String keytabPath;

    /**
     * s3a 内部http连接池大小设置：默认15
     */
    @Value("${fs.s3a.connection.maximum}")
    private String s3aConnPoolSize;

    /**
     * s3a 执行命令失败最大重试次数：默认20
     */
    @Value("${fs.s3a.attempts.maximum}")
    private String s3aMaxAttempts;

    /**
     * s3a 建立连接超时时间：默认5000ms
     */
    @Value("${fs.s3a.connection.establish.timeout}")
    private String s3aConnEstablishTimeout;

    /**
     * s3a Socket连接超时时间：默认200000ms
     */
    @Value("${fs.s3a.connection.timeout}")
    private String s3aConnTimeout;

    /**
     * s3a 用于分片并发上传的线程数（这些线程也算在s3a的连接池中）：默认10
     */
    @Value("${fs.s3a.threads.max}")
    private String concurrentUploadThreads;

    /**
     * s3a 线程池中空闲线程等待终止时间,空闲时间超过该值的线程将被关闭：默认60s
     */
    @Value("${fs.s3a.threads.keepalivetime}")
    private String threadKeepAliveTime;

    @Value("${fs.oss.endpoint}")
    private String ossEndpoint;

    @Value("${fs.oss.access-key-id}")
    private String ossAccessKeyId;

    @Value("${fs.oss.access-key-secret}")
    private String ossAccessKeySecret;

    @Value("${fs.oss.security-token}")
    private String ossStsToken;

    @Value("${fs.oss.multipart.download.size}")
    private String ossMultipartDownloadSize;


    public String getS3aConnPoolSize() {
        if (StringUtils.isBlank(s3aConnPoolSize)){
            s3aConnPoolSize = "15";
        }
        return s3aConnPoolSize;
    }

    public String getS3aMaxAttempts() {
        if (StringUtils.isBlank(s3aMaxAttempts)){
            s3aMaxAttempts = "20";
        }
        return s3aMaxAttempts;
    }

    public String getS3aConnEstablishTimeout() {
        if (StringUtils.isBlank(s3aConnEstablishTimeout)){
            s3aConnEstablishTimeout = "5000";
        }
        return s3aConnEstablishTimeout;
    }

    public String getS3aConnTimeout() {
        if (StringUtils.isBlank(s3aConnTimeout)){
            s3aConnTimeout = "200000";
        }
        return s3aConnTimeout;
    }

    public String getConcurrentUploadThreads() {
        if (StringUtils.isBlank(concurrentUploadThreads)){
            concurrentUploadThreads = "10";
        }
        return concurrentUploadThreads;
    }

    public String getThreadKeepAliveTime() {
        if (StringUtils.isBlank(threadKeepAliveTime)){
            threadKeepAliveTime = "60";
        }
        return threadKeepAliveTime;
    }




    public String getSchedulerEnv() {
        return schedulerEnv;
    }

    public String getSupportViewSuffixes() {
        if (StringUtils.isEmpty(supportViewSuffixes)){
            supportViewSuffixes = "txt,log,sh,conf,cfg,py,java,sql,hql,xml,properties";
        }
        supportViewSuffixes = supportViewSuffixes.toLowerCase();
        return supportViewSuffixes;
    }


    public String getUploadBaseDir() {
        return uploadBaseDir;
    }

    public String getDownloadBaseDir() {
        return downloadBaseDir;
    }

    public String getResStorageFSType() {
        if (StringUtils.isEmpty(resStorageFSType)){
            resStorageFSType = ScriptStorageFSType.HDFS.name();
        }
        return resStorageFSType;
    }

    public String getDfsSuperUser() {
        if (StringUtils.isEmpty(dfsSuperUser)){
            dfsSuperUser = "hdfs";
        }
        return dfsSuperUser;
    }

    public String getDfsStorageBaseDir() {
        return dfsStorageBaseDir;
    }

    public String getDefaultFS() {
        return defaultFS;
    }

    public String getS3aEndpoint() {
        return s3aEndpoint;
    }

    public String getS3aAccessKey() {
        return s3aAccessKey;
    }

    public String getS3aSecretKey() {
        return s3aSecretKey;
    }

    public String getKerberosEnable() {
        if (StringUtils.isEmpty(kerberosEnable)){
            kerberosEnable = "false";
        }
        return kerberosEnable;
    }

    public String getKrb5ConfPath() {
        return krb5ConfPath;
    }

    public String getKerberosUser() {
        return kerberosUser;
    }

    public String getKeytabPath() {
        return keytabPath;
    }

    public String getAllowUploadExts() {
        if (StringUtils.isEmpty(allowUploadExts)){
            allowUploadExts = Constants.RESOURCE_DEFAULT_ALLOWED_EXTS;
        }
        allowUploadExts = allowUploadExts.toLowerCase();
        return allowUploadExts;
    }


    public String getOssEndpoint() {
        if (StringUtils.isBlank(ossEndpoint) && OSS.equals(getResStorageFSType())){
            throw new RuntimeException("oss endpoint 未配置");
        }
        return ossEndpoint;
    }

    public String getOssAccessKeyId() {
        if (StringUtils.isBlank(ossAccessKeyId) && OSS.equals(getResStorageFSType())){
            throw new RuntimeException("oss accessKeyId 未配置");
        }
        return ossAccessKeyId;
    }

    public String getOssAccessKeySecret() {
        if (StringUtils.isBlank(ossAccessKeySecret) && OSS.equals(getResStorageFSType())){
            throw new RuntimeException("oss accessKeySecret 未配置");
        }
        return ossAccessKeySecret;
    }

    public String getOssStsToken() {
        return ossStsToken;
    }

    public String getOssMultipartDownloadSize() {
        return ossMultipartDownloadSize;
    }

    /**
     * 将已有配置转换成Properties
     * @return
     */
    public Properties getProperties(){
        Properties props = new Properties();

        props.put(Constants.SCHEDULER_ENV_PATH,this.getSchedulerEnv());
        props.put(Constants.RESOURCE_VIEW_SUFFIXES,this.getSupportViewSuffixes());
        props.put(Constants.DATA_UPLOAD_BASEDIR_PATH,this.getUploadBaseDir());
        props.put(Constants.DATA_DOWNLOAD_BASEDIR_PATH,getDownloadBaseDir());
        props.put(Constants.RESOURCE_STORAGE_DFS_TYPE,getResStorageFSType());
        props.put(Constants.DFS_SUPER_USER,getDfsSuperUser());
        props.put(Constants.DATA_STORAGE_DFS_BASE_PATH,getDfsStorageBaseDir());
        props.put(Constants.FS_DEFAULT_FS,getDefaultFS());
        props.put(Constants.FS_S3A_ENDPOINT,getS3aEndpoint());
        props.put(Constants.FS_S3A_ACCESS_KEY,getS3aAccessKey());
        props.put(Constants.FS_S3A_SECRET_KEY,getS3aSecretKey());
        props.put(Constants.HADOOP_KERBEROS_AUTHENTICATION_ENABLE,getKerberosEnable());
        props.put(Constants.JAVA_SECURITY_KRB5_CONF_PATH,getKrb5ConfPath());
        props.put(Constants.LOGIN_USER_KEY_TAB_USERNAME,getKerberosUser());
        props.put(Constants.LOGIN_USER_KEY_TAB_PATH,getKeytabPath());
        props.put(Constants.RESOURCE_UPLOAD_SUFFIXES,getAllowUploadExts());

        props.put("fs.s3a.connection.maximum",getS3aConnPoolSize());
        props.put("fs.s3a.attempts.maximum",getS3aMaxAttempts());
        props.put("fs.s3a.connection.establish.timeout",getS3aConnEstablishTimeout());
        props.put("fs.s3a.connection.timeout",getS3aConnTimeout());
        props.put("fs.s3a.threads.max",getConcurrentUploadThreads());
        props.put("fs.s3a.threads.keepalivetime",getThreadKeepAliveTime());

        props.put(Constants.FS_OSS_ACCESS_KEY_ID,getOssAccessKeyId());
        props.put(Constants.FS_OSS_ACCESS_KEY_SECRET,getOssAccessKeySecret());
        props.put(Constants.FS_OSS_ENDPOINT,getOssEndpoint());
        props.put(Constants.FS_OSS_SECURITY_TOKEN,getOssStsToken());
        props.put(Constants.FS_OSS_MULTIPART_DOWNLOAD_SIZE,getOssMultipartDownloadSize());
        return props;
    }

}
