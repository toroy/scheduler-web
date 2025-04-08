package com.clubfactory.platform.scheduler.web.core.dqc.service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.DqcPartitionMapper;
import com.clubfactory.platform.scheduler.dal.po.DqcPartition;
import com.clubfactory.platform.scheduler.web.core.dqc.vo.DqcPartitionVO;
import com.clubfactory.platform.scheduler.web.core.service.BaseNewService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DqcPartitionService extends BaseNewService<DqcPartitionVO, DqcPartition> {

    @Resource
    DqcPartitionMapper dqcPartitionMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(dqcPartitionMapper);
    }

    public Boolean delById(Long id) {
        Assert.notNull(id);

        DqcPartition dqcPartition = new DqcPartition();
        dqcPartition.setId(id);
        this.logicRemove(dqcPartition);
        return true;
    }

    public DqcPartition getById(Long id) {
        Assert.notNull(id);

        DqcPartition dqcPartition = new DqcPartition();
        dqcPartition.setId(id);
        return this.get(dqcPartition);
    }

    public String getExpression(Long id) {
        Assert.notNull(id);
        DqcPartition dqcPartition = new DqcPartition();
        dqcPartition.setId(id);
        return this.get(dqcPartition).getExpression();
    }

    public Map<Long, String> getExpressionMap(List<Long> ids) {
        Assert.collectionNonEmpty(ids, "ids");
        DqcPartition dqcPartition = new DqcPartition();
        dqcPartition.setIds(ids);
        return this.list(dqcPartition).stream().collect(Collectors.toMap(DqcPartition::getId, DqcPartition::getExpression));
    }

}
