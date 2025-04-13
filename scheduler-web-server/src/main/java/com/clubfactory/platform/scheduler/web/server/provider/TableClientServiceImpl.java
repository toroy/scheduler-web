package com.clubfactory.platform.scheduler.web.server.provider;

import com.clubfactory.platform.scheduler.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.bean.Pager;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.enums.JobCategoryEnum;
import com.clubfactory.platform.scheduler.dal.enums.TaskStatusEnum;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.client.dto.*;
import com.clubfactory.platform.scheduler.web.client.enums.DbTypeEnum;
import com.clubfactory.platform.scheduler.web.client.service.TableClientService;
import com.clubfactory.platform.scheduler.web.client.vo.TableConnectVo;
import com.clubfactory.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.clubfactory.platform.scheduler.web.client.vo.TaskVO;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.vo.*;
import com.clubfactory.platform.scheduler.web.server.dto.CipherTextDto;
import com.clubfactory.platform.scheduler.web.server.service.DataSourceBizService;
import com.clubfactory.platform.scheduler.web.server.service.JobDetailBizService;
import com.clubfactory.platform.scheduler.web.server.service.SubscribeBizService;
import com.clubfactory.platform.scheduler.web.server.service.basic.TableOnlineLineageBasicService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TableClientServiceImpl implements TableClientService {

	@Resource
	CollectDbService collectDbService;
	@Resource
	TaskService taskService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	TableOnlineLineageService tableOnlineLineageService;
	@Resource
	TableOnlineLineageBasicService tableOnlineLineageBasicService;
	@Resource
    DataSourceBizService dataSourceBizService;
	@Resource
	SubscribeBizService subscribeBizService;
	@Resource
	UserGroupRelService userGroupRelService;
	@Resource
	AlertSubService alertSubService;
	@Resource
	SubGroupRelService subGroupRelService;
	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	UserService userService;

	private static final String DEFAULT = "default";
	
	@Override
	public BaseResult<TableConnectVo> getConnect(TableConnectDto dto) {
		CollectDb collectDb = collectDbService.get(dto.getDbHost(), dto.getDbName());
		if (collectDb == null) {
			collectDb = collectDbService.getHive(dto.getDbHost());
			if (collectDb == null) {
				return new BaseResult<TableConnectVo>(new TableConnectVo());
			}
		}
		TableConnectVo tableConnectVo = new TableConnectVo();
		BeanUtil.copyBeanNotNull2Bean(collectDb, tableConnectVo);
		return new BaseResult<TableConnectVo>(tableConnectVo);
	}

	@Override
	public BaseResult<List<TableConnectVo>> listConnect(List<DbTypeEnum> types) {
		
		List<String> dbTypes = types.stream().map(DbTypeEnum::name).collect(Collectors.toList());
		List<CollectDbVO> collectDbVOs = collectDbService.listByDbTypes(dbTypes);
		if (CollectionUtils.isEmpty(collectDbVOs)) {
			return new BaseResult<List<TableConnectVo>>(Lists.newArrayList());
		}
		// 从血缘表获取信息
        List<TableConnectVo> tableConnectVos = listTableConnect(collectDbVOs, dbTypes);
		List<TableConnectVo> connectVos = distinct(tableConnectVos);
		
		return new BaseResult<List<TableConnectVo>>(connectVos);
	}


    private List<TableConnectVo> listTableConnect(List<CollectDbVO> collectDbVOs, List<String> dbTypes) {
        Set<TableConnectVo> tableConnectVos = Sets.newHashSet();
        List<TableOnlineLineageVO> lineageVOs = tableOnlineLineageService.listAll();
        if (CollectionUtils.isNotEmpty(lineageVOs)) {
			for (TableOnlineLineageVO lineageVO : lineageVOs) {
				for (CollectDbVO db : collectDbVOs) {
					if (!dbTypes.contains(db.getDsType().name())) {
						continue;
					}
					if (db.getDsType().equals(lineageVO.getDbType())
							&& StringUtils.equals(lineageVO.getDbName(), db.getDbName())
							&& StringUtils.equals(lineageVO.getDbHost(), db.getDbHost())) {
						tableConnectVos.add(genTableConnect(lineageVO.getTableName(), db));
					}
				}
			}
        }

        List<JobOnlineVO> jobOnlines = jobOnlineService.listByBizDb();
        if (CollectionUtils.isNotEmpty(jobOnlines)) {
        	for (JobOnlineVO jobOnlineVO : jobOnlines) {
				for (CollectDbVO db : collectDbVOs) {
					if (!dbTypes.contains(db.getDsType().name())) {
						continue;
					}
					if (db.getId().equals(jobOnlineVO.getDbSourceId())
							&& jobOnlineVO.getCategroy() == JobCategoryEnum.COLLECT) {
						String tableName = jobDetailBizService.getTableName(jobOnlineVO.getSourceTable());
						tableConnectVos.add(genTableConnect(tableName, db));
					}
					if (db.getId().equals(jobOnlineVO.getDbTargetId())
							&& jobOnlineVO.getCategroy() == JobCategoryEnum.REFLUE) {
						String tableName = jobDetailBizService.getTableName(jobOnlineVO.getTargetTable());
						tableConnectVos.add(genTableConnect(tableName, db));
					}
				}
			}
		}
        return Lists.newArrayList(tableConnectVos);
    }

    private List<TableConnectVo> distinct(List<TableConnectVo> tableConnectVos) {
		if (CollectionUtils.isEmpty(tableConnectVos)) {
			return Lists.newArrayList();
		}
		
		List<TableConnectVo> newTableConnectVos = Lists.newArrayList();
		Map<String, Object> map = Maps.newHashMap();
		for (TableConnectVo tableConnectVo : tableConnectVos) {
			String key = String.format("%s_%s_%s", tableConnectVo.getDbHost(),tableConnectVo.getDbName(),tableConnectVo.getTableName());
			if (map.get(key) == null) {
				newTableConnectVos.add(tableConnectVo);
				map.put(key, true);
			}
		}

		return newTableConnectVos;
	}

	private TableConnectVo genTableConnect(String targetTable, CollectDbVO db) {
		TableConnectVo tableConnectVo = new TableConnectVo();
		BeanUtil.copyBeanNotNull2Bean(db, tableConnectVo);
		tableConnectVo.setTableName(targetTable);
		tableConnectVo.setDsType(DbTypeEnum.valueOf(db.getDsType().name()));
		if (targetTable.contains(".")) {
			String[] targetTableArr = StringUtils.split(targetTable, ".");
			tableConnectVo.setDbName(targetTableArr[0]);
			tableConnectVo.setTableName(targetTableArr[1]);
		}

		//解密
        String dbPassword = dataSourceBizService.decrypt(new CipherTextDto(db.getEncryptPwd(), db.getPwdKey()));
        tableConnectVo.setDsPassword(dbPassword);
		return tableConnectVo;
	}

	@Override
	public BaseResult<PageUtils<TaskVO>> listTasks(TableConnectDto dto, Pager pager) {
        List<Long> jobIds = getJobIdByLineage(dto);
        if (CollectionUtils.isEmpty(jobIds)) {
            return new BaseResult<PageUtils<TaskVO>>();
        }

        PageUtils<Task> pages = pageTasks(pager, jobIds);
        if (CollectionUtils.isEmpty(pages.getRows())) {
            return new BaseResult<PageUtils<TaskVO>>();
        }
        List<TaskVO> taskVos = pages.getRows().stream().map(taskDto -> {
            TaskVO taskVO = new TaskVO();
            BeanUtil.copyBeanNotNull2Bean(taskDto, taskVO);
            return taskVO;
        }).collect(Collectors.toList());
        return new BaseResult<PageUtils<TaskVO>>(new PageUtils<TaskVO>(taskVos, pages.getTotalCount(), pages.getPageSize(), pages.getPageNo()));
    }

	private PageUtils<Task> pageTasks(Pager pager, List<Long> jobIds) {
		Task task = new Task();
		task.setPageNo(pager.getPageNo());
		task.setPageSize(pager.getPageSize());
		task.setIds(jobIds);
		task.setIsDeleted(false);
		task.setStatus(TaskStatusEnum.SUCCESS);
		task.setQueryListFieldName("job_id");
		task.setOrderBy("start_time desc");
        return taskService.pageList(task);
	}

	private List<Long> getJobIdByLineage(TableConnectDto dto) {
		List<TableOnlineLineageVO> lineageVOs = Lists.newArrayList();
		if (dto.getDbType() == DbTypeEnum.HIVE) {
			lineageVOs = tableOnlineLineageService.listByChild(dto.getDbName(), dto.getTableName());
		} else {
			lineageVOs = tableOnlineLineageService.listByChild(dto.getDbHost(), dto.getDbName(), dto.getTableName());
		}
		if (CollectionUtils.isEmpty(lineageVOs)) {
			return Lists.newArrayList();
		}
		return lineageVOs.stream().map(TableOnlineLineageVO::getJobId).distinct().collect(Collectors.toList());
	}

	private String getTableName(TableConnectDto connectDto, String tableName) {
		String targetTable = tableName;
		if (StringUtils.contains(targetTable, ".")) {
			targetTable = connectDto.getDbName() + "." + connectDto.getTableName();
		}
		return targetTable;
	}


	@Override
	public BaseResult<TableLineageGraphVo> getTableLineageGraph(String dbName, String dbHost, String tableName) {
		return tableOnlineLineageBasicService.getTableLineageGraph(dbName, dbHost, tableName);
	}


	@Override
	public BaseResult<TableLineageGraphVo> getTableLineageGraph(Long tableId) {
		return tableOnlineLineageBasicService.getTableLineageGraph(tableId);
	}

	@Override
	public BaseResult<Boolean> subscribeAlert(SimpleSubscribeDto subscribeDto) {
		try {
			this.subscribeBizService.defaultSubscribe(subscribeDto);
			return new BaseResult<>(true);
		} catch (Exception e) {
			log.error("订阅失败", e);
			return new BaseResult<>(BaseResult.FALIED, e.getMessage());
		}
	}

	@Override
	public BaseResult<Boolean> cancelSubscribe(SimpleSubscribeDto subscribeDto) {
		try {
			this.subscribeBizService.cancelSubscribe(subscribeDto);
			return new BaseResult<>(true);
		} catch (Exception e) {
			log.error("取消订阅失败", e);
			return new BaseResult<>(BaseResult.FALIED, e.getMessage());
		}
	}

	@Override
	public BaseResult<List<SubscribeUserDto>> listSubscribeUser(TableInfoDto tableInfoDto) {
		try {
			Assert.nonNull(tableInfoDto, "订阅表信息不能为空");
			AlertSub alertSubWhere = new AlertSub();
			alertSubWhere.setIsDeleted(false);
			alertSubWhere.setPicId(tableInfoDto.getPicUid());
			alertSubWhere.setTableName(tableInfoDto.getTableName());
			alertSubWhere.setDbName(tableInfoDto.getDbName());
			alertSubWhere.setDataSource(tableInfoDto.getDataSource());
			List<AlertSubVO> alertSubList = this.alertSubService.list(alertSubWhere);
			if (CollectionUtils.isEmpty(alertSubList)) {
				return new BaseResult<>(Lists.newArrayList());
			}
			List<Long> subscribeIdList = alertSubList.stream().filter(alertSubVO -> alertSubVO.getId() != null)
					.map(AlertSubVO::getId).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(subscribeIdList)) {
				return new BaseResult<>(Lists.newArrayList());
			}
			List<SubGroupRel> subGroupRelList = this.subGroupRelService.listPoByField("sub_id", subscribeIdList);
			if (CollectionUtils.isEmpty(subGroupRelList)) {
				return new BaseResult<>(Lists.newArrayList());
			}
			List<Long> groupIdList = subGroupRelList.stream().filter(subGroupRel -> subGroupRel.getGroupId() != null)
					.map(SubGroupRel::getGroupId).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(groupIdList)) {
				return new BaseResult<>(Lists.newArrayList());
			}
			List<UserInfo> userInfoList = this.userGroupRelService.listUserInfosByGroupIds(groupIdList);
			if (CollectionUtils.isEmpty(userInfoList)) {
				return new BaseResult<>(Lists.newArrayList());
			}
			List<SubscribeUserDto>  subscribeUserDtoList = userInfoList.stream().map(userInfo -> {
				SubscribeUserDto subscribeUserDto = new SubscribeUserDto();
				BeanUtil.copyBeanNotNull2Bean(userInfo, subscribeUserDto);
				subscribeUserDto.setUsername(userInfo.getName());
				return subscribeUserDto;
			}).collect(Collectors.toList());
			return new BaseResult<>(subscribeUserDtoList);
		} catch (Exception e) {
			log.error("获取订阅用户列表失败", e);
			return new BaseResult<>(BaseResult.FALIED, "获取订阅用户列表失败");
		}
	}

	@Override
	public BaseResult<Boolean> isSubscribe(CheckSubscribeDto checkSubscribeDto) {
		try {
			boolean isSubscribe = this.subscribeBizService.isSubscribe(checkSubscribeDto);
			return new BaseResult<>(isSubscribe);
		} catch (Exception e) {
			return new BaseResult<>(BaseResult.FALIED, e.getMessage());
		}
	}

	public BaseResult<List<UserDto>> listTableJobCreateUser(String dbName, String tableName) {
		List<TableOnlineLineageVO> tableOnlineLineageVOS = tableOnlineLineageService.listByChild(dbName, tableName);
		if (CollectionUtils.isEmpty(tableOnlineLineageVOS)) {
			log.warn("没有查到具体table_online_lineage信息, dbName:{}, tableName:{}", dbName, tableName);
			return new BaseResult<List<UserDto>>(Lists.newArrayList());
		}
		List<Long> jobIds = new ArrayList<>(tableOnlineLineageVOS.stream().map(TableOnlineLineageVO::getJobId).collect(Collectors.toSet()));
		List<JobOnline> jobOnlines = jobOnlineService.listByIds(jobIds);

		if (CollectionUtils.isEmpty(jobOnlines)) {
			log.warn("没有查到具体job_online信息, dbName:{}, tableName:{}", dbName, tableName);
			return new BaseResult<List<UserDto>>(Lists.newArrayList());
		}

		List<Long> createUsers = jobOnlines.stream().map(JobOnline::getCreateUser).collect(Collectors.toList());
		List<UserVO> userVOS = userService.listByIds(createUsers);
		List<UserDto> userDtos = userVOS.stream().map(userVO -> {
			UserDto userDto = new UserDto();
			userDto.setUid(userVO.getUid());
			userDto.setName(userVO.getName());
			userDto.setDepartId(userVO.getDepartId());
			userDto.setDepartName(userVO.getDepartName());
			return userDto;
		}).collect(Collectors.toList());
		return new BaseResult<>(userDtos);
	}
}
