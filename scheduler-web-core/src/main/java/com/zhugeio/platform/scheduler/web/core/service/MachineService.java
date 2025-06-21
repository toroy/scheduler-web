package com.zhugeio.platform.scheduler.web.core.service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.core.dto.SchedulerNodeDto;
import com.zhugeio.platform.scheduler.web.core.dto.SchedulerNodeEditDto;
import com.zhugeio.platform.scheduler.web.core.vo.MachineVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.MachineMapper;
import com.zhugeio.platform.scheduler.dal.enums.CommonStatus;
import com.zhugeio.platform.scheduler.dal.enums.MachineTypeEnum;
import com.zhugeio.platform.scheduler.dal.po.Machine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class MachineService extends BaseNewService<MachineVO,Machine> {

    @Resource
    MachineMapper machineMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(machineMapper);
    }
    
    public List<MachineVO> listSlaves() {
    	Machine machine = new Machine();
		machine.setIsDeleted(false);
		machine.setStatus(CommonStatus.ENABLED);
		machine.setType(MachineTypeEnum.WORKER);
		return this.list(machine);
    }
    
    public MachineVO getSlaveById(Long id) {
    	Assert.notNull(id);
    	Machine machine = new Machine();
		machine.setIsDeleted(false);
		machine.setId(id);
		machine.setStatus(CommonStatus.ENABLED);
		machine.setType(MachineTypeEnum.WORKER);
		return this.get(machine);
    }

	/**
	 * @return
	 */
    public Map<String, List<MachineVO>> getSlavesMapByType() {
    	List<MachineVO> machineVOs = this.listSlaves();
    	if (CollectionUtils.isEmpty(machineVOs)) {
    		return Maps.newHashMap();
    	}
    	return machineVOs.stream().collect(Collectors.groupingBy(MachineVO::getFunctions));
    }
    

    public Boolean isMaster(String ip) {
    	Assert.notNull(ip);
    	Machine machine = new Machine();
    	machine.setIsDeleted(false);
    	machine.setType(MachineTypeEnum.MASTER);
    	machine.setIp(ip);
    	Machine res = this.get(machine);
    	
    	if (res != null) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public Map<String, String> getNameMapByIp() {
    	List<MachineVO> machines = listByNotDeleted();
    	if (CollectionUtils.isEmpty(machines)) {
    		return Maps.newHashMap();
    	}
    	return machines.stream().collect(Collectors.toMap(MachineVO::getIp, MachineVO::getName, (oldvalue, newValue) -> newValue));
    }
    
    public Map<String, Long> getIdMapByIp() {
    	List<MachineVO> machines = listByNotDeleted();
    	if (CollectionUtils.isEmpty(machines)) {
    		return Maps.newHashMap();
    	}
    	return machines.stream().collect(Collectors.toMap(MachineVO::getIp, MachineVO::getId, (oldvalue, newValue) -> newValue));
    }

	private List<MachineVO> listByNotDeleted() {
		Machine machine = new Machine();
    	machine.setIsDeleted(false);
    	return this.list(machine);
	}
    
    public Map<Long, String> getNameMap() {
    	List<MachineVO> machines = listByNotDeleted();
    	if (CollectionUtils.isEmpty(machines)) {
    		return Maps.newHashMap();
    	}
    	return machines.stream().collect(Collectors.toMap(MachineVO::getId, MachineVO::getName));
    }


	/**
	 * 新增调度机
	 * @param nodeDto
	 * @param createUser
	 */
    public void addMachine(SchedulerNodeDto nodeDto, Long createUser){
		Assert.notNull(createUser);
		Assert.notNull(nodeDto);
		Assert.notNull(nodeDto.getMachineType());
		if (nodeDto.getSlots() == null || nodeDto.getSlots() < 0){
			nodeDto.setSlots(0);
		}

		Machine machine = new Machine();
		machine.setIp(nodeDto.getIp());
		machine.setIsSelf(false);
		machine.setIsDeleted(false);
		machine.setType(nodeDto.getMachineType());
		machine.setCreateUser(createUser);
		machine.setUpdateUser(createUser);
		if (nodeDto.getFunctions() != null && nodeDto.getMachineType() != MachineTypeEnum.MASTER){
			machine.setFunctions(nodeDto.getFunctions());
		}
		machine.setStatus(nodeDto.getStatus());
		machine.setSlots(nodeDto.getSlots());
		machine.setName(nodeDto.getMachineName());

		this.save(machine);
	}


	/**
	 * 更新调度机
	 * @param editDto
	 * @param updateUser
	 * @param isDelete
	 */
	public void editMachine(SchedulerNodeEditDto editDto, Long updateUser, boolean isDelete){
		Assert.notNull(editDto);
		Assert.notNull(editDto.getId());
		Assert.notNull(updateUser);

		MachineVO vo = getMachineById(editDto.getId());

		Machine machine = new Machine();
		machine.setId(editDto.getId());
		machine.setIsDeleted(false);

		Map<String,Object> updateParams = Maps.newHashMap();
		updateParams.put("update_user",updateUser);
		if (StringUtils.isNotEmpty(editDto.getMachineName())){
			updateParams.put("name",editDto.getMachineName());
		}
		if (StringUtils.isNotEmpty(editDto.getIp())){
			updateParams.put("ip",editDto.getIp());
		}
		if (editDto.getMachineType() != null){
			updateParams.put("type",editDto.getMachineType());
		}
		if (editDto.getStatus() != null){
			updateParams.put("status",editDto.getStatus());
		}
		if (editDto.getSlots() != null && editDto.getSlots() >= 0){
			updateParams.put("slots",editDto.getSlots());
		}

		if (editDto.getFunctions() != null){
			if (editDto.getMachineType() == null){
				if ((vo != null && MachineTypeEnum.WORKER == vo.getType())){
					updateParams.put("`functions`",editDto.getFunctions());
				}
			}else if (editDto.getMachineType() == MachineTypeEnum.WORKER ) {
				updateParams.put("`functions`", editDto.getFunctions());
			}
		}
		if (isDelete){
			updateParams.put("is_deleted",true);
		}

		machine.setUpdateParam(updateParams);
		this.edit(machine);

	}
	
    public Map<String, String> getIpStringByJobTypeMap() {
    	Map<String, List<String>> mapList = getIpsByJobTypeMap();
    	if (MapUtils.isEmpty(mapList)) {
    		return Maps.newHashMap();
    	}
    	Map<String, String> map = genIpStringByMap(mapList);
    	return map;
    }
	
    private Map<String, List<String>> getIpsByJobTypeMap() {
		List<MachineVO> machineVos = this.listSlaves();
		if (CollectionUtils.isEmpty(machineVos)) {
			return Maps.newHashMap();
		}
		Map<String, List<String>> jobTypeMaps = Maps.newHashMap();
		for (MachineVO machineVO : machineVos) {
			if (machineVO.getFunctions() == null || StringUtils.isEmpty(machineVO.getIp())) {
				continue;
			}
			String key = machineVO.getFunctions();
			List<String> list = jobTypeMaps.get(key);
			if (CollectionUtils.isNotEmpty(list)) {
				list.add(machineVO.getIp());
				jobTypeMaps.put(key, list);
			} else {
				jobTypeMaps.put(key, Lists.newArrayList(machineVO.getIp()));
			}
		}
		return jobTypeMaps;
    }
	
	private Map<String, String> genIpStringByMap(Map<String, List<String>> mapList) {
		Map<String, String> map = Maps.newHashMap();
    	for (Entry<String, List<String>>  entries : mapList.entrySet()) {
    		
    		map.put(entries.getKey(), StringUtils.join(entries.getValue(),","));
    	}
		return map;
	}
	
	// 如果根据id取不到，就取全部的
	public Map<String, List<String>> getIpsMap(Long id) {
		Map<String, List<String>>  machineMap = this.getIpsMapByType(id);
		if (MapUtils.isEmpty(machineMap)) {
			machineMap = this.getIpsByJobTypeMap();
		}
		return machineMap;
	}
    
	public Map<String, List<String>> getIpsMapByType(Long id) {
		Assert.notNull(id);
		MachineVO vo = this.getSlaveById(id);
		if (vo == null) {
			return Maps.newHashMap();
		}
		// CAL_PYTHON COLLECT_PYTHON
		String functions = vo.getFunctions();
		String ip = vo.getIp();
		if (functions == null || StringUtils.isEmpty(ip)) {
			return Maps.newHashMap();
		}
		Map<String, List<String>> map = Maps.newHashMap();
		map.put(functions, Lists.newArrayList(ip));
    	return map;
	}


	/**
	 * 根据ID获取调度机信息
	 * @param machineId
	 * @return
	 */
	public MachineVO getMachineById(Long machineId){
		Assert.notNull(machineId);
		Machine machine = new Machine();
		machine.setId(machineId);
		machine.setIsDeleted(false);
		return this.get(machine);
	}

	/**
	 * 获取所有活跃的Worker
	 * @return
	 */
	public List<MachineVO> listActiveWorkers(){
		Machine machine = new Machine();
		machine.setIsDeleted(false);
		machine.setType(MachineTypeEnum.WORKER);
		machine.setStatus(CommonStatus.ENABLED);
		return this.list(machine).stream()
				.filter(distinctByKey(Machine::getIp))
				.collect(Collectors.toList());
	}

	public List<MachineVO> listAllWorkerInfos(){
		Machine machine = new Machine();
		machine.setIsDeleted(false);
		machine.setType(MachineTypeEnum.WORKER);
		machine.setStatus(CommonStatus.ENABLED);
		return this.list(machine);
	}


	/**
	 * 用于根据指定属性去重
	 * @param keyExtractor
	 * @param <T>
	 * @return
	 */
	private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
}
