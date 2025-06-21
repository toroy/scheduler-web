package com.zhugeio.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhugeio.platform.scheduler.dal.po.Team;
import com.zhugeio.platform.scheduler.dal.po.User;
import com.zhugeio.platform.scheduler.web.core.service.TeamService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.utils.HttpUtils;
import com.zhugeio.platform.scheduler.web.core.vo.DepartmentVo;
import com.zhugeio.platform.scheduler.web.core.vo.TeamVO;
import com.zhugeio.platform.scheduler.web.core.vo.UserVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Slf4j
@Service
public class TeamBizService {

    @Autowired
    private TeamService teamService;
    @Resource
    UserService userService;

    @Value("${team.list.poll.address}")
    private String pollAddress;

    @Value("${team.list.poll.app-id}")
    private String appId;

    @Value("${team.list.poll.search-ids}")
    private String ids;

    /**
     * 更新sc_team中的数据：先删除后插入
     * 这里的事务只对插入操作生效，truncate不生效
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void refreshTeams(){
        List<Team> teams = pollTeamListFromMiddleWare();
        if (CollectionUtils.isNotEmpty(teams)){
            teamService.clearData();
            Team team = new Team();
            team.setDepartId(-1);
            team.setDepartName("公共");
            team.setIsDeleted(false);
            teams.add(team);
            try {
                teamService.saveBatch(teams);
                teamService.freshCache();
                log.info("更新团队列表成功...");
            }catch (Exception e){
                log.error("更新团队列表失败",e);
            }

        }
    }

    /**
     * 从中间件API获取团队列表
     * @return
     */
    private List<Team> pollTeamListFromMiddleWare(){
        if (StringUtils.isEmpty(pollAddress) || StringUtils.isEmpty(ids) || StringUtils.isEmpty(appId)){
            return null;
        }
        List<Team> teams = Lists.newArrayList();
        List<String> idList = Arrays.asList(ids.trim().split(","));
        for (String parenIdStr : idList){
            Integer parentId = NumberUtils.toInt(parenIdStr,-1);
            if (parentId != -1) {
                String url = String.format("%s?appId=%s&id=%s", pollAddress, appId, parenIdStr);
                log.info("poll team list from {}", url);
                String jsonStr = HttpUtils.get(url);
                List<TeamVO> teamVOS = toList(jsonStr).stream()
                        .filter(vo -> parentId.equals(vo.getParentId()))
                        .map(vo -> {
                            try {
                                String departName = vo.getDepartName();
                                if (departName != null){
                                    departName = departName.split("-")[0];
                                    if (StringUtils.isNotBlank(departName)){
                                        vo.setDepartName(departName);
                                    }
                                }
                            }catch (Exception e){
                                log.error(e.getMessage());
                            }
                            return vo;
                        })
                        .collect(Collectors.toList());
                teams.addAll(teamVOS);
            }
        }
        return teams;

    }


    /**
     * 接口响应值格式化
     * @param json
     * @return
     */
    private  List<TeamVO> toList(String json) {
        try {
            if (StringUtils.isEmpty(json)) {
                return new ArrayList<>();
            }
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject.getInteger("code") != 0){
                return new ArrayList<>();
            }
            String data = jsonObject.getString("data");
            return JSONArray.parseArray(data, TeamVO.class);
        } catch (Exception e) {
            log.error("JSONArray.parseArray exception!",e);
        }

        return new ArrayList<>();
    }


    /**
     * 列出所有部门信息
     * @return
     */
    public List<DepartmentVo> listDepartments() {
        User userDto = new User();
        userDto.setIsDeleted(false);
        List<UserVO> users = userService.list(userDto);
        if (CollectionUtils.isEmpty(users)) {
            return Lists.newArrayList();
        }

        Map<String, Integer> departMap = users.stream()
                .filter(user -> user.getDepartId() != null
                        && user.getDepartName() != null
                        && user.getUpdateTime() != null)
                .sorted(Comparator.comparing(User::getUpdateTime))
                .peek(user -> user.setDepartName(getDepartMent(user.getDepartName())))
                .collect(Collectors.toMap(User::getDepartName, User::getDepartId
                        , (oldkey, newkey) -> newkey));

        return users.stream().map(user -> {
            DepartmentVo department = new DepartmentVo();
            department.setDepartName(getDepartMent(user.getDepartName()));
            department.setDepartId(departMap.get(department.getDepartName()));
            return department;
        }).distinct().sorted(Comparator.comparing(DepartmentVo::getDepartName)).collect(Collectors.toList());
    }

    @NotNull
    private String getDepartMent(String departMent) {
        return StringUtils.substringBefore(departMent, "-").trim();
    }

    /**
     * 判断sc_team表是否为空
     * @return
     */
    public boolean teamInfosIsEmpty(){
        return CollectionUtils.isEmpty(teamService.list(new Team()));
    }

}
