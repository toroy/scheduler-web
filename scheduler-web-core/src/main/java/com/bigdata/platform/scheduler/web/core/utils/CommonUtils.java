package com.bigdata.platform.scheduler.web.core.utils;

import com.bigdata.platform.scheduler.web.core.Constants;
import com.bigdata.platform.scheduler.web.core.enums.ScriptStorageFSType;
import com.bigdata.platform.scheduler.dal.enums.IEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;


/**
 * common utils
 */
public class CommonUtils {

    /**
     * @return get the path of system environment variables
     */
    public static String getSystemEnvPath() {
        String envPath = PropertyUtils.getString(Constants.SCHEDULER_ENV_PATH);
        if (StringUtils.isEmpty(envPath)) {
            envPath = System.getProperty("user.home") + File.separator + ".bash_profile";
        }

        return envPath;
    }

    public static String getEnumDesc(IEnum IEnum) {
        if (IEnum == null) {
            return null;
        }
        return IEnum.getDesc();
    }


    /**
     * if upload resource is HDFS and kerberos startup is true , else false
     *
     * @return
     */
    public static boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getString(Constants.RESOURCE_STORAGE_DFS_TYPE);
        ScriptStorageFSType resUploadType = ScriptStorageFSType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(Constants.HADOOP_KERBEROS_AUTHENTICATION_ENABLE);
        return resUploadType == ScriptStorageFSType.HDFS && kerberosStartupState;
    }

    /**
     * load kerberos configuration
     *
     * @throws Exception
     */
    public static void loadKerberosConf() throws Exception {
        if (CommonUtils.getKerberosStartupState()) {
            System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF, PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
            Configuration configuration = new Configuration();
            configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
            UserGroupInformation.setConfiguration(configuration);
            UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
        }
    }

}
