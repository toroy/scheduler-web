package com.zhugeio.platform.scheduler.web.core.utils;

import com.zhugeio.platform.scheduler.web.core.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * 脱敏工具
 * @author xiejiajun
 */
public class MaskUtil {

    /**
     * 姓名脱敏
     * 张xx -> 张**
     * 张x -> 张*
     * @param fullName
     * @return
     */
    public static String maskName(String fullName){
        if (StringUtils.isNotBlank(fullName)) {
            String name = StringUtils.left(fullName, 1);
            return StringUtils.rightPad(name, StringUtils.length(fullName), "*");
        }
        return fullName;
    }

    /**
     * 手机号脱敏，保留前3后4
     * @param phoneNo
     * @return
     */
    @Deprecated
    public static String maskPhoneNo(String phoneNo) {
        if(StringUtils.isNotBlank(phoneNo)){
            phoneNo = phoneNo.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
        }
        return phoneNo;
    }

    /**
     * 手机号脱敏中间几位
     * @param phoneNo
     * @return
     */
    public static String maskPhoneNumber(String phoneNo) {
        if(StringUtils.isNotBlank(phoneNo)){
            phoneNo = phoneNo.trim();
            String areaNo = "";
            if (phoneNo.contains("-")) {
                areaNo = StringUtils.substring(phoneNo, 0 , phoneNo.indexOf("-") + 1);
                phoneNo = StringUtils.substringAfter(phoneNo, "-");
            }
            int len = phoneNo.length();
            String maskStr = Constants.PHONE_NO_MASK_STR;
            if (len < 11) {
                maskStr = Constants.MINI_PHONE_NO_MASK_STR;
            }
            int retainLen = len - maskStr.length();
            int prefixLen = retainLen / 2;
            int suffixStartPos = len - prefixLen;
            if (retainLen % 2 != 0) {
                suffixStartPos -= 1;
            }
            phoneNo = areaNo.concat(StringUtils.left(phoneNo, prefixLen)).concat(maskStr)
                    .concat(StringUtils.substring(phoneNo, suffixStartPos));
        }
        return phoneNo;
    }


    /**
     * 身份证脱敏，保留前六后三
     * @param idCardNo
     * @return
     */
    public static String maskIdCardNo(String idCardNo){
        if (StringUtils.isNotBlank(idCardNo)) {
            return StringUtils.left(idCardNo, 6).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(idCardNo, 3), StringUtils.length(idCardNo), "*"), "******"));
        }
        return idCardNo;
    }

    public static void main(String[] args) {
        System.out.println(maskPhoneNumber("027-13978292828"));
        System.out.println(maskPhoneNumber("0853-13978292828"));
        System.out.println(maskPhoneNumber("13978292828"));
        System.out.println(maskPhoneNumber("1397829282"));
        System.out.println(maskPhoneNumber("13978292"));
        System.out.println(maskPhoneNumber("1397829"));
    }
}
