package com.clubfactory.platform.scheduler.web.core.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.ScriptMapper;
import com.clubfactory.platform.scheduler.dal.po.Script;
import com.clubfactory.platform.scheduler.web.core.utils.DFSUtils;
import com.clubfactory.platform.scheduler.web.core.utils.FileUtils;
import com.clubfactory.platform.scheduler.web.core.vo.ScriptContentVo;
import com.clubfactory.platform.scheduler.web.core.vo.ScriptVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ScriptService extends BaseNewService<ScriptVO,Script> {

    @Resource
    ScriptMapper scriptMapper;
    @Resource
	UserService userService;

    @PostConstruct
    public void init(){
        setBaseMapper(scriptMapper);
    }
    
    public Map<Long, Integer> getScriptVersionMap() {
    	Script script = new Script();
    	script.setIsDeleted(false);
    	List<ScriptVO> scripts = this.list(script);
    	if (CollectionUtils.isEmpty(scripts)) {
    		return Maps.newHashMap();
    	}
    	return scripts.stream().collect(Collectors.toMap(Script::getId, Script::getVersion));
    }
    
    public Map<String, Long> getScriptNameMap() {
    	Script script = new Script();
    	script.setIsDeleted(false);
    	List<ScriptVO> scripts = this.list(script);
    	if (CollectionUtils.isEmpty(scripts)) {
    		return Maps.newHashMap();
    	}
    	return scripts.stream().collect(Collectors.toMap(Script::getScriptName, Script::getId));
    }
    
    public String getName(Long id) {
    	Assert.notNull(id);
    	
    	Script script = new Script();
    	script.setId(id);
    	script.setIsDeleted(false);
    	ScriptVO scriptVo = this.get(script);
    	if (scriptVo == null) {
    		return null;
    	}
    	return scriptVo.getScriptName();
    }
    
    public Integer getVersion(Long id) {
    	Assert.notNull(id);
    	
    	Script script = new Script();
    	script.setId(id);
    	script.setIsDeleted(false);
    	ScriptVO scriptVo = this.get(script);
    	if (scriptVo == null) {
    		return null;
    	}
    	return scriptVo.getVersion();
    }
    
    public String getFileName(Long id) {
    	Assert.notNull(id);
    	
    	Script script = new Script();
    	script.setId(id);
    	script.setIsDeleted(false);
    	ScriptVO scriptVo = this.get(script);
    	if (scriptVo == null) {
    		return null;
    	}
    	return scriptVo.getFileName();
    }

	/**
	 * 根据用户ID和脚本名称查找脚本记录
	 * @param scriptName
	 * @param createUser
	 * @return
	 */
    public ScriptVO getScriptByName(String scriptName,Long createUser){
    	Assert.notNull(scriptName);
    	Assert.notNull(createUser);
		Script script = new Script();
		script.setScriptName(scriptName);
		script.setCreateUser(createUser);
		script.setIsDeleted(false);

		return  this.get(script);
	}

	/**
	 * 根据脚本名称完全匹配对应的脚本ID
	 * @param scriptName
	 * @return
	 */
	public Long getIdByScriptName(String scriptName){
    	Assert.notNull(scriptName);
    	return scriptMapper.selectIdByScriptName(scriptName);
	}


	/**
	 * @param scriptId
	 * @param version
	 * @param allowViewList
	 * @return
	 */
	public ScriptContentVo getScriptContentById(Long scriptId, Integer version, List<String> allowViewList) {
		Assert.notNull(scriptId);
		Script script = new Script();
		script.setId(scriptId);
		script.setIsDeleted(false);

		ScriptVO scriptVO = get(script);
		if(scriptVO == null){
			throw new BizException("脚本记录不存在或已被删除");
		}
		if (version != null && scriptVO.getVersion() < version) {
			throw new BizException("脚本版本不存在, 当前最新版本为: " + scriptVO.getVersion());
		}
		if (version != null) {
			scriptVO.setVersion(version);
		}

		if (allowViewList == null){
			String allowViews = FileUtils.getResourceViewSuffixs();
			allowViewList = Arrays.asList(allowViews.split(","));
		}
		String fileExt = scriptVO.getFileExt();
		if (StringUtils.isBlank(fileExt) || !allowViewList.contains(fileExt.toLowerCase().trim())){
			throw new BizException(String.format("当前只支持在线查看以下类型的文件: %s",String.join(",",allowViewList)));
		}
		String dfsFileName = getDfsFilePath(scriptVO);
		log.info(" script dfs path is {} ", dfsFileName);
		String content;
		try {
			if(DFSUtils.getInstance().exists(dfsFileName)) {
				List<String> contentList = DFSUtils.getInstance().catFile(dfsFileName, 0, Integer.MAX_VALUE);
				content = StringUtils.join(contentList,"\n");
			}else {
				content = "DFS上脚本文件不存在";
			}
		} catch (IOException e) {
			throw new BizException(e.getMessage());
		}
		ScriptContentVo contentVo = new ScriptContentVo();
		contentVo.setContent(content);
		contentVo.setId(scriptId);
		contentVo.setFileExt(scriptVO.getFileExt());
		return contentVo;
	}


	/**
	 * 获取文件在DFS上的存储路径
	 * @param scriptVO
	 * @return
	 */
	public String getDfsFilePath(ScriptVO scriptVO) {
		String dfsFileBasePath = scriptVO.getScriptBasePath();
		String fileName = String.format("%s_%s",scriptVO.getFileName(),scriptVO.getVersion());
		return DFSUtils.getDfsFilePath(dfsFileBasePath,fileName);
	}


	/**
	 * 根据DB中查出来的信息组装脚本在DFS上存储的父目录
	 * @param scriptVO
	 * @return
	 */
	public String getUserDir(ScriptVO scriptVO){
		// 组装DFS上userDir
		Long createUser = scriptVO.getCreateUser();
		Assert.notNull(createUser,"创建人");
		String username = userService.getUserAlias(createUser);
		username = StringUtils.isBlank(username) ? "unnamed" : username;
		return String.format("%s_%s",username,createUser);
	}

	public List<ScriptVO> listByCreateUser(Long userId) {
		Assert.notNull(userId);
		Script script = new Script();
		script.setIsDeleted(false);
		script.setCreateUser(userId);
		return this.list(script);
	}
	
	public List<ScriptVO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		Script script = new Script();
		script.setIsDeleted(false);
		script.setIds(ids);
		return this.list(script);
	}

	public void editOwnerByIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "id列表");
		
		Script script = new Script();
		script.setIsDeleted(false);
		script.setCreateUser(userId);
		script.setIds(ids);
		Map<String, Object> updateParam = Maps.newHashMap();
		script.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(script);
	}
	

}
