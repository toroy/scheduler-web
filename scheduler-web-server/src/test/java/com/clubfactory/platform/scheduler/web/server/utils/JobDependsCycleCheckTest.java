package com.clubfactory.platform.scheduler.web.server.utils;

import com.clubfactory.platform.scheduler.web.core.utils.JobDependsCycleCheck;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.clubfactory.platform.scheduler.web.server.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class JobDependsCycleCheckTest extends BaseTest  {
    private List<JobOnlineDependsVO> jobOnlineDependsVOS = new ArrayList<>();


    @Before
    public void initJobOnlineMap() {
        JobOnlineDependsVO jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(324L);
        jobOnlineDependsVO.setParentId(325L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(324L);
        jobOnlineDependsVO.setParentId(326L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(325L);
        jobOnlineDependsVO.setParentId(331L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(324L);
        jobOnlineDependsVO.setParentId(329L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(324L);
        jobOnlineDependsVO.setParentId(341L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(329L);
        jobOnlineDependsVO.setParentId(328L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(330L);
        jobOnlineDependsVO.setParentId(328L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(331L);
        jobOnlineDependsVO.setParentId(347L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(347L);
        jobOnlineDependsVO.setParentId(326L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

        jobOnlineDependsVO = new JobOnlineDependsVO();
        jobOnlineDependsVO.setJobId(326L);
        jobOnlineDependsVO.setParentId(331L);
        jobOnlineDependsVOS.add(jobOnlineDependsVO);

    }



    @Test
    public void testCheckCycle() {
        Map<Long, Set<Long>> jobOnlineMap = new HashMap<>();
        for (JobOnlineDependsVO jobOnlineDependsVO : jobOnlineDependsVOS) {
            Long jobId = jobOnlineDependsVO.getJobId();
            Long parentId = jobOnlineDependsVO.getParentId();
            if (jobOnlineMap.containsKey(jobId)) {
                jobOnlineMap.get(jobId).add(parentId);
            } else {
                Set<Long> parentIdSet = new HashSet<>();
                parentIdSet.add(parentId);
                jobOnlineMap.put(jobId, parentIdSet);
            }
        }


        System.out.println(jobOnlineMap);
        JobDependsCycleCheck jobDependsCycleCheck = new JobDependsCycleCheck(jobOnlineMap);
        System.out.println(328L + "," + Arrays.asList(325L));
        Assert.assertFalse(jobDependsCycleCheck.checkCycle(328L, Arrays.asList(325L)));
        Assert.assertTrue(jobDependsCycleCheck.checkCycle(342L, Arrays.asList(344L)));
        Assert.assertTrue(jobDependsCycleCheck.checkCycle(324L, Arrays.asList(587L)));
    }
}
