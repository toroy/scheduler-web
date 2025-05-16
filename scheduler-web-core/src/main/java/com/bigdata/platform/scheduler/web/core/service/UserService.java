package com.bigdata.platform.scheduler.web.core.service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.UserMapper;
import com.bigdata.platform.scheduler.dal.po.User;
import com.bigdata.platform.scheduler.web.core.enums.CacheEnum;
import com.bigdata.platform.scheduler.web.core.utils.GuavaCacheUtil;
import com.bigdata.platform.scheduler.web.core.vo.DepartmentVo;
import com.bigdata.platform.scheduler.web.core.vo.UserVO;
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
public class UserService extends BaseNewService<UserVO,User> {

    @Resource
    UserMapper userMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(userMapper);
    }
    
    public List<Long> listIdsByUserNameAndDepartName(String name, String departName) {
    	if (StringUtils.isBlank(name) && StringUtils.isBlank(departName)) {
    		return Lists.newArrayList();
    	}
    	
    	User user = new User();
    	user.setDepartName(departName);
    	user.setIsDeleted(false);
    	user.setName(name);
    	List<UserVO> users = this.list(user);
    	if (CollectionUtils.isEmpty(users)) {
    		return Lists.newArrayList();
    	}
    	return users.stream().map(User::getId).collect(Collectors.toList());
    }

	public List<UserVO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		User user = new User();
		user.setIsDeleted(false);
		user.setIds(ids);
		List<UserVO> users = this.list(user);
		return users;
	}

	public Map<Long, Integer> getDepartIdMap(List<Long> ids) {
    	if (CollectionUtils.isEmpty(ids)) {
    		return Maps.newHashMap();
    	}
    	User user = new User();
    	user.setIsDeleted(false);
    	user.setIds(ids);
    	List<UserVO> users = this.list(user);
    	if (CollectionUtils.isEmpty(users)) {
    		return Maps.newHashMap();
    	}
    	return users.stream().collect(Collectors.toMap(UserVO::getId, UserVO::getDepartId));
    }

	/**
	 * 根据部门名称模糊查询出所有相关的部门id
	 * @param departName
	 * @return
	 */
	public List<Integer> listDepartIdsByDepartName(String departName) {
		List<UserVO> users = listUserByDepartName(departName);
		if (CollectionUtils.isEmpty(users)) {
			return Lists.newArrayList();
		}
		return users.stream()
				.map(User::getDepartId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * 根据部门模糊查处所有相关用户的id
	 * @param departName
	 * @return
	 */
	public List<Long> listUserIdsByDepartName(String departName){
		List<UserVO> users = listUserByDepartName(departName);
		if (CollectionUtils.isEmpty(users)) {
			return Lists.newArrayList();
		}
		return users.stream()
				.map(User::getId)
				.collect(Collectors.toList());
	}

	/**
	 * 根据部门名称查询所有用户
	 * @param departName
	 * @return
	 */
	public List<UserVO> listUserByDepartName(String departName){
		if (StringUtils.isEmpty(departName)) {
			return Lists.newArrayList();
		}

		User user = new User();
		user.setDepartName(departName);
		user.setIsDeleted(false);
		return this.list(user);
	}

	/**
	 * 根据userId获取部门名称
	 * @param id
	 * @return
	 */
    public String getDepartName(Long id) {
    	Assert.notNull(id);
    	
    	String key = CacheEnum.DEPENT.getKey(id);
    	String name = GuavaCacheUtil.get(key);
    	if (name != null) {
    		return name;
    	}
    	
    	UserVO userVO = getUserInfoById(id);
    	if (userVO == null) {
    		return null;
    	}
    	name = userVO.getDepartName();
    	
    	GuavaCacheUtil.put(key, name);
    	return name;
    }
    
    public String getUserName(Long id) {
    	Assert.notNull(id);
    	
    	String key = CacheEnum.USER.getKey(id);
    	String name = GuavaCacheUtil.get(key);
    	if (name != null) {
    		return name;
    	}
    	UserVO userVO = getUserInfoById(id);
		if (userVO == null) {
			return null;
		}
    	name = userVO.getName();
    	
    	GuavaCacheUtil.put(key, name);
    	return name;
    }

	/**
	 * 根据uid查询用户名
	 * @param uid
	 * @return
	 */
	public String getUserNameByUid(String uid) {
		Assert.notNull(uid);

		String key = CacheEnum.USER_UID.getKey(uid);
		String name = GuavaCacheUtil.get(key);
		if (name != null) {
			return name;
		}
		UserVO userVO = getUserInfoByUid(uid);
		if (userVO == null) {
			return null;
		}
		name = userVO.getName();

		GuavaCacheUtil.put(key, name);
		return name;
	}

	public Map<String, String> getUserNameMapByUid(List<String> uids) {
		if (CollectionUtils.isEmpty(uids)) {
			return Maps.newHashMap();
		}

		List<String> userIds = uids.stream().collect(Collectors.toList());

		User user = new User();
		user.setIdsString(userIds);
		user.setQueryListFieldName("uid");
		user.setIsDeleted(false);
		return this.list(user).stream().collect(Collectors.toMap(User::getUid, User::getName, (oldValue, newValue) -> newValue));
	}

	/**
	 * 根据uid查询userId
	 * @param uid
	 * @return
	 */
	public Long getUserIdByUid(String uid) {
		Assert.notNull(uid);

		String key = CacheEnum.USER_UID_ID.getKey(uid);
		Long userId = GuavaCacheUtil.get(key);
		if (userId != null) {
			return userId;
		}
		UserVO userVO = getUserInfoByUid(uid);
		if (userVO == null) {
			return null;
		}
		userId = userVO.getId();

		GuavaCacheUtil.put(key, userId);
		return userId;
	}

    /**
     * 获取微信的uid
     *
     * @param id 主键id
     * @return 微信的uid
     */
    public String getUid(Long id) {
        Assert.notNull(id);

        String key = CacheEnum.USERID.getKey(id);
        String uid = GuavaCacheUtil.get(key);
        if (uid != null) {
            return uid;
        }
        UserVO userVO = getUserInfoById(id);
        if (userVO == null) {
            return null;
        }
        uid = userVO.getUid();

        GuavaCacheUtil.put(key, uid);
        return uid;
    }


	/**
	 * 根据用户名获取用户Alias
	 * @param id 主键id
	 * @return 用户Alias
	 */
	public String getUserAlias(Long id) {
		Assert.notNull(id);

		String key = CacheEnum.USER_ALIAS.getKey(id);
    	String alias = GuavaCacheUtil.get(key);
		if (alias != null) {
			return alias;
		}

		UserVO userVO = getUserInfoById(id);
		if (userVO == null) {
			return null;
		}
		alias = userVO.getAlias();

		GuavaCacheUtil.put(key, alias);
		return alias;
	}

	/**
	 * 根据用户id获取用户信息
	 * @param id
	 * @return
	 */
	public UserVO getUserInfoById(Long id){
		User user = new User();
		user.setId(id);
		user.setIsDeleted(false);
		return this.get(user);
	}

	public UserVO getUserInfoByUid(String uid) {
		User user = new User();
		user.setUid(uid);
		user.setIsDeleted(false);
		return this.get(user);
	}


    public User getOrSaveByUserId(String userId, String name, String department, Integer departmentId, String alias) {
		if (StringUtils.isNotBlank(department)) {
			department = StringUtils.substringBefore(department,"-");
		}

    	User vo = new User();
    	vo.setUid(userId);
    	User user = this.get(vo);
    	if (user == null) {
    		// 保存用户
    		vo = new User();
        	vo.setUid(userId);
    		vo.setName(name);
			vo.setIsAdmin(false);
        	vo.setDepartName(department);
        	vo.setDepartId(departmentId);
        	vo.setAlias(StringUtils.replace(alias.trim(), " ", "").toLowerCase());
        	user = this.save(vo);
    	}  else if (!StringUtils.equals(department, user.getDepartName()) 
    			|| !departmentId.equals(user.getDepartId())) {
    		// 部门变更，改本地信息
    		vo = new User();
        	vo.setUid(userId);
        	Map<String, Object> updateParam = Maps.newHashMap();
        	updateParam.put("depart_name", department);
        	updateParam.put("depart_id", departmentId);
        	vo.setUpdateParam(updateParam);
        	this.edit(vo);
    	}
    	return user;
    }

	/**
	 * 列出所有department信息
	 * @return
	 */
	@Deprecated
    public List<DepartmentVo> listDepartments(){
		List<UserVO> users = this.list(new User());
		if (CollectionUtils.isEmpty(users)){
			return null;
		}
		List<DepartmentVo> departmentVos = users.stream()
				.map(po -> {
					if (po == null){
						return null;
					}
					DepartmentVo departmentVo = new DepartmentVo();
					departmentVo.setDepartId(po.getDepartId());
					departmentVo.setDepartName(po.getDepartName());
					return departmentVo;
				}).distinct()
				.collect(Collectors.toList());
		return departmentVos;
	}
    
	public List<UserVO> listByName(String name) {
		User user = new User();
		user.setIsDeleted(false);
		user.setName(name);
		return this.list(user);
	}
    
}
