package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.dal.dao.UserGroupRelMapper;
import com.zhugeio.platform.scheduler.dal.po.GroupInfo;
import com.zhugeio.platform.scheduler.dal.po.UserGroupRel;
import com.zhugeio.platform.scheduler.dal.po.UserInfo;
import com.zhugeio.platform.scheduler.web.core.utils.StreamApiUtils;
import com.zhugeio.platform.scheduler.web.core.vo.UserGroupRelVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserGroupRelService extends BaseNewService<UserGroupRelVO, UserGroupRel> {

    @Resource
    UserGroupRelMapper userGroupRelMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(userGroupRelMapper);
    }


    public List<GroupInfo> listGroupInfosByUserInfoId(Long userInfoId) {
        return this.userGroupRelMapper.selectGroupsByUserId(userInfoId);
    }

    public List<UserInfo> listUserInfosByGroupId(Long groupId) {
        return this.userGroupRelMapper.selectUserInfosByGroupId(groupId);
    }


    /**
     * 根据组ID列表获取联系人列表
     * @param groupIds
     * @return
     */
    public List<UserInfo> listUserInfosByGroupIds(List<Long> groupIds) {
        List<UserInfo> userInfoList = this.userGroupRelMapper.selectUserInfosByGroupIds(groupIds);
        if (CollectionUtils.isEmpty(userInfoList)) {
            return userInfoList;
        }
        return userInfoList.stream().filter(StreamApiUtils.distinctByKey(UserInfo::getId)).collect(Collectors.toList());
    }

    /**
     * 根据联系人ID列表获取联系人组列表
     * @param userInfoIds
     * @return
     */
    public List<GroupInfo> listGroupInfosByUserInfoIds(List<Long> userInfoIds) {
        return this.userGroupRelMapper.selectGroupInfosByUserIds(userInfoIds);
    }

}
