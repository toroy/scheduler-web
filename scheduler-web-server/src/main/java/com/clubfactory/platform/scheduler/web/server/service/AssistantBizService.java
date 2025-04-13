package com.clubfactory.platform.scheduler.web.server.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.po.JobOnline;
import com.clubfactory.platform.scheduler.dal.po.Project;
import com.clubfactory.platform.scheduler.web.core.constant.SysConfigConstant;
import com.clubfactory.platform.scheduler.web.core.utils.SysConfigUtil;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineVO;
import com.clubfactory.platform.scheduler.web.core.constant.Fields;
import com.clubfactory.platform.scheduler.web.server.constant.RestParams;
import com.clubfactory.platform.scheduler.web.server.dto.ProjectPagerDto;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.service.basic.AssistantBasicService;
import com.clubfactory.platform.scheduler.web.core.utils.MapUtils;
import com.clubfactory.platform.scheduler.web.server.vo.Vertex;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chen qian
 */
@Slf4j
@Service
public class AssistantBizService extends AssistantBasicService {


    public static final String HDFS = "hdfs";
    public static final String S3 = "s3";
    public static final String CONTENT_LENGTH = "Content-Length";
    private AmazonS3 s3Client = null;

    private AmazonS3 getS3Client() {
        if (s3Client == null) {
            synchronized (this) {
                if (s3Client == null) {
                    s3Client = AmazonS3ClientBuilder.standard()
                            .withRegion(Regions.US_WEST_2)
                            .build();
                    return s3Client;
                } else {
                    return s3Client;
                }
            }
        } else {
            return s3Client;
        }
    }


    public ResponseEntity getFileBytesByType(String type) throws Exception {
        String url = "";
        switch (type) {
            case RestParams.BATCH_SCRIPT:
                url = SysConfigUtil.getByKey(SysConfigConstant.ASSITANT_DOWNLOAD_PATH_BATCH_SCRIPT);
                break;
            case RestParams.TASK_PYTHON:
                url = SysConfigUtil.getByKey(SysConfigConstant.ASSITANT_DOWNLOAD_PATH_TASK_PYTHON);
                break;
            case RestParams.TASK_JSON:
                url = SysConfigUtil.getByKey(SysConfigConstant.ASSITANT_DOWNLOAD_PATH_TASK_JSON);
                break;
            case RestParams.DEP_JSON:
                url = SysConfigUtil.getByKey(SysConfigConstant.ASSITANT_DOWNLOAD_PATH_DEP_JSON);
                break;
            default:
                throw new BizException("不支持的type：" + type);
        }

        MutableObject<String> fileName = new MutableObject<>();
        GetObjectRequest gor = getGetObjectRequestFrom(url, fileName);
        S3Object obj = getS3Client().getObject(gor);
        ObjectMetadata objectMetadata = obj.getObjectMetadata();
        long contentLen = objectMetadata.getContentLength();
        byte[] bytes = IOUtils.toByteArray(obj.getObjectContent());
//        fileName.setValue("test.txt");
//        long contentLen = 1;
//        byte[] bytes = "a".getBytes();

        Resource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .contentLength(contentLen)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName.getValue() + "\"")
                .body(resource);
    }


    public Vertex getProjectGraph(Long projectId) {
        Assert.notNull(projectId);

        JobOnline job = new JobOnline();
        job.setProjectId(projectId);
        job.setIsDeleted(false);
        List<JobOnlineVO> jobVos = jobOnlineService.list(job);

        Project example = new Project();
        example.setId(projectId);
        Project project = projectService.get(example);
        Vertex rootVertex = new Vertex();
        rootVertex.setId(projectId);
        rootVertex.setName(project.getProjectName());
        rootVertex.setIsSelfDependent(false);
        if (CollectionUtils.isEmpty(jobVos)) {
            return rootVertex;
        }

        List<Long> jobIds = jobVos.stream().map(JobOnlineVO::getJobId).collect(Collectors.toList());

        List<JobOnlineDependsVO> dependsVOs = jobOnlineDependsService.listVoByField(Fields.JOB_ID, jobIds);
        Map<Long, List<JobOnlineDependsVO>> jobIdDepVosMap = new HashMap<>();
        Set<Long> topJobIds = new HashSet<>(jobIds);
//        Set<Long> topJobIds = dependsVOs.stream().map(JobOnlineDependsVO::getParentId).collect(Collectors.toSet());

        for (JobOnlineDependsVO ele : dependsVOs) {
            long jobId = ele.getJobId();
            long pid = ele.getParentId();
            if (jobId != pid && jobIds.contains(pid)) {
                //jobId 是别人的子，不应该出现在 topJobIds 里
                topJobIds.remove(jobId);
            }
            MapUtils.fillKeyListMap(jobIdDepVosMap, jobId, ele);
        }

        List<JobOnlineDependsVO> dependsChildVOs = jobOnlineDependsService.listNoSelfByParentIds(jobIds);
        Map<Long, List<JobOnlineDependsVO>> jobPidDepChildVosMap = new HashMap<>();
        for (JobOnlineDependsVO ele : dependsChildVOs) {
            MapUtils.fillKeyListMap(jobPidDepChildVosMap, ele.getParentId(), ele);
        }

        List<Long> childIds = dependsChildVOs.stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
        List<JobOnline> childJobOnlines = jobOnlineService.listByIdsIfNotDelete(childIds);
        Map<Long, List<JobOnline>> childJobIdOnlinesMap = new HashMap<>();
        for (JobOnline ele : childJobOnlines) {
            MapUtils.fillKeyListMap(childJobIdOnlinesMap, ele.getJobId(), ele);
        }

        loopTopJobIds(projectId, rootVertex, jobVos, topJobIds, jobIdDepVosMap, jobPidDepChildVosMap, childJobIdOnlinesMap);
        return rootVertex;
    }


    public PageUtils<Project> queryProjectByPage(ProjectPagerDto pagerDto) {
        Project project = new Project();
        project.setIsDeleted(false);
        project.setProjectName(pagerDto.getName());

        PageUtils<Project> pages = projectService.pageList(project);
        // 返回数据
        if (pages.getSize() == 0) {
            return new PageUtils<Project>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        long loginUid = LocalUser.get().getLocalUserId();

        List<Project> projVos = pages.getRows()
                .stream()
                .map(po -> {
                    Project projectVO = new Project();
                    if (po != null) {
                        return po;
                    }else {
                        return new Project();
                    }
                })
                .sorted(new Comparator<Project>() {
                    @Override
                    public int compare(Project o1, Project o2) {
                        long uid1 = o1.getCreateUser();
                        long uid2 = o2.getCreateUser();
                        //自己的排前面
                        if (uid1 == loginUid) {
                            return -1;
                        }
                        if (uid2 == loginUid) {
                            return 1;
                        }
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        return new PageUtils<Project>(projVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
    }


}
