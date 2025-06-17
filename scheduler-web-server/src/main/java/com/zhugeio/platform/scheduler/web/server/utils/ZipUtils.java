package com.zhugeio.platform.scheduler.web.server.utils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.nio.charset.StandardCharsets;


/**
 * Zip包解压工具
 * @author xiejiajun
 */
public class ZipUtils {

    /**
     * 将路径为zipFilePath的zip包解压到destDir
     * @param zipFilePath zip包路径
     * @param destDir 解压到
     */
    public static void unzip(String zipFilePath,String destDir){
        try {
            zipFilePath = zipFilePath.trim();
            destDir = destDir.trim();
            ZipFile zipFile = new ZipFile(zipFilePath);
            // TODO 需要验证Windows上传的zip包是否会文件名乱码
            zipFile.setFileNameCharset(StandardCharsets.UTF_8.name());
            zipFile.extractAll(destDir);
        } catch (ZipException e) {
            throw new RuntimeException("解压Zip包失败",e);
        }
    }
}
