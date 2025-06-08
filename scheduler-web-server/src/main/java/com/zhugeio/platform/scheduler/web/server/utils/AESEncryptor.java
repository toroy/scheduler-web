package com.zhugeio.platform.scheduler.web.server.utils;


import com.zhugeio.platform.scheduler.common.utils.AESUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


/**
 * @author xiejiajun
 */
@Component
public class AESEncryptor {

    private AESUtils aesUtils;

    @PostConstruct
    public void init(){
        this.aesUtils = new AESUtils();
    }


    /**
     * 产生符合要求的Key,随机性更好
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String generateKey() throws NoSuchAlgorithmException, UnsupportedEncodingException {
       return aesUtils.generateKey();
    }


    /**
     * 加密
     * @param input
     * @param key
     * @return
     */
    public String encrypt(String input, String key) throws Exception {
        if (input == null || key == null){
            return null;
        }
        return aesUtils.encrypt(input, key);
    }


    /**
     * 解密
     * @param cipherText
     * @param key
     * @return
     */
    public String decrypt(String cipherText, String key) throws Exception {
        if (cipherText == null || key == null){
            return null;
        }
       return aesUtils.decrypt(cipherText, key);
    }

    public static void main(String[] args) throws Exception {
        AESUtils aesUtils = new AESUtils();
        String password = aesUtils.decrypt("0Is8AMmqBA1s4WiUzVUtxb7KSttjo7FFweczwVa5riY=","atB0Ci+qxb+OK8ZfvqM37Q==");
        System.out.println(password);
    }



}
