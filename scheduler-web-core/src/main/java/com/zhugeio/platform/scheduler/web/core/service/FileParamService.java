package com.zhugeio.platform.scheduler.web.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.FileParamMapper;
import com.zhugeio.platform.scheduler.dal.dao.FileParamMapper;
import com.zhugeio.platform.scheduler.dal.po.FileParam;
import com.zhugeio.platform.scheduler.dal.po.FileParam;
import com.zhugeio.platform.scheduler.web.core.utils.DFSUtils;
import com.zhugeio.platform.scheduler.web.core.utils.FileUtils;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamVO;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamContentVo;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileParamService extends BaseNewService<FileParamVO, FileParam> {

    @Resource
	FileParamMapper fileParamMapper;
    @Resource
	UserService userService;

    @PostConstruct
    public void init(){
        setBaseMapper(fileParamMapper);
    }
    
    public Map<Long, Integer> getFileParamVersionMap() {
		FileParam fileParam = new FileParam();
		fileParam.setIsDeleted(false);
    	List<FileParamVO> fileParams = this.list(fileParam);
    	if (CollectionUtils.isEmpty(fileParams)) {
    		return Maps.newHashMap();
    	}
    	return fileParams.stream().collect(Collectors.toMap(FileParam::getId, FileParam::getVersion));
    }
    
    public Map<String, Long> getFileParamNameMap() {
		FileParam fileParam = new FileParam();
		fileParam.setIsDeleted(false);
    	List<FileParamVO> fileParams = this.list(fileParam);
    	if (CollectionUtils.isEmpty(fileParams)) {
    		return Maps.newHashMap();
    	}
    	return fileParams.stream().collect(Collectors.toMap(FileParam::getFileParamName, FileParam::getId));
    }
    
    public String getName(Long id) {
    	Assert.notNull(id);
    	
    	FileParam fileParam = new FileParam();
    	fileParam.setId(id);
    	fileParam.setIsDeleted(false);
    	FileParamVO fileParamVo = this.get(fileParam);
    	if (fileParamVo == null) {
    		return null;
    	}
    	return fileParamVo.getFileParamName();
    }
    
    public Integer getVersion(Long id) {
    	Assert.notNull(id);
    	
    	FileParam fileParam = new FileParam();
    	fileParam.setId(id);
    	fileParam.setIsDeleted(false);
    	FileParamVO fileParamVo = this.get(fileParam);
    	if (fileParamVo == null) {
    		return null;
    	}
    	return fileParamVo.getVersion();
    }
    
    public String getFileName(Long id) {
    	Assert.notNull(id);
    	
    	FileParam fileParam = new FileParam();
    	fileParam.setId(id);
    	fileParam.setIsDeleted(false);
    	FileParamVO fileParamVo = this.get(fileParam);
    	if (fileParamVo == null) {
    		return null;
    	}
    	return fileParamVo.getFileName();
    }

	/**
	 * 根据用户ID和脚本名称查找脚本记录
	 * @param fileParamName
	 * @param createUser
	 * @return
	 */
    public FileParamVO getFileParamByName(String fileParamName,Long createUser){
    	Assert.notNull(fileParamName);
    	Assert.notNull(createUser);
		FileParam fileParam = new FileParam();
		fileParam.setFileParamName(fileParamName);
		fileParam.setCreateUser(createUser);
		fileParam.setIsDeleted(false);

		return  this.get(fileParam);
	}

	/**
	 * 根据脚本名称完全匹配对应的脚本ID
	 * @param fileParamName
	 * @return
	 */
	public Long getIdByFileParamName(String fileParamName){
    	Assert.notNull(fileParamName);
    	return fileParamMapper.selectIdByFileParamName(fileParamName);
	}


	/**
	 * @param fileParamId
	 * @param version
	 * @param allowViewList
	 * @return
	 */
	public FileParamContentVo getFileParamContentById(Long fileParamId, Integer version, List<String> allowViewList) {
		Assert.notNull(fileParamId);
		FileParam fileParam = new FileParam();
		fileParam.setId(fileParamId);
		fileParam.setIsDeleted(false);

		FileParamVO fileParamVO = get(fileParam);
		if(fileParamVO == null){
			throw new BizException("脚本记录不存在或已被删除");
		}
		if (version != null && fileParamVO.getVersion() < version) {
			throw new BizException("脚本版本不存在, 当前最新版本为: " + fileParamVO.getVersion());
		}
		if (version != null) {
			fileParamVO.setVersion(version);
		}

		if (allowViewList == null){
			String allowViews = FileUtils.getResourceViewSuffixs();
			allowViewList = Arrays.asList(allowViews.split(","));
		}
		String fileExt = fileParamVO.getFileExt();
		if (StringUtils.isBlank(fileExt) || !allowViewList.contains(fileExt.toLowerCase().trim())){
			throw new BizException(String.format("当前只支持在线查看以下类型的文件: %s",String.join(",",allowViewList)));
		}
		String dfsFileName = getDfsFilePath(fileParamVO);
		log.info(" fileParam dfs path is {} ", dfsFileName);
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
		FileParamContentVo contentVo = new FileParamContentVo();
		contentVo.setContent(content);
		contentVo.setId(fileParamId);
		contentVo.setFileExt(fileParamVO.getFileExt());
		return contentVo;
	}


	/**
	 * 获取文件在DFS上的存储路径
	 * @param fileParamVO
	 * @return
	 */
	public String getDfsFilePath(FileParamVO fileParamVO) {
		String dfsFileBasePath = fileParamVO.getFileParamBasePath();
		String fileName = String.format("%s_%s",fileParamVO.getFileName(),fileParamVO.getVersion());
		return DFSUtils.getDfsFilePath(dfsFileBasePath,fileName);
	}


	/**
	 * 根据DB中查出来的信息组装脚本在DFS上存储的父目录
	 * @param fileParamVO
	 * @return
	 */
	public String getUserDir(FileParamVO fileParamVO){
		// 组装DFS上userDir
		Long createUser = fileParamVO.getCreateUser();
		Assert.notNull(createUser,"创建人");
		String username = userService.getUserAlias(createUser);
		username = StringUtils.isBlank(username) ? "unnamed" : username;
		return String.format("%s_%s",username,createUser);
	}

	public List<FileParamVO> listByCreateUser(Long userId) {
		Assert.notNull(userId);
		FileParam fileParam = new FileParam();
		fileParam.setIsDeleted(false);
		fileParam.setCreateUser(userId);
		return this.list(fileParam);
	}
	
	public List<FileParamVO> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		FileParam fileParam = new FileParam();
		fileParam.setIsDeleted(false);
		fileParam.setIds(ids);
		return this.list(fileParam);
	}

	public void editOwnerByIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "id列表");
		
		FileParam fileParam = new FileParam();
		fileParam.setIsDeleted(false);
		fileParam.setCreateUser(userId);
		fileParam.setIds(ids);
		Map<String, Object> updateParam = Maps.newHashMap();
		fileParam.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(fileParam);
	}
	

}
