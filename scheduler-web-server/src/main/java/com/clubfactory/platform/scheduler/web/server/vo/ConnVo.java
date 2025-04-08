package com.clubfactory.platform.scheduler.web.server.vo;

import com.clubfactory.platform.scheduler.dal.enums.DbType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class ConnVo implements Serializable {

    private static final long serialVersionUID = -1112864448293696641L;

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 数据源名称
     */
    private String connId;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private String port;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码密文
     */
    private String encryptPwd;

    /**
     * 加密密钥
     */
    private String pwdKey;

    /**
     * 连接串
     */
    private String connUrl;

    /**
     * 数据源类型
     */
    private DbType dbType;

    /**
     * 数据库名称
     */
    private String dbName;


}
