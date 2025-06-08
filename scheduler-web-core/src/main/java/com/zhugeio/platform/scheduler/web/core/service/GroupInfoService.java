package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.dal.dao.GroupInfoMapper;
import com.zhugeio.platform.scheduler.dal.po.GroupInfo;
import com.zhugeio.platform.scheduler.web.core.vo.GroupInfoVO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Service
public class GroupInfoService extends BaseNewService<GroupInfoVO, GroupInfo> {

    @Resource
    GroupInfoMapper groupInfoMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(groupInfoMapper);
    }

    public List<GroupInfoVO> listByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setIds(ids);
        groupInfo.setIsDeleted(false);
        return this.list(groupInfo);
    }


}
