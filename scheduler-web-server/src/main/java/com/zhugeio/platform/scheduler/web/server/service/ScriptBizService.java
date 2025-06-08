package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.dto.ChangeDto;
import com.zhugeio.platform.scheduler.web.server.dto.ScriptZipDto;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.utils.LocalFileUtils;
import com.zhugeio.platform.scheduler.web.server.utils.ZipUtils;
import com.zhugeio.platform.scheduler.web.server.vo.CommonEnumVo;
import com.zhugeio.platform.scheduler.web.server.vo.ScriptVo;
import com.zhugeio.platform.scheduler.web.server.vo.SimpleScriptVo;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.enums.ScriptType;
import com.zhugeio.platform.scheduler.dal.po.Script;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.web.core.service.JobOnlineService;
import com.zhugeio.platform.scheduler.web.core.service.JobService;
import com.zhugeio.platform.scheduler.web.core.service.ScriptService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.utils.DFSUtils;
import com.zhugeio.platform.scheduler.web.core.utils.FileUtils;
import com.zhugeio.platform.scheduler.web.core.utils.PropertyUtils;
import com.zhugeio.platform.scheduler.web.core.vo.ScriptVO;
import com.zhugeio.platform.scheduler.web.server.dto.ScriptPagerDto;
import com.zhugeio.platform.scheduler.web.core.vo.ScriptContentVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
@Slf4j
@DependsOn(value = "commonPropertiesConfig")
public class ScriptBizService {


    @Autowired
    private ScriptService scriptService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobBizService jobBizService;
    @Autowired
    private JobOnlineService jobOnlineService;
    @Autowired
    private UserService userService;

    private List<String> allowViewList;

    private List<String> allowUploadList;

    @PostConstruct
    public void init(){
        if (allowUploadList == null){
            String allowExts = PropertyUtils.getString(Constants.RESOURCE_UPLOAD_SUFFIXES);
            if (allowExts == null){
                allowUploadList = new ArrayList<>();
            }else {
                allowUploadList = Arrays.asList(allowExts.split(","));
            }
        }
    }
    
    /**
     * 新增脚本
     * @param userDto
     * @param file
     * @param scriptName
     * @param scriptDesc
     * @param scriptType
     */
    @Transactional(rollbackFor = Exception.class)
    public SimpleScriptVo addScript(LoginUserDto userDto, MultipartFile file, String scriptName, String scriptDesc,
                                    ScriptType scriptType){
        Assert.notNull(userDto);
        Assert.notNull(scriptType);
        Long createUser = userDto.getLocalUserId();
        String username = StringUtils.isEmpty(userDto.getAlias()) ? "unnamed" : userDto.getAlias();
        String userDir =  String.format("%s_%s",username,createUser);

        if (file.isEmpty()){
            throw new BizException("脚本文件为空");
        }

        if (file.getSize() > Constants.maxScriptSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    file.getSize(),Constants.maxScriptSize));
        }
        if (!userDto.getIsAdmin() && scriptType == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能添加系统脚本");
        }

        // 确保脚本名称唯一性
        ensureScriptNameUnique(scriptName,null);

        String fileExt = FileUtils.suffix(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-","");
        if (StringUtils.isEmpty(fileExt) || !allowUploadList.contains(fileExt.toLowerCase().trim())){
            throw new BizException(String.format("当前只支持上传以下类型的文件: %s",String.join(",",allowUploadList)));
        }

        String dfsResourcePath = DFSUtils.getDfsResDir(userDir);
        // 脚本记录入库
        Script script = new Script();
        script.setCreateUser(createUser);
        script.setUpdateUser(createUser);
        script.setFileName(fileName);
        script.setScriptName(scriptName);
        script.setRemark(scriptDesc);
        script.setScriptType(scriptType);
        script.setVersion(1);
        script.setFileExt(fileExt);
        script.setScriptBasePath(dfsResourcePath);
        scriptService.save(script);
        Long scriptId = script.getId();
        Assert.nonNull(scriptId, "生成脚本ID失败");

        String dfsFileName = String.format("%s_%s",fileName,script.getVersion());
        // 上传文件
        uploadFileToDFS(dfsResourcePath,userDir,dfsFileName,file);
        return SimpleScriptVo.builder().scriptId(scriptId).scriptName(scriptName).scriptVersion(script.getVersion()).build();
    }

    /**
     * 更新脚本
     * @param userDto
     * @param scriptId
     * @param file
     * @param scriptName
     * @param scriptDesc
     * @param scriptType
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateScript(LoginUserDto userDto,
                          Long scriptId,
                          MultipartFile file,
                          String scriptName,
                          String scriptDesc,
                          ScriptType scriptType){
        Assert.notNull(scriptId);
        Assert.notNull(userDto);
        Long updateUser = userDto.getLocalUserId();

        // 文件大小校验
        if (file != null && !file.isEmpty() && file.getSize() > Constants.maxScriptSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    file.getSize(),Constants.maxScriptSize));
        }

        // 查询当前脚本原始信息
        ScriptVO scriptVO = getScriptById(scriptId);
        if (scriptVO == null){
            throw new BizException("脚本不存在");
        }

        if (!userDto.getIsAdmin() && scriptType == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能修改系统脚本");
        }

        // 组装DFS上userDir
        String userDir = scriptService.getUserDir(scriptVO);
        Integer newVersion = scriptVO.getVersion() + 1;

        // 文件扩展名检验
        if (file != null && !file.isEmpty()) {
            String fileExt = FileUtils.suffix(file.getOriginalFilename());
            String originExt = scriptVO.getFileExt();
            if (!StringUtils.equalsIgnoreCase(fileExt, originExt)) {
                throw new BizException(
                        String.format("新文件扩展名必须和之前保持一致，新扩展名为[%s],原扩展名为[%s]", fileExt, originExt));
            }
        }
        // 权限检查
        checkEditPermission(scriptVO,userDto);

        // 脚本记录入库
        Script newScript = new Script();
        newScript.setId(scriptId);
        newScript.setIsDeleted(false);

        Map<String,Object> updateParams = Maps.newHashMap();
        updateParams.put("update_user",updateUser);
        if (StringUtils.isNotBlank(scriptName)){
            ensureScriptNameUnique(scriptName,scriptId);
            updateParams.put("script_name",scriptName);
        }
        if (StringUtils.isNotBlank(scriptDesc)){
            updateParams.put("remark",scriptDesc);
        }
        if(scriptType != null){
            updateParams.put("script_type",scriptType);
            scriptVO.setScriptType(scriptType);
        }
        if (file != null && !file.isEmpty()){
            // 有文件上传时，更新版本号
            log.info("需要更新文件，脚本版本号自增1，当前版本为{}",newVersion);
            updateParams.put("version",newVersion);
        }
        newScript.setUpdateParam(updateParams);

        scriptService.edit(newScript);

        if (file != null && !file.isEmpty()){
            String dfsFileBasePath = scriptVO.getScriptBasePath();
            String dfsFileName = String.format("%s_%s",scriptVO.getFileName(),newVersion);
            // 上传文件
            this.uploadFileToDFS(dfsFileBasePath, userDir,dfsFileName,file);
            //  触发对应任务为待审核状态
            if (scriptVO.getScriptType() == ScriptType.USER_LEVEL) {
                jobService.editRedoingByScriptId(scriptId, userDto.getLocalUserId());
            }

            if (scriptVO.getScriptType() == ScriptType.SYS_LEVEL){
                jobOnlineService.editVersionBySysSciptId(scriptId,newVersion);
            }


        }
    }

    /**
     * 更新脚本内容
     * @param userDto
     * @param scriptId
     * @param content
     */
    @Transactional(rollbackFor = Exception.class)
    public void editScript(LoginUserDto userDto,
                           Long scriptId,
                           String content) {
        Assert.notNull(scriptId);
        Assert.notNull(userDto);
        Long updateUser = userDto.getLocalUserId();

        // 文件大小校验
        if (content != null && content.getBytes().length > Constants.maxScriptSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    content.getBytes().length,Constants.maxScriptSize));
        }

        // 查询当前脚本原始信息
        ScriptVO scriptVO = getScriptById(scriptId);
        if (scriptVO == null){
            throw new BizException("脚本不存在");
        }

        if (!userDto.getIsAdmin() && scriptVO.getScriptType() == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能修改系统脚本");
        }

        String userDir = scriptService.getUserDir(scriptVO);
        Integer newVersion = scriptVO.getVersion() + 1;

        // 权限检查
        checkEditPermission(scriptVO,userDto);

        // 脚本记录入库
        Script newScript = new Script();
        newScript.setId(scriptId);
        newScript.setIsDeleted(false);

        Map<String,Object> updateParams = Maps.newHashMap();
        // 更新版本号
        updateParams.put("version",newVersion);
        updateParams.put("update_user",updateUser);
        newScript.setUpdateParam(updateParams);

        if(content == null){
            content = "";
        }

        scriptService.edit(newScript);

        String dfsFileName = String.format("%s_%s",scriptVO.getFileName(),newVersion);

        // 触发对应任务为待审核状态
        if (scriptVO.getScriptType() == ScriptType.USER_LEVEL) {
            jobService.editRedoingByScriptId(scriptId, userDto.getLocalUserId());
        }

        if (scriptVO.getScriptType() == ScriptType.SYS_LEVEL){
            jobOnlineService.editVersionBySysSciptId(scriptId,newVersion);
        }

        // 上传文件
        uploadContentToDfs(scriptVO.getScriptBasePath(), userDir,dfsFileName,content);

    }


    /**
     * 脚本下载
     * @param scriptId
     * @return
     */
    public Resource downloadScript(Long scriptId, Integer version) throws IOException {
        Assert.notNull(scriptId);
        ScriptVO  scriptVO = getScriptById(scriptId);
        if (scriptVO == null){
            log.error("download file not exist,  script id {}", scriptId);
            throw new BizException("脚本元数据不存在..");
        }
        if (version != null && scriptVO.getVersion() < version) {
            throw new BizException("脚本版本不存在, 当前最新版本为: " + scriptVO.getVersion());
        }
        if (version != null) {
            scriptVO.setVersion(version);
        }

        String dfsFileName = scriptService.getDfsFilePath(scriptVO);
        log.info("script dfs path is {} ", dfsFileName);

        String localFileName = this.copyDfsToLocal(dfsFileName,scriptVO.getScriptName());
        return LocalFileUtils.file2Resource(localFileName);
    }


    /**
     * 从DFS下载文件到本地
     * @param dfsFilePath
     * @param localFileName
     * @return 下载后的本地文件路径
     * @throws IOException
     */
    private String copyDfsToLocal(String dfsFilePath,String localFileName) throws IOException {
        String localFilePath = FileUtils.getDownloadFilename(localFileName);
        DFSUtils.getInstance().copyDfsToLocal(dfsFilePath, localFilePath, false, true);
        return localFilePath;
    }


    /**
     * 脚本级别枚举
     * @param userDto
     * @return
     */
    public Map listScriptTypes(LoginUserDto userDto){
        Assert.notNull(userDto);

        Map<String, List<CommonEnumVo>> response = new HashMap<>(3);
        List<CommonEnumVo> list = Lists.newArrayList();
        list.add(new CommonEnumVo(ScriptType.USER_LEVEL.name(),ScriptType.USER_LEVEL.getDesc()));
        if (userDto.getIsAdmin()){
            list.add(new CommonEnumVo(ScriptType.SYS_LEVEL.name(),ScriptType.SYS_LEVEL.getDesc()));
        }
        response.put("type",list);
        return response;
    }

    /**
     * 脚本软删除
     * @param userDto
     * @param scriptId
     */
    public void deleteScript(LoginUserDto userDto,Long scriptId){
        Assert.notNull(userDto);
        Assert.notNull(scriptId);

        Script script = new Script();
        script.setId(scriptId);
        script.setIsDeleted(false);
        ScriptVO  scriptVO = scriptService.get(script);

        if (scriptVO == null){
            throw new BizException("脚本已被删除,无需重复操作");
        }
        if (!userDto.getIsAdmin() && !userDto.getLocalUserId().equals(scriptVO.getCreateUser())){
            throw new BizException(String.format("用户[%s]无权删除该脚本",userDto.getName()));
        }

        script = new Script();
        script.setId(scriptId);
        Map<String, Object> updateParam = Maps.newHashMap();
        updateParam.put("is_deleted",true);
        script.setUpdateParam(updateParam);
        scriptService.edit(script);
    }

    /**
     * 根据脚本ID获取脚本内容
     * @param scriptId
     * @param version
     * @return
     */
    public ScriptContentVo getScriptContentById(Long scriptId, Integer version) {
        return scriptService.getScriptContentById(scriptId, version, allowViewList);
    }


    /**
     * 分页查询
     * @param pagerDto
     * @return
     */
    public PageUtils<ScriptVo> queryByPage(ScriptPagerDto pagerDto){
        Assert.notNull(pagerDto);

        Script script = new Script();
        script.setIsDeleted(false);
        script.setPageNo(pagerDto.getPageNo());
        script.setPageSize(pagerDto.getPageSize());
        script.setId(pagerDto.getScriptId());
        script.setScriptName(pagerDto.getScriptName());

        if (StringUtils.isNotBlank(pagerDto.getCreateUser()) ) {
            List<Long> userIds;
            if (StringUtils.isEmpty(pagerDto.getDepartName())) {
                userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), null);
            }else {
                userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), pagerDto.getDepartName());
            }
            if (CollectionUtils.isNotEmpty(userIds)) {
                script.setIds(userIds);
                script.setQueryListFieldName("create_user");
            } else {
                return new PageUtils<ScriptVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
        }else {
            if (StringUtils.isNotBlank(pagerDto.getDepartName())){
                List<Long> userIds = userService.listUserIdsByDepartName(pagerDto.getDepartName());
                if (CollectionUtils.isEmpty(userIds)){
                    return new PageUtils<ScriptVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
                }
                script.setIds(userIds);
                script.setQueryListFieldName("create_user");
            }
        }


        PageUtils<Script> pages = scriptService.pageList(script);
        // 返回数据
        if (pages.getSize() == 0) {
            return new PageUtils<ScriptVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }

        if (allowViewList == null) {
            String viewSuffixes = PropertyUtils.getString(Constants.RESOURCE_VIEW_SUFFIXES);
            String[] viewArr = StringUtils.split(viewSuffixes, ",");
            if (viewArr != null) {
                allowViewList = Arrays.asList(viewArr);
            }else {
                allowViewList = new ArrayList<>();
            }
        }

        List<ScriptVo> dataSourceVos = pages.getRows()
                .stream()
                .map(po -> {
                    ScriptVo scriptVo = new ScriptVo();
                    if (po != null) {
                        BeanUtil.copyBeanNotNull2Bean(po, scriptVo);
                    }else {
                        return scriptVo;
                    }
                    boolean isModifiable = false;
                    if (StringUtils.isNotBlank(po.getFileExt())) {
                        isModifiable = allowViewList.contains(po.getFileExt().trim());
                    }
                    scriptVo.setDesc(po.getRemark());
                    if(po.getCreateUser() != null) {
                        scriptVo.setCreateUser(userService.getUserName(po.getCreateUser()));
                        scriptVo.setDepartName(userService.getDepartName(po.getCreateUser()));
                    }
                    if (po.getScriptType() != null){
                        scriptVo.setScriptType(po.getScriptType().name());
                        scriptVo.setScriptTypeDesc(po.getScriptType().getDesc());
                    }
                    String downloadFileName = "unknown_file";
                    if (StringUtils.isNotBlank(po.getScriptName())){
                        downloadFileName = po.getScriptName();
                        if (StringUtils.isNotBlank(po.getFileExt()) &&
                                !StringUtils.endsWithIgnoreCase(downloadFileName,po.getFileExt())){
                            downloadFileName = String.format("%s.%s",downloadFileName,po.getFileExt());
                        }
                    }
                    scriptVo.setDownloadFileName(downloadFileName);
                    scriptVo.setModifiable(isModifiable);
                    return scriptVo;
                })
                .collect(Collectors.toList());
        return new PageUtils<ScriptVo>(dataSourceVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
    }


    /**
     * 上传文件到DFS
     * @param userDir
     * @param dfsResourcePath
     * @param dfsFileName
     * @param file
     */
    private  void uploadFileToDFS(String dfsResourcePath, String userDir,String dfsFileName,MultipartFile file){
        String localFilename = FileUtils.getUploadFilename(userDir, UUID.randomUUID().toString());
        String dfsFilePath = DFSUtils.getDfsFilePath(dfsResourcePath,dfsFileName);
        try {
            //  判断用户资源目录是否存在
            if (!DFSUtils.getInstance().exists(dfsResourcePath)) {
                DFSUtils.getInstance().mkdir(dfsResourcePath);
            }
            LocalFileUtils.copyFile(file, localFilename);
            // 上传文件到DFS并删除本地临时文件
            log.info("开始将文件上传到DFS");
            DFSUtils.getInstance().copyLocalToDfs(localFilename, dfsFilePath, true, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                FileUtils.deleteIfExists(localFilename);
            } catch (IOException ex) {
                log.error("文件清理失败: {}",e.getMessage());
            }
            throw new BizException("文件上传失败:" + e.getMessage());
        }
    }

    /**
     *
     * 将字符串更新到DFS
     * @param dfsFileBasePath
     * @param userDir
     * @param dfsFileName
     * @param content
     * @return
     */
    private void uploadContentToDfs(String dfsFileBasePath, String userDir, String dfsFileName, String content) {

        String localFilename = FileUtils.getUploadFilename(userDir, UUID.randomUUID().toString());
        String dfsFilePath = DFSUtils.getDfsFilePath(dfsFileBasePath, dfsFileName);
        try {
            if (!FileUtils.writeContent2File(content, localFilename)){
                throw new BizException("写入本地临时文件时出错");
            }
            //  判断用户资源目录是否存在
            if (!DFSUtils.getInstance().exists(dfsFileBasePath)) {
                DFSUtils.getInstance().mkdir(dfsFileBasePath);
            }
            // 上传文件到DFS并删除本地临时文件
            DFSUtils.getInstance().copyLocalToDfs(localFilename, dfsFilePath, true, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                FileUtils.deleteIfExists(localFilename);
            } catch (IOException ex) {
                log.error("文件清理失败: {}",e.getMessage());
            }
            throw new BizException("文件上传失败:" + e.getMessage());
        }

    }


    /**
     * 检查更新操作权限
     * @param scriptVO
     * @param userDto
     */
    private void checkEditPermission(ScriptVO scriptVO,LoginUserDto userDto){
        Long createUser = userDto.getLocalUserId();
        if (!createUser.equals(scriptVO.getCreateUser()) && !userDto.getIsAdmin()){
            throw new BizException(String.format("用户[%s]无权更新当前脚本",userDto.getName()));
        }
    }


    /**
     * 根据脚本ID查询脚本信息
     * @param scriptId
     * @return
     */
    private ScriptVO getScriptById(Long scriptId){
        Script script = new Script();
        script.setId(scriptId);
        script.setIsDeleted(false);
        return scriptService.get(script);
    }


    /**
     * 确保脚本名称全局唯一
     * @param scriptName
     * @param excludeId
     */
    private void ensureScriptNameUnique(String scriptName,Long excludeId){
        Long scriptId = scriptService.getIdByScriptName(scriptName);
        if (scriptId != null){
            if (!scriptId.equals(excludeId)) {
                throw new BizException(String.format("当前已存在名称为【%s】的脚本，请保证脚本名称的唯一性", scriptName));
            }
        }
    }


    /**
     * 解析zip包批量添加脚本
     * @param userDto
     * @param zipDto
     * @return
     */
    public boolean parseScriptZip(LoginUserDto userDto, ScriptZipDto zipDto){
        Assert.notNull(zipDto,"脚本压缩包信息");
        Assert.notNull(userDto,"用户信息");
        String localZipFile = String.format("%s.zip",userDto.getAlias());
        String dfsZipFilePath = zipDto.getZipFile();
        String localZipFilePath = null;
        String uncompressDir = null;
        if (!zipDto.isS3Mode()){
            Long scriptId = zipDto.getZipScriptId();
            Assert.notNull(scriptId,"脚本模式下scriptId");
            ScriptVO scriptVO = this.getScriptById(scriptId);
            Assert.notNull(scriptVO,String.format("脚本ID【%s】对应的脚本信息",scriptId));
            dfsZipFilePath = scriptService.getDfsFilePath(scriptVO);
        }
        try {
            localZipFilePath = this.copyDfsToLocal(dfsZipFilePath,localZipFile);
            log.info("download zip package form {} to {} success",dfsZipFilePath,localZipFilePath);
            uncompressDir = FileUtils.genZipUncompressDir();
            ZipUtils.unzip(localZipFilePath,uncompressDir);
            log.info("uncompress zip package {} to {} success",localZipFilePath,uncompressDir);

            File [] fileList = FileUtils.listFiles(uncompressDir);
            if (fileList == null || fileList.length == 0){
                throw new BizException("zip包解压失败，或者一级目录无文件");
            }
            this.processScriptList(userDto,fileList);
            log.info("zip package {} files save success",dfsZipFilePath);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (localZipFilePath != null) {
                    FileUtils.deleteIfExists(localZipFilePath);
                    log.info("local zip package clear success");
                }
            } catch (IOException e) {
                log.error("clear local zip package failed",e);
            }
            try {
                if (uncompressDir != null) {
                    FileUtils.deleteIfExists(uncompressDir);
                    log.info("local zip package uncompress dir clear success");
                }
            } catch (IOException e) {
                log.error("clear local uncompress dir failed",e);
            }
        }
    }
    
	public void changeScriptOwner(ChangeDto changeDto, LoginUserDto userDto) {
		List<Long> scriptIds = changeDto.getScriptIds();
		List<ScriptVO> scriptVos = scriptService.listByIds(scriptIds);
		
		if (BooleanUtils.isTrue(userDto.getIsAdmin())) {
			Map<Long, List<ScriptVO>> scriptMap = scriptVos.stream().collect(Collectors.groupingBy(ScriptVO::getCreateUser));
			for (Entry<Long, List<ScriptVO>> entry : scriptMap.entrySet()) {
				ChangeDto subChangeDto = new ChangeDto();
				subChangeDto.setIsScript(true);
				subChangeDto.setTargetOwnerId(changeDto.getTargetOwnerId());
				subChangeDto.setOriginOwnerId(entry.getKey());
				List<Long> subScriptIds = entry.getValue().stream().map(ScriptVO::getId).collect(Collectors.toList());
				subChangeDto.setScriptIds(subScriptIds);
				jobBizService.changeOwner(subChangeDto, userDto);
			}
		} else {
			for (ScriptVO script : scriptVos) {
				if (!script.getCreateUser().equals(userDto.getLocalUserId())) {
					throw new BizException(ErrorCode.SCRIPT_NOT_PERMISSION.setParams(script.getScriptName()));
				}
			}
			changeDto.setOriginOwnerId(userDto.getLocalUserId());
			changeDto.setIsScript(true);
			jobBizService.changeOwner(changeDto, userDto);
		}
	}


    /**
     * 批量处理文件
     * @param userDto
     * @param fileList
     */
    private void processScriptList(LoginUserDto userDto, File[] fileList){
        List<ScriptVO> updateScripts = Lists.newArrayList();
        List<File> newScripts  = Lists.newArrayList();
        List<String> conflictScripts = Lists.newArrayList();
        List<String> scriptNames = Lists.newArrayList();
        Long currentUid = userDto.getLocalUserId();
        Map<String,File> fileMap = Maps.newHashMap();
        List<String>  updateScriptNames = Lists.newArrayList();

        // 脚本类型校验
        for (File f : fileList){
            String scriptName = f.getName().trim();
            String fileExt = FileUtils.suffix(scriptName);
            if (StringUtils.isBlank(fileExt) || !allowUploadList.contains(fileExt.toLowerCase().trim())){
                throw new BizException(String.format("当前只支持以下类型的脚本: %s",String.join(",",allowUploadList)));
            }
            if (scriptNames.contains(scriptName)){
                throw new BizException("zip包内有重复文件,不允许通过在文件名首尾加空格来区分文件");
            }
            scriptNames.add(scriptName);
            fileMap.put(scriptName,f);
        }
        Script script = new Script();
        script.setIsDeleted(false);
        script.setIdsString(scriptNames);
        script.setQueryListFieldName("script_name");
        List<ScriptVO> scriptVOS = scriptService.list(script);

        scriptVOS.forEach(vo -> {
            if (!currentUid.equals(vo.getCreateUser())){
                conflictScripts.add(vo.getScriptName());
            }else {
                updateScripts.add(vo);
                updateScriptNames.add(vo.getScriptName());
            }
        });

        if (CollectionUtils.isNotEmpty(conflictScripts)){
            throw new BizException(String.format("Zip包中文件名【%s】在已经有其他用户使用,请更改后重新上传",
                    String.join(",",conflictScripts)));
        }

        for (File f : fileList){
            String scriptName = f.getName().trim();
            if (!updateScriptNames.contains(scriptName)){
                newScripts.add(f);
            }
        }

        this.persistScripts(newScripts,userDto);
        log.info("persist {} scripts success",newScripts.size());
        this.updateScripts(fileMap,updateScripts,userDto);
        log.info("update {} scripts success",updateScripts.size());

    }

    /**
     * 持久化脚本
     * @param files
     * @param userDto
     */
    private void persistScripts(List<File> files,LoginUserDto userDto){
        List<Script> scripts = Lists.newArrayList();
        files.forEach(f -> {
            String scriptName = f.getName().trim();
            String localFilePath = f.getAbsolutePath();
            String fileExt = FileUtils.suffix(scriptName);
            String fileName = System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-","");
            Long userId = userDto.getLocalUserId();
            String username = StringUtils.isEmpty(userDto.getAlias()) ? "unnamed_user" : userDto.getAlias();
            String userDir =  String.format("%s_%s",username,userId);
            Integer scriptVersion = 1;

            String dfsFileBasePath = DFSUtils.getDfsResDir(userDir);
            String dfsFileName = String.format("%s_%s",fileName,scriptVersion);
            String dfsFilePath = DFSUtils.getDfsFilePath(dfsFileBasePath, dfsFileName);
            try {
                if (!DFSUtils.getInstance().exists(dfsFileBasePath)) {
                    DFSUtils.getInstance().mkdir(dfsFileBasePath);
                }
                log.info("开始上传文件{}到DFS路径:{}",scriptName,dfsFilePath);
                DFSUtils.getInstance().copyLocalToDfs(localFilePath, dfsFilePath, true, true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BizException("文件上传失败:" + e.getMessage());
            }

            // 脚本记录入库
            Script script = new Script();
            script.setCreateUser(userId);
            script.setUpdateUser(userId);
            script.setFileName(fileName);
            script.setScriptName(scriptName);
            script.setRemark("批量添加");
            script.setScriptType(ScriptType.USER_LEVEL);
            script.setVersion(scriptVersion);
            script.setScriptBasePath(dfsFileBasePath);
            script.setFileExt(fileExt);
            scripts.add(script);
        });
        scriptService.saveBatch(scripts);
    }

    /**
     * 更新脚本
     * @param fileMap
     * @param updateScripts
     * @param userDto
     */
    private void updateScripts(Map<String,File> fileMap,List<ScriptVO> updateScripts,LoginUserDto userDto){
        updateScripts.forEach(vo -> {
            String scriptName = vo.getScriptName();
            File f = fileMap.get(scriptName);
            if (f == null){
                throw new BizException(String.format("获取文件%s失败",scriptName));
            }

            Long scriptId = vo.getId();
            Integer newVersion = vo.getVersion() + 1;
            String dfsFileName = String.format("%s_%s",vo.getFileName(),newVersion);
            String localFilePath = f.getAbsolutePath();

            String dfsFileBasePath = vo.getScriptBasePath();
            String dfsFilePath = DFSUtils.getDfsFilePath(dfsFileBasePath, dfsFileName);

            try {
                if (!DFSUtils.getInstance().exists(dfsFileBasePath)) {
                    DFSUtils.getInstance().mkdir(dfsFileBasePath);
                }
                log.info("开始上传文件{}到DFS路径:{}",scriptName,dfsFilePath);
                DFSUtils.getInstance().copyLocalToDfs(localFilePath, dfsFilePath, true, true);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BizException("文件上传失败:" + e.getMessage());
            }

            // 脚本记录入库
            Script newScript = new Script();
            newScript.setId(scriptId);
            newScript.setIsDeleted(false);
            Map<String,Object> updateParams = Maps.newHashMap();
            updateParams.put("update_user",userDto.getLocalUserId());
            updateParams.put("version",newVersion);
            newScript.setUpdateParam(updateParams);
            scriptService.edit(newScript);
            jobService.editRedoingByScriptId(scriptId, userDto.getLocalUserId());
        });

    }



}
