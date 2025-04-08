package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.dal.dao.UserInfoMapper;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.core.vo.UserInfoVO;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Service
public class UserInfoService extends BaseNewService<UserInfoVO, UserInfo> {

    @Resource
    UserInfoMapper userInfoMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(userInfoMapper);
    }

    /**
     * 根据userId查找默认组ID
     * @param userId
     * @return
     */
    public Long getDefaultGroupIdByUserId(Long userId) {
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setUserId(userId);
        userInfoWhere.setIsDeleted(false);
        UserInfo userInfo = this.userInfoMapper.get(userInfoWhere);
        return userInfo != null ? userInfo.getMainGroupId() : null;
    }

    /**
     * 根据联系人ID获取其默认组ID
     * @param userInfoId
     * @return
     */
    public Long getDefaultGroupIdById(Long userInfoId) {
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setId(userInfoId);
        userInfoWhere.setIsDeleted(false);
        UserInfo userInfo = this.userInfoMapper.get(userInfoWhere);
        return userInfo != null ? userInfo.getMainGroupId() : null;
    }

    public List<UserInfo> listLoginUserInfos(UserInfo userInfoWhere) {
        return this.userInfoMapper.listLoginUserInfos(userInfoWhere);
    }
}
