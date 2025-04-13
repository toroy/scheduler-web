package com.clubfactory.platform.scheduler.web.server.vo;

import com.beust.jcommander.internal.Lists;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.AlertSub;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class SubscribeVo {
    private Long id;
    private String dataSource;
    /**
     * 用于展示的数据源描述信息
     */
    private String desc;
    private String dbName;
    private String tableName;
    /**
     * 责任人名称
     */
    private String pic;
    private String subType;
    private List<BasicGroupInfoVo> groups;

    public static SubscribeVo po2Vo(AlertSub alertSub, String picName, String dataSourceDesc, List<GroupInfo> groupInfos) {
        SubscribeVo subscribeVo = new SubscribeVo();
        BeanUtil.copyBeanNotNull2Bean(alertSub, subscribeVo);
        subscribeVo.setSubType(alertSub.getSubType().getDesc());
        subscribeVo.setPic(picName);
        subscribeVo.setDesc(dataSourceDesc);
        List<BasicGroupInfoVo> groupInfoVos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(groupInfos)) {
            groupInfoVos = groupInfos.stream().map(BasicGroupInfoVo::po2Vo).collect(Collectors.toList());
        }
        subscribeVo.setGroups(groupInfoVos);
        return subscribeVo;
    }
}
