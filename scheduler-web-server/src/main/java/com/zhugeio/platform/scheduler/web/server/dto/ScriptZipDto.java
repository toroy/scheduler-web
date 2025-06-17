package com.zhugeio.platform.scheduler.web.server.dto;

import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class ScriptZipDto {

    /**
     * 是否从S3直接拉取Zip包
     */
    private boolean isS3Mode = false;

    /**
     * zip包对应的S3完整路径,isS3Mode为true时使用
     */
    private String zipFile;

    /**
     * 上传的Zip包对应的脚本ID,isS3Mode为false时使用
     */
    private Long zipScriptId;

}
