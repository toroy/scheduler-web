package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.dal.po.AlertSub;
import com.zhugeio.platform.scheduler.dal.po.GroupInfo;
import com.zhugeio.platform.scheduler.web.core.vo.SubGroupRelVO;
import com.zhugeio.platform.scheduler.dal.dao.SubGroupRelMapper;
import com.zhugeio.platform.scheduler.dal.po.SubGroupRel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Service
public class SubGroupRelService extends BaseNewService<SubGroupRelVO, SubGroupRel> {

    @Resource
    SubGroupRelMapper subGroupRelMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(subGroupRelMapper);
    }

    /**
     * 根据指定字段批量删除
     * @param whereField
     * @param batchIdList
     */
    public void batchRemove(String whereField, List<Long> batchIdList) {
        SubGroupRel subGroupRelWhere = new SubGroupRel();
        subGroupRelWhere.setIsDeleted(false);
        subGroupRelWhere.setIds(batchIdList);
        subGroupRelWhere.setQueryListFieldName(whereField);
        this.subGroupRelMapper.remove(subGroupRelWhere);
    }

    public List<GroupInfo> listGroupInfosBySubId(Long subscribeId) {
        return this.subGroupRelMapper.selectGroupInfosBySubscribeId(subscribeId);
    }

    public List<AlertSub> listSubscribeInfosByGroupId(Long groupId) {
        return this.subGroupRelMapper.selectSubInfosByGroupInfoId(groupId);
    }

}
