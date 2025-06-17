package com.zhugeio.platform.scheduler.web.server.dqc.controller;

import com.zhugeio.platform.scheduler.web.server.dqc.service.DqcPartitionBizService;
import com.zhugeio.platform.scheduler.web.server.dqc.vo.PartitionCalVo;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.dal.po.DqcPartition;
import com.zhugeio.platform.scheduler.web.core.dqc.vo.DqcPartitionVO;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "dqc-分区")
@RequestMapping("/dqc/partition")
@RestController
public class DqcPartitionController {

    @Resource
    DqcPartitionBizService partitionBizService;

    @ApiOperation(value="分区计算", notes="分区计算")
    @PostMapping("cal")
    public BaseResult<PartitionCalVo> cal(@RequestBody DqcPartition partition) {
        return new BaseResult<>(partitionBizService.cal(partition.getExpression()));
    }

    @ApiOperation(value="新建分区表达式", notes="新建分区表达式")
    @PostMapping("addExpression")
    public BaseResult<Boolean> addExpression(@RequestBody DqcPartition partition) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(partitionBizService.addExpression(partition, userDto));
    }

    @ApiOperation(value="删除分区表达式", notes="删除分区表达式")
    @PostMapping("deleteExpression")
    public BaseResult<Boolean> deleteExpression(@RequestBody DqcPartition partition) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(partitionBizService.delete(partition.getId(), userDto));
    }

    @ApiOperation(value="分区表达式列表", notes="分区表达式列表")
    @GetMapping("list")
    public BaseResult<Map<String, List<DqcPartitionVO>>> list(DqcPartition partition) {
        List<DqcPartitionVO> partitionVOS = partitionBizService.list(partition.getDbName(), partition.getTableName(), partition.getExpression());
        Map<String, List<DqcPartitionVO>> map = Maps.newHashMap();
        map.put("rows", partitionVOS);
        return new BaseResult<>(map);
    }
}
