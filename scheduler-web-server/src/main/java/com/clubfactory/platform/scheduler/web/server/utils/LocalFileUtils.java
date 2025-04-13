package com.clubfactory.platform.scheduler.web.server.utils;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author xiejiajun
 */
public class LocalFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileUtils.class);

    /**
     * 将上传的文件写到destFilename指定的路径
     *
     * @param file
     * @param destFilename
     */

    public static void copyFile(MultipartFile file, String destFilename) {
        InputStream in = null;
        try {
            File destFile = new File(destFilename);
            File destParentDir = new File(destFile.getParent());

            if (!destParentDir.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(destParentDir);
            }
            in = file.getInputStream();
            Files.copy(in, Paths.get(destFilename));
        } catch (IOException e) {
            logger.error("failed to copy file , {} is empty file", file.getOriginalFilename(), e);
            throw new BizException("拷贝文件到本地失败:" + e.getMessage());
        } finally {
            if (in != null ){
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * 文件下载资源封装
     *
     * @param filename
     * @return
     */
    public static Resource file2Resource(String filename) throws MalformedURLException {
        Path file = Paths.get(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            logger.error("file can not read : {}", filename);
        }
        return null;
    }

}
