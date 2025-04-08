package com.clubfactory.platform.scheduler.web.server.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.clubfactory.platform.scheduler.dal.enums.CommonStatus;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;
import com.clubfactory.platform.scheduler.web.core.service.JobService;
import com.clubfactory.platform.scheduler.web.server.service.inter.ConditionCompleter;
import com.clubfactory.platform.scheduler.web.core.utils.JobTypeCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.Machine;
import com.clubfactory.platform.scheduler.web.core.dto.SchedulerNodeDto;
import com.clubfactory.platform.scheduler.web.core.dto.SchedulerNodeEditDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.service.MachineService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.vo.MachineVO;
import com.clubfactory.platform.scheduler.web.server.dto.SchedulerNodePagerDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.utils.SelectDictUtils;
import com.clubfactory.platform.scheduler.web.server.vo.SchedulerNodeVo;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiejiajun
 */
@Service
@Slf4j
public class SchedulerNodeBizService {


    @Autowired
    private MachineService machineService;
    @Autowired
    private UserService userService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobOnlineService jobOnlineService;


    /**
     * 新增调度机节点
     * @param userDto
     * @param nodeDto
     */
    public void addNode(LoginUserDto userDto, SchedulerNodeDto nodeDto){
        Assert.notNull(userDto);
        Assert.notNull(nodeDto);
        if (!userDto.getIsAdmin()){
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("新增调度机",userDto.getName()));
        }
        machineService.addMachine(nodeDto,userDto.getLocalUserId());
    }

    /**
     * 编辑调度机节点信息
     * @param userDto
     * @param editDto
     */
    public void editNode(LoginUserDto userDto, SchedulerNodeEditDto editDto){
        Assert.notNull(userDto);
        Assert.notNull(editDto);
        Long id = editDto.getId();
        Assert.notNull(id);
        if (!userDto.getIsAdmin()) {
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("修改调度机信息",userDto.getName()));
        }

        MachineVO machineVO = machineService.getSlaveById(id);
        if (CommonStatus.DISABLED == editDto.getStatus()
                && machineVO != null) {
            List<String> jobNames = jobService.listJobNameByMachineId(id);
            if (CollectionUtils.isNotEmpty(jobNames)) {
                throw new BizException(ErrorCode.JOB_CONTANT_MACHINE_ID.setParams(StringUtils.join(jobNames,",")));
            }
            List<String> jobOnlineNames = jobOnlineService.listJobNameByMachineId(id);
            if (CollectionUtils.isNotEmpty(jobOnlineNames)) {
                throw new BizException(ErrorCode.JOB_ONLINE_CONTANT_MACHINE_ID.setParams(StringUtils.join(jobOnlineNames,",")));
            }
        }
        machineService.editMachine(editDto,userDto.getLocalUserId(),false);
    }

    /**
     * 软删除调度机
     * @param userDto
     * @param nodeId
     */
    public void delNode(LoginUserDto userDto, Long nodeId){
        Assert.notNull(userDto);
        Assert.notNull(nodeId);
        if (!userDto.getIsAdmin()){
            throw new BizException(ErrorCode.NON_ADMIN_ERROR.setParams("删除调度机信息",userDto.getName()));
        }
        SchedulerNodeEditDto editDto = new SchedulerNodeEditDto();
        editDto.setId(nodeId);
        machineService.editMachine(editDto,userDto.getLocalUserId(),true);
    }

    /**
     * 根据id查询调度机信息
     * @param nodeId
     * @return
     */
    public SchedulerNodeVo getById(Long nodeId){
        Assert.notNull(nodeId);
        Machine machine = new Machine();
        machine.setId(nodeId);
        machine.setIsDeleted(false);
        MachineVO vo = machineService.get(machine);
        if (vo == null){
            return null;
        }
        SchedulerNodeVo schedulerNodeVo = new SchedulerNodeVo();
        BeanUtil.copyBeanNotNull2Bean(vo,schedulerNodeVo);
        schedulerNodeVo.setMachineName(vo.getName());
        schedulerNodeVo.setMachineType(vo.getType());
        completeProperties(vo,schedulerNodeVo);
        return schedulerNodeVo;
    }

    /**
     * 分页查询调度机列表
     * @param pagerDto
     * @return
     */
    public PageUtils<SchedulerNodeVo> queryByPage(SchedulerNodePagerDto pagerDto){
        return this.queryByPage(pagerDto, machine -> {
            List<Long> userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), null);
            if (CollectionUtils.isEmpty(userIds)) {
                return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
            machine.setIds(userIds);
            machine.setQueryListFieldName("create_user");
            return null;
        });
    }


    /**
     * 分页查询调度机列表
     * @param pagerDto
     * @return
     */
    public PageUtils<SchedulerNodeVo> queryByPage(SchedulerNodePagerDto pagerDto,
                                                  ConditionCompleter<Machine,SchedulerNodeVo> conditionCompleter){
        Assert.notNull(pagerDto);

        Machine machine = new Machine();
        machine.setPageNo(pagerDto.getPageNo());
        machine.setPageSize(pagerDto.getPageSize());
        machine.setIsDeleted(false);
        machine.setIp(pagerDto.getIp());
        machine.setName(pagerDto.getMachineName());

        if (StringUtils.isNotEmpty(pagerDto.getCreateUser())) {
            PageUtils<SchedulerNodeVo> pageUtils = conditionCompleter.complete(machine);
            if (pageUtils != null){
                return pageUtils;
            }
        }

        PageUtils<Machine> pages = machineService.pageList(machine);
        // 返回数据
        if (pages.getSize() == 0) {
            return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        List<SchedulerNodeVo> schedulerNodeVos = pages.getRows()
                .stream()
                .map(po -> {
                    SchedulerNodeVo vo = new SchedulerNodeVo();
                    if (po != null) {
                        BeanUtil.copyBeanNotNull2Bean(po, vo);
                    }else {
                        return vo;
                    }
                    completeProperties(po,vo);
                    vo.setMachineName(po.getName());
                    vo.setMachineType(po.getType());
                    return vo;
                })
                .collect(Collectors.toList());
        return new PageUtils<SchedulerNodeVo>(schedulerNodeVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
    }


    /**
     * 获取调度机相关的枚举
     * @return
     */
    public Map getEnums(){
        return SelectDictUtils.getSchedulerNodeEnums(JobTypeCache.allType());
    }

    /**
     * 调度机复制
     * @param nodeId
     * @param createUser
     */
    public void copyNode(Long nodeId,Long createUser){
        Assert.notNull(nodeId);
        Assert.notNull(createUser);
        Machine machine = new Machine();
        machine.setId(nodeId);
        machine.setIsDeleted(false);
        MachineVO vo = machineService.get(machine);
        if (vo == null){
            throw new BizException("找不到被复制的节点或被复制的节点已被删除");
        }
        SchedulerNodeDto nodeDto = new SchedulerNodeDto();
        BeanUtil.copyBeanNotNull2Bean(vo,nodeDto);
        nodeDto.setMachineName(String.format("%s_副本",vo.getName()));
        nodeDto.setMachineType(vo.getType());
        if (vo.getFunctions() != null) {
            nodeDto.setFunctions(vo.getFunctions());
        }
        machineService.addMachine(nodeDto,createUser);
    }


    /**
     * 根据po补全vo属性
     * @param po
     * @param vo
     */
    private void completeProperties(Machine po,SchedulerNodeVo vo){
        if(po.getCreateUser() != null) {
            vo.setCreateUser(userService.getUserName(po.getCreateUser()));
        }

        if (po.getStatus() != null){
            vo.setStatus(po.getStatus().name());
            vo.setStatusDesc(po.getStatus().getDesc());
        }

        if (po.getFunctions() != null){
            // 调度机功能描述描述(对应作业类型中文描述)
            vo.setFunctionsDesc(JobTypeCache.getJobTypeByFunction(po.getFunctions()).getType());
        }
    }


}
