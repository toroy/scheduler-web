package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.vo.Vertex;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.po.JobOnline;
import com.zhugeio.platform.scheduler.dal.po.Project;
import com.zhugeio.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.zhugeio.platform.scheduler.web.core.vo.JobOnlineVO;
import com.zhugeio.platform.scheduler.web.core.constant.Fields;
import com.zhugeio.platform.scheduler.web.server.dto.ProjectPagerDto;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;
import com.zhugeio.platform.scheduler.web.server.service.basic.AssistantBasicService;
import com.zhugeio.platform.scheduler.web.core.utils.MapUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chen qian
 */
@Slf4j
@Service
public class AssistantBizService extends AssistantBasicService {

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
