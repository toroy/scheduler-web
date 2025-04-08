package com.clubfactory.platform.scheduler.web.core.proxy;

import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.meta.client.dto.ColumnDto;
import com.clubfactory.platform.meta.client.dto.TableOwnerDto;
import com.clubfactory.platform.meta.client.service.MetaClientService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2022/2/8 7:55 下午
 **/
@Service
@Slf4j
public class MetaClientProxy extends BaseProxy {

    @Resource
    private MetaClientService metaClientService;

    public Map<String, TableOwnerDto> getUserByTableKey(String key) {
        BaseResult<Map<String, TableOwnerDto>> result = metaClientService.getUserByTableKey(Lists.newArrayList(key));
        checkResult(result.getCode(), result.getMessage());
        return result.getBody();
    }

    public List<ColumnDto> listHiveColumns(String dbName, String tableName) {
        BaseResult<List<ColumnDto>> result = metaClientService.listHiveColumns(dbName, tableName);
        checkResult(result.getCode(), result.getMessage());
        return result.getBody();
    }
}
