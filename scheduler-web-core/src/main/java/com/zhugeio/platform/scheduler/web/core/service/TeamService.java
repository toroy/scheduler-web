package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.TeamMapper;
import com.zhugeio.platform.scheduler.dal.po.Team;
import com.zhugeio.platform.scheduler.web.core.vo.TeamVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamService extends BaseNewService<TeamVO, Team> {

    @Resource
    TeamMapper teamMapper;

    /**
     * 存储departId -> departName缓存的Map
     */
    private volatile Map<Integer, String> departmentCache = Maps.newConcurrentMap();

    @PostConstruct
    public void init(){
        setBaseMapper(teamMapper);
    }


    /**
     * 刷新缓存
     */
    public void freshCache(){
        departmentCache = Maps.newConcurrentMap();
    }


    /**
     * 根据部门名称模糊查询出所有相关的部门id
     * @param departName
     * @return
     */
    public List<Integer> listDepartIdsByDepartName(String departName) {
        if (StringUtils.isEmpty(departName)) {
            return Lists.newArrayList();
        }
        Team team = new Team();
        team.setDepartName(departName);
        List<TeamVO> teams = this.list(team);

        if (CollectionUtils.isEmpty(teams)) {
            return Lists.newArrayList();
        }

        return teams.stream()
                .map(Team::getDepartId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据departId获取departName
     * @param departId
     * @return
     */
    public String getDepartNameByDepartId(Integer departId){
        Assert.notNull(departId);

        if (departmentCache.get(departId) != null) {
            return departmentCache.get(departId);
        }
        Team  team = new Team();
        team.setDepartId(departId);
        TeamVO teamVO = this.get(team);
        if (teamVO == null) {
            return null;
        }
        String departName = teamVO.getDepartName();
        departmentCache.put(departId, departName);
        return departName;
    }

    /**
     * truncate sc_team表中的数据
     */
    public void clearData(){
        teamMapper.truncate();
    }

}
