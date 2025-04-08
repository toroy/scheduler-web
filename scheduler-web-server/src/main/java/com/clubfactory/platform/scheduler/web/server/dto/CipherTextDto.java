package com.clubfactory.platform.scheduler.web.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CipherTextDto implements Serializable {

    /**
     * 密文
     */
    private String cipherText;

    /**
     * 加密密钥
     */
    private String pwdKey;
}
