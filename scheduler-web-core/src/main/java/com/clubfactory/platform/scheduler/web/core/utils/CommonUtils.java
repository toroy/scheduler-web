package com.clubfactory.platform.scheduler.web.core.utils;

import com.clubfactory.platform.scheduler.dal.enums.IEnum;
import com.clubfactory.platform.scheduler.web.core.Constants;
import com.clubfactory.platform.scheduler.web.core.enums.ScriptStorageFSType;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;

import static com.clubfactory.platform.scheduler.web.core.Constants.*;
import static com.clubfactory.platform.scheduler.web.core.utils.PropertyUtils.getBoolean;
import static com.clubfactory.platform.scheduler.web.core.utils.PropertyUtils.getString;


/**
 * common utils
 */
public class CommonUtils {

    /**
     * @return get the path of system environment variables
     */
    public static String getSystemEnvPath() {
        String envPath = getString(SCHEDULER_ENV_PATH);
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
        String resUploadStartupType = getString(Constants.RESOURCE_STORAGE_DFS_TYPE);
        ScriptStorageFSType resUploadType = ScriptStorageFSType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = getBoolean(Constants.HADOOP_KERBEROS_AUTHENTICATION_ENABLE);
        return resUploadType == ScriptStorageFSType.HDFS && kerberosStartupState;
    }

    /**
     * load kerberos configuration
     *
     * @throws Exception
     */
    public static void loadKerberosConf() throws Exception {
        if (CommonUtils.getKerberosStartupState()) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, getString(JAVA_SECURITY_KRB5_CONF_PATH));
            Configuration configuration = new Configuration();
            configuration.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS);
            UserGroupInformation.setConfiguration(configuration);
            UserGroupInformation.loginUserFromKeytab(getString(LOGIN_USER_KEY_TAB_USERNAME),
                    getString(Constants.LOGIN_USER_KEY_TAB_PATH));
        }
    }

}
