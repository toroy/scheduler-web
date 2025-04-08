package com.clubfactory.platform.scheduler.web.core.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * property utils
 * single instance
 */
public class PropertyUtils {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static Properties properties;

    private static boolean isInit;

    /**
     * 初始化PropertyUtils
     * @param props
     */
    public static void init(Properties props){
        if (!isInit) {
            isInit = true;
            properties = props;
        }
    }

    /**
     * get property value
     *
     * @param key property name
     * @return
     */
    public static String getString(String key) {
        if (!isInit){
            throw new RuntimeException("PropertyUtils 尚未初始化");
        }
        if (properties == null){
            return null;
        }
        return properties.getProperty(key.trim());
    }

    /**
     * get property value
     *
     * @param key property name
     * @return  get property int value , if key == null, then return -1
     */
    public static int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }
        return defaultValue;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return
     */
    public static Boolean getBoolean(String key) {
        String value = properties.getProperty(key.trim());
        if(null != value){
            return Boolean.parseBoolean(value);
        }

        return null;
    }

    /**
     * get property long value
     * @param key
     * @param defaultVal
     * @return
     */
    public static long getLong(String key, long defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Long.parseLong(val);
    }

    /**
     *
     * @param key
     * @return
     */
    public static long getLong(String key) {
        return getLong(key,-1);
    }

    /**
     *
     * @param key
     * @param defaultVal
     * @return
     */
    public double getDouble(String key, double defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Double.parseDouble(val);
    }


    /**
     *  get array
     * @param key       property name
     * @param splitStr  separator
     * @return
     */
    public static String[] getArray(String key, String splitStr) {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        try {
            String[] propertyArray = value.split(splitStr);
            return propertyArray;
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }
        return null;
    }

    /**
     *
     * @param key
     * @param type
     * @param defaultValue
     * @param <T>
     * @return  get enum value
     */
    public <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                         T defaultValue) {
        String val = getString(key);
        return val == null ? defaultValue : Enum.valueOf(type, val);
    }

    /**
     * get all properties with specified prefix, like: fs.
     * @param prefix prefix to search
     * @return
     */
    public static Map<String, String> getPrefixedProperties(String prefix) {
        Map<String, String> matchedProperties = new HashMap<>();
        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(prefix)) {
                matchedProperties.put(propName, properties.getProperty(propName));
            }
        }
        return matchedProperties;
    }
}
