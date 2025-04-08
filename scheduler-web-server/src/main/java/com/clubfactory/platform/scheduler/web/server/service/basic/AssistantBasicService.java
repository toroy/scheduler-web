package com.clubfactory.platform.scheduler.web.server.service.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.clubfactory.platform.scheduler.dal.enums.DependTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.JobOnline;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineDependsService;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;
import com.clubfactory.platform.scheduler.web.core.service.ProjectService;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineVO;
import com.clubfactory.platform.scheduler.web.server.service.GraphBizService;
import com.clubfactory.platform.scheduler.web.server.vo.Vertex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AssistantBasicService {

    @Resource
    protected ProjectService projectService;

    @Resource
    protected JobOnlineService jobOnlineService;

    @Resource
    protected JobOnlineDependsService jobOnlineDependsService;

    @Resource
    protected GraphBizService graphBizService;


    public GetObjectRequest getGetObjectRequestFrom(String url, MutableObject<String> fileName) {
        String url2 = url.replace("s3://", "");
        String[] arr = url2.split("[/]", 2);
        if (arr.length != 2) {
            throw new BizException("无法识别 s3 url: " + url);
        }
        GetObjectRequest gor = new GetObjectRequest(arr[0], arr[1]);
        String[] arr2 = url2.split("[/]");
        fileName.setValue(arr2[arr2.length - 1]);
        return gor;
    }


    public void loopTopJobIds(long projectId, Vertex rootVertex, List<JobOnlineVO> jobVos,
                              Set<Long> topJobIds,
                              Map<Long, List<JobOnlineDependsVO>> jobIdDepVosMap,
                              Map<Long, List<JobOnlineDependsVO>> jobPidDepChildVosMap,
                              Map<Long, List<JobOnline>> childJobIdOnlinesMap) {
        List<Vertex.Edge> rootEdgesToTopVtx = rootVertex.getChilds();

        for (JobOnlineVO jobOnlineVo : jobVos) {
            Long jobId = jobOnlineVo.getJobId();
            if (!topJobIds.contains(jobId)) {
                continue;
            }
            topJobIds.remove(jobId);

            List<JobOnlineDependsVO> dependsVOs = jobIdDepVosMap.getOrDefault(jobId, new ArrayList<>());
            List<JobOnlineDependsVO> dependsChildVOs = jobPidDepChildVosMap.getOrDefault(jobId, new ArrayList<>());
            Vertex topVertex = graphBizService.genJobOnlineVertex(jobOnlineVo, dependsVOs, dependsChildVOs);
            // 取子节点信息
            List<Long> childIds = dependsChildVOs.stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
            List<JobOnline> childJobOnlines = new ArrayList<>();
            for (Long cid : childIds) {
                childJobOnlines.addAll(childJobIdOnlinesMap.getOrDefault(cid, new ArrayList<>()));
            }
            Map<Long, DependTypeEnum> dependTypeMap = new HashMap<>();
            for (JobOnlineDependsVO ele : dependsChildVOs) {
                dependTypeMap.put(ele.getJobId(), ele.getType());
            }

            if (!childJobOnlines.isEmpty()) {
                for (JobOnline childJobOnline : childJobOnlines) {
                    Long curChildJobId = childJobOnline.getJobId();
                    Long curProjId = childJobOnline.getProjectId();
                    if (curProjId == null) {
                        continue;
                    }
                    // 自依赖，不放了
                    if (jobId.equals(curChildJobId) ||
                            projectId != curProjId.longValue()) {
                        continue;
                    }
                    Vertex.Edge edge = graphBizService.genGraph(childJobOnline, dependTypeMap);
                    Vertex subVertex = edge.getVertex();
                    recursionGenGraph(projectId, curChildJobId, childJobOnline, subVertex, jobIdDepVosMap, jobPidDepChildVosMap, childJobIdOnlinesMap);
                    topVertex.addChild(edge);
                }
            }

            Vertex.Edge rootVtxEdge = new Vertex.Edge();
            rootVtxEdge.setType(dependTypeMap.get(jobId));
            rootVtxEdge.setVertex(topVertex);
            rootEdgesToTopVtx.add(rootVtxEdge);
        }
    }

    private void recursionGenGraph(long projectId, Long jobId, JobOnline jobOnlineVo, Vertex prevVertex,
                                   Map<Long, List<JobOnlineDependsVO>> jobIdDepVosMap,
                                   Map<Long, List<JobOnlineDependsVO>> jobPidDepChildVosMap,
                                   Map<Long, List<JobOnline>> childJobIdOnlinesMap) {
        List<JobOnlineDependsVO> dependsChildVOs = jobPidDepChildVosMap.get(jobId);
        if (CollectionUtils.isEmpty(dependsChildVOs)) {
            return;
        }
        // 取子节点信息
        List<Long> childIds = dependsChildVOs.stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
        List<JobOnline> childJobOnlines = new ArrayList<>();
        for (Long cid : childIds) {
            childJobOnlines.addAll(childJobIdOnlinesMap.getOrDefault(cid, new ArrayList<>()));
        }
        Map<Long, DependTypeEnum> dependTypeMap = new HashMap<>();
        for (JobOnlineDependsVO ele : dependsChildVOs) {
            dependTypeMap.put(ele.getJobId(), ele.getType());
        }

        if (!childJobOnlines.isEmpty()) {
            for (JobOnline childJobOnline : childJobOnlines) {
                Long childJobId = childJobOnline.getJobId();
                Long curProjId = childJobOnline.getProjectId();
                if (curProjId == null) {
                    continue;
                }
                // 自依赖或不同项目的，过滤掉
                if (jobId.equals(childJobId) ||
                        projectId != curProjId.longValue()) {
                    continue;
                }
                Vertex subVertex = new Vertex();
                subVertex.setId(childJobId);
                subVertex.setName(childJobOnline.getName());
                subVertex.setTargetTable(childJobOnline.getTargetTable());
                subVertex.setScheduler(childJobOnline.getCycleType().getSchedulerTime(JSON.parseObject(childJobOnline.getSchedulerTime(), SchedulerTimeDto.class)));

                recursionGenGraph(projectId, childJobId, childJobOnline, subVertex, jobIdDepVosMap, jobPidDepChildVosMap, childJobIdOnlinesMap);

                Vertex.Edge toPrevEdge = new Vertex.Edge();
                toPrevEdge.setType(dependTypeMap.get(childJobOnline.getJobId()));
                toPrevEdge.setVertex(subVertex);
                prevVertex.addChild(toPrevEdge);
            }
        }
    }

}
