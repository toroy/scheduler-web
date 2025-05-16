package com.bigdata.platform.scheduler.web.server;

import com.bigdata.platform.scheduler.web.core.utils.DFSUtils;
import org.junit.Test;

import java.io.IOException;

public class DFSTest  extends BaseTest{

    /**
     * 将resource.storage.dfs.type配置为S3即可
     * @throws IOException
     */
    @Test
    public void upload2S3() throws IOException {

        String tenant = "scheduler_user1";
        String fileName = "test_file.txt";

        String resourcePath = DFSUtils.getDfsResDir(tenant);
        String dfsFilename = DFSUtils.getDfsFilePath(resourcePath,fileName);


        System.out.println(dfsFilename);
        System.out.println(resourcePath);

        String localFilename = "本地文件绝对路径";

        if (!DFSUtils.getInstance().exists(resourcePath)) {
            DFSUtils.getInstance().mkdir(resourcePath);
        }
        DFSUtils.getInstance().copyLocalToDfs(localFilename,dfsFilename,true,true);

    }

    /**
     * 将resource.storage.dfs.type配置为HDFS即可
     * @throws IOException
     */
    @Test
    public void upload2HDFS() throws IOException {
        String tenant = "scheduler_user1";
        String fileName = "test_file.txt";

        String resourcePath = DFSUtils.getDfsResDir(tenant);
        String dfsFilename = DFSUtils.getDfsFilePath(resourcePath,fileName);

        System.out.println(dfsFilename);
        System.out.println(resourcePath);

        String localFilename = "本地文件绝对路径";

        if (!DFSUtils.getInstance().exists(resourcePath)) {
            DFSUtils.getInstance().mkdir(resourcePath);
        }
        DFSUtils.getInstance().copyLocalToDfs(localFilename,dfsFilename,true,true);
    }

}
