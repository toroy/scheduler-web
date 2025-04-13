package com.clubfactory.platform.scheduler.web.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.dao.ClusterMapper;
import com.clubfactory.platform.scheduler.dal.enums.ClusterTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.CommonStatus;
import com.clubfactory.platform.scheduler.dal.po.Cluster;
import com.clubfactory.platform.scheduler.web.core.dto.ClusterDto;
import com.clubfactory.platform.scheduler.web.core.dto.ClusterEditDto;
import com.clubfactory.platform.scheduler.web.core.vo.ClusterVO;

/**
 * @author xiejiajun
 */
@Service
public class ClusterService extends BaseNewService<ClusterVO, Cluster> {

    @Resource
    ClusterMapper clusterMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(clusterMapper);
    }
    
    public Long getClusterIdByType(ClusterTypeEnum clusterType, String function) {
    	Cluster cluster = new Cluster();
        cluster.setIsDeleted(false);
        cluster.setStatus(CommonStatus.ENABLED);
        cluster.setType(clusterType);
        cluster.setFunctions(function);
        List<ClusterVO> clusters = this.list(cluster);
        if (CollectionUtils.isEmpty(clusters)) {
        	return null;
        }
        return clusters.stream().map(Cluster::getId).findAny().get();
    }
    
    /**
     * 取同一个部门，同一类型的线上集群
     * 
     * @param type
     * @param departId
     * @return
     */
    public Long getClusterByJob(String type, Integer departId) {
    	Assert.notNull(type);
        Assert.notNull(departId);
        
        Cluster cluster = new Cluster();
        cluster.setIsDeleted(false);
        cluster.setStatus(CommonStatus.ENABLED);
        cluster.setType(ClusterTypeEnum.ONLINE);
        cluster.setFunctions(type);
        List<ClusterVO> clusters = this.list(cluster);
        if (CollectionUtils.isEmpty(clusters)) {
        	return null;
        }
        List<Long> ids = clusters.stream().filter(vo -> vo.getDepartId().equals(departId)).map(ClusterVO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
        	return null;
        }
        return ids.stream().findAny().get();
    }

    
    /**
     * 添加集群
     * @param dto
     * @param createUser
     */
    public void addCluster(ClusterDto dto,Long createUser){
        Assert.notNull(dto);
        Assert.notNull(createUser);

        Cluster cluster = new Cluster();
        BeanUtil.copyBeanNotNull2Bean(dto,cluster);

        cluster.setIsDeleted(false);
        cluster.setCreateUser(createUser);
        cluster.setUpdateUser(createUser);

        this.save(cluster);
    }

    /**
     * 修改集群信息
     * @param editDto
     * @param updateUser
     * @param isDelete
     */
    public void editClusterInfo(ClusterEditDto editDto,Long updateUser,boolean isDelete){
        Assert.notNull(editDto);
        Assert.notNull(updateUser);
        Assert.notNull(editDto.getId());

        Cluster cluster = new Cluster();
        cluster.setId(editDto.getId());
        cluster.setIsDeleted(false);

        Map<String,Object> updateParams = BeanUtil.copyBeanCamel2MapUnder(editDto,null);
        updateParams.remove("yarn_r_m_hosts");
        updateParams.remove("yarn_r_m_http_port");
        if (StringUtils.isNotBlank(editDto.getYarnRMHosts())){
            updateParams.put("yarn_rm_hosts",editDto.getYarnRMHosts());
        }
        if (StringUtils.isNotBlank(editDto.getYarnRMHttpPort())){
            updateParams.put("yarn_rm_http_port",editDto.getYarnRMHttpPort());
        }
        if (StringUtils.isNotBlank(editDto.getFunctions())){
            updateParams.put("functions",editDto.getFunctions());
        }
        updateParams.put("update_user",updateUser);
        if (isDelete){
            updateParams.put("is_deleted",true);
        }

        cluster.setUpdateParam(updateParams);
        this.edit(cluster);
    }

}
