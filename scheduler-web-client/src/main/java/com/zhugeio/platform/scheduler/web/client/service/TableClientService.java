package com.zhugeio.platform.scheduler.web.client.service;

import java.util.List;

import com.zhugeio.platform.scheduler.web.client.dto.*;
import com.zhugeio.platform.scheduler.web.client.enums.DbTypeEnum;
import com.zhugeio.platform.scheduler.web.client.vo.TableConnectVo;
import com.zhugeio.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.zhugeio.platform.scheduler.web.client.vo.TaskVO;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.bean.Pager;
import com.zhugeio.platform.scheduler.web.client.dto.*;

public interface TableClientService {

	/**
	 * 通过表，库，以及链接host 获取这表的链接信息
	 * 
	 * @param dto 
	 * @return
	 */
	public BaseResult<TableConnectVo> getConnect(TableConnectDto dto);
	
	
	/**
	 * 获取对应数据库类型的所有表信息
	 * 
	 * @param types
	 * @return
	 */
	public BaseResult<List<TableConnectVo>> listConnect(List<DbTypeEnum> types);
	
	
	/**
	 * 根据表分页查询task
	 * 
	 * @param dto
	 * @return
	 */
	public BaseResult<PageUtils<TaskVO>> listTasks(TableConnectDto dto, Pager pager);


	/**
	 * 每次调用接口，只返回一层父和一层子数据
	 *
	 * @param dbName
	 * @param dbHost
	 * @param tableName
	 * @return
	 */
	public BaseResult<TableLineageGraphVo> getTableLineageGraph(String dbName, String dbHost, String tableName);


	/**
	 * 每次调用接口，只返回一层父和一层子数据，点击某个父或子，通过id再查询数据
	 *
	 * @param tableId id用于获取某个子节点的数据
	 * @return
	 */
	public BaseResult<TableLineageGraphVo> getTableLineageGraph(Long tableId);

	/**
	 * 告警订阅
	 * @param  subscribeDto
	 * @return
	 */
	BaseResult<Boolean> subscribeAlert(SimpleSubscribeDto subscribeDto);

	/**
	 * 取消订阅
	 * @param subscribeDto
	 * @return
	 */
	BaseResult<Boolean> cancelSubscribe(SimpleSubscribeDto subscribeDto);

	/**
	 * 获取指定表所有的订阅用户列表
	 * @param tableInfoDto
	 * @return
	 */
	BaseResult<List<SubscribeUserDto>> listSubscribeUser(TableInfoDto tableInfoDto);

	/**
	 * 检测指定用户是否已订阅某张表的通知
	 * @param checkSubscribeDto
	 * @return
	 */
	BaseResult<Boolean> isSubscribe(CheckSubscribeDto checkSubscribeDto);


	/**
	 * 通过数据库表获取job的创建用户
	 * @param dbName, tableName
	 * @return
	 */
	BaseResult<List<UserDto>> listTableJobCreateUser(String dbName, String tableName);

}
