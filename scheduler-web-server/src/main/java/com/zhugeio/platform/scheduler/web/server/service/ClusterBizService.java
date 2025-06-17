package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.utils.SelectDictUtils;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.po.Cluster;
import com.zhugeio.platform.scheduler.web.core.dto.ClusterDto;
import com.zhugeio.platform.scheduler.web.core.dto.ClusterEditDto;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.web.core.service.ClusterService;
import com.zhugeio.platform.scheduler.web.core.service.TeamService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.vo.ClusterVO;
import com.zhugeio.platform.scheduler.web.core.vo.DepartmentVo;
import com.zhugeio.platform.scheduler.web.server.dto.ClusterPagerDto;
import com.zhugeio.platform.scheduler.web.server.service.inter.ConditionCompleter;
import com.zhugeio.platform.scheduler.web.server.service.inter.ViewTransformer;
import com.zhugeio.platform.scheduler.web.core.utils.JobTypeCache;
import com.zhugeio.platform.scheduler.web.server.vo.ClusterVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
public class ClusterBizService {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamBizService teamBizService;


    /**
     * 新增集群
     * @param userDto
     * @param clusterDto
     */
    public void addCluster(LoginUserDto userDto, ClusterDto clusterDto){
        Assert.notNull(userDto);
        Assert.notNull(clusterDto);
        if (!userDto.getIsAdmin()){
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("新增集群",userDto.getName()));
        }
        clusterService.addCluster(clusterDto,userDto.getLocalUserId());

    }

    /**
     * 修改集群信息
     * @param userDto
     * @param editDto
     */
    public void editCluster(LoginUserDto userDto, ClusterEditDto editDto){
        Assert.notNull(userDto);
        Assert.notNull(editDto);
        if (!userDto.getIsAdmin()){
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("修改集群信息",userDto.getName()));
        }
        clusterService.editClusterInfo(editDto,userDto.getLocalUserId(),false);
    }

    /**
     * 软删除集群信息
     * @param userDto
     * @param clusterId
     */
    public void delCluster(LoginUserDto userDto, Long clusterId){
        Assert.notNull(userDto);
        Assert.notNull(clusterId);
        if (!userDto.getIsAdmin()){
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("删除集群信息",userDto.getName()));
        }
        ClusterEditDto editDto = new ClusterEditDto();
        editDto.setId(clusterId);
        clusterService.editClusterInfo(editDto,userDto.getLocalUserId(),true);

    }

    /**
     * 根据ID获取集群信息
     * @param clusterId
     * @return
     */
    public ClusterVo getClusterById(Long clusterId){
        Assert.notNull(clusterId);
        Cluster cluster = new Cluster();
        cluster.setIsDeleted(false);
        cluster.setId(clusterId);
        ClusterVO vo = clusterService.get(cluster);
        if (vo == null){
            return null;
        }
        ClusterVo clusterVo  = new ClusterVo();
        BeanUtil.copyBeanNotNull2Bean(vo,clusterVo);
        if (vo.getCreateUser() != null) {
            clusterVo.setCreateUser(userService.getUserName(vo.getCreateUser()));
        }

        completeVo(vo,clusterVo);
        return clusterVo;
    }

    /**
     * 分页查询集群列表
     * @param pagerDto
     * @return
     */
    public PageUtils<ClusterVo> queryByPage(ClusterPagerDto pagerDto){

        ViewTransformer<Cluster,ClusterVo> viewTransformer = po -> {
            ClusterVo vo = new ClusterVo();
            if (po != null) {
                BeanUtil.copyBeanNotNull2Bean(po, vo);
            }else {
                return vo;
            }
            if(po.getCreateUser() != null) {
                vo.setCreateUser(userService.getUserName(po.getCreateUser()));
            }
            completeVo(po,vo);
            vo.setProxyPassword("******");
            return vo;
        };

        ConditionCompleter<Cluster,ClusterVo> conditionCompleter = cluster -> {
            List<Long> userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), null);
            if (CollectionUtils.isNotEmpty(userIds)) {
                return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
            cluster.setIds(userIds);
            cluster.setQueryListFieldName("create_user");
            return null;
        };

        return queryByPage(pagerDto,conditionCompleter,viewTransformer);
    }

    /**
     * 分页查询集群列表
     * @param pagerDto
     * @return
     */
    public PageUtils<ClusterVo> queryByPage(ClusterPagerDto pagerDto,
                                            ConditionCompleter<Cluster,ClusterVo> conditionCompleter,
                                            ViewTransformer<Cluster,ClusterVo> voViewTransformer){
        Assert.notNull(pagerDto);

        Cluster cluster = new Cluster();
        cluster.setPageNo(pagerDto.getPageNo());
        cluster.setPageSize(pagerDto.getPageSize());
        cluster.setIsDeleted(false);
        cluster.setClusterName(pagerDto.getClusterName());
        cluster.setUrl(pagerDto.getUrl());

        if (StringUtils.isNotEmpty(pagerDto.getCreateUser())) {
            PageUtils<ClusterVo> result = conditionCompleter.complete(cluster);
            if (result != null){
                return result;
            }
        }
        if (StringUtils.isNotEmpty(pagerDto.getDepartName())){
            List<Integer> departIds = userService.listDepartIdsByDepartName(pagerDto.getDepartName());
            if (CollectionUtils.isEmpty(departIds)){
                return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
            cluster.setDepartIds(departIds);
        }

        PageUtils<Cluster> pages = clusterService.pageList(cluster);
        // 返回数据
        if (pages.getSize() == 0) {
            return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        List<ClusterVo> schedulerNodeVos = pages.getRows()
                .stream()
                .map(po -> voViewTransformer.transform(po))
                .collect(Collectors.toList());
        return new PageUtils<ClusterVo>(schedulerNodeVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
    }


    /**
     * 获取集群相关的枚举
     * @return
     */
    public Map getEnums(){
        Map<String, List> enums =  SelectDictUtils.getStatusAndFeatureEnums(JobTypeCache.allType());
        List<DepartmentVo> departmentVos = teamBizService.listDepartments();
        enums.put("teams",departmentVos);
        return enums;
    }


    /**
     *
     * @param  po
     * @param  vo
     */
    private void completeVo(Cluster po,ClusterVo vo){
        if (po.getDepartId() != null){
            vo.setDepartId(po.getDepartId());
            vo.setDepartName(teamService.getDepartNameByDepartId(po.getDepartId()));
        }

        if (po.getStatus() != null){
            vo.setStatus(po.getStatus().name());
            vo.setStatusDesc(po.getStatus().getDesc());
        }
        
        if (po.getType() != null) {
            vo.setType(po.getType().name());
        	vo.setTypeDesc(po.getType().getDesc());
        }
    }

}
