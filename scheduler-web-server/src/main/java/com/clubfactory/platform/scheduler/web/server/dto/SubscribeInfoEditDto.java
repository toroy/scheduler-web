package com.clubfactory.platform.scheduler.web.server.dto;

import com.beust.jcommander.internal.Lists;
import com.clubfactory.platform.scheduler.dal.po.SubGroupRel;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class SubscribeInfoEditDto extends SubscribeInfoDto {

    /**
     * 订阅ID
     */
    private Long id;

    public List<SubGroupRel> buildRefList() {
        if (CollectionUtils.isEmpty(this.getUserGroups())) {
            return Lists.newArrayList();
        }
        Long loginUser = LocalUser.getLoginUserId();
        return this.getUserGroups().stream()
                .map(groupId -> {
                    SubGroupRel rel = new SubGroupRel();
                    rel.setGroupId(groupId);
                    rel.setSubId(id);
                    rel.setCreateUser(loginUser);
                    rel.setUpdateUser(loginUser);
                    return rel;
                }).collect(Collectors.toList());
    }

}
