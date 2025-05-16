package com.bigdata.platform.scheduler.web.server.dqc.service;

import com.bigdata.platform.scheduler.web.server.dqc.vo.PartitionCalVo;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.common.constant.DateFormatPattern;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.DateUtil;
import com.bigdata.platform.scheduler.common.utils.placeholder.MacroVarConvertUtils;
import com.bigdata.platform.scheduler.dal.po.DqcPartition;
import com.bigdata.platform.scheduler.web.core.dqc.service.DqcPartitionService;
import com.bigdata.platform.scheduler.web.core.dqc.service.DqcRuleService;
import com.bigdata.platform.scheduler.web.core.dqc.vo.DqcPartitionVO;
import com.bigdata.platform.scheduler.web.core.dqc.vo.DqcRuleVO;
import com.bigdata.platform.scheduler.web.core.enums.ErrorCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DqcPartitionBizService {

    @Resource
    DqcPartitionService dqcPartitionService;
    @Resource
    DqcRuleService dqcRuleService;
    @Resource
    DqcBizService dqcBizService;

    public Boolean addExpression(DqcPartition partition, LoginUserDto userDto) {
        Assert.notNull(partition);
        Assert.notBlank(partition.getDbName());
        Assert.notBlank(partition.getTableName());
        Assert.notBlank(partition.getExpression());
        dqcBizService.isOwner(partition.getDbName(), partition.getTableName(), userDto.getUserid());

        partition.setCreateUser(userDto.getLocalUserId());
        partition.setUpdateUser(userDto.getLocalUserId());
        dqcPartitionService.save(partition);
        return true;
    }

    public List<DqcPartitionVO> list(String dbName, String tableName, String expression) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);

        DqcPartition partition = new DqcPartition();
        partition.setDbName(dbName);
        partition.setTableName(tableName);
        partition.setIsDeleted(false);
        partition.setExpression(expression);
        return dqcPartitionService.list(partition);
    }

    public Boolean delete(Long id, LoginUserDto userDto) {
        Assert.notNull(id);
        DqcPartition partition = dqcPartitionService.getById(id);
        dqcBizService.isOwner(partition.getDbName(), partition.getTableName(), userDto.getUserid());

        List<DqcRuleVO> vos = dqcRuleService.listByPartitionId(id);
        if (CollectionUtils.isNotEmpty(vos)) {
            String ruleNames = vos.stream().map(DqcRuleVO::getName).collect(Collectors.joining(","));
            throw new BizException(ErrorCode.DQC_DELETE_PARTITION_RULE_EXIST_ERROR.setParams(ruleNames));
        }

        return dqcPartitionService.delById(id);
    }


    public PartitionCalVo cal(String expression) {
        if (StringUtils.isBlank(expression)) {
            return new PartitionCalVo();
        }

        Date date = new Date();
        String result = MacroVarConvertUtils.coverPlaceholders(expression, date);
        if (StringUtils.equalsIgnoreCase(expression, result)) {
            result = "日期表达式格式不符合";
        } else {
            result = "成功，" + result;
        }
        PartitionCalVo partitionCalVo = new PartitionCalVo();
        partitionCalVo.setResult(result);
        partitionCalVo.setTaskTime(DateUtil.format(date, DateFormatPattern.YYYY_MM_DD_HH_MM_SS));

        return partitionCalVo;
    }

    public String cal(String expression, Date date) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
       return  MacroVarConvertUtils.coverPlaceholders(expression, date);
    }
}
