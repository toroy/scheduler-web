package com.zhugeio.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.dao.JobMapper;
import com.zhugeio.platform.scheduler.dal.dao.JobOnlineMapper;
import com.zhugeio.platform.scheduler.dal.enums.JobStatusEnum;
import com.zhugeio.platform.scheduler.dal.enums.ScriptType;
import com.zhugeio.platform.scheduler.dal.po.BasePO;
import com.zhugeio.platform.scheduler.dal.po.FileParam;
import com.zhugeio.platform.scheduler.dal.po.Job;
import com.zhugeio.platform.scheduler.dal.po.JobOnline;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.core.dto.JobDto;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.web.core.service.FileParamService;
import com.zhugeio.platform.scheduler.web.core.service.JobOnlineService;
import com.zhugeio.platform.scheduler.web.core.service.JobService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.utils.DFSUtils;
import com.zhugeio.platform.scheduler.web.core.utils.FileUtils;
import com.zhugeio.platform.scheduler.web.core.utils.PropertyUtils;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamContentVo;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamVO;
import com.zhugeio.platform.scheduler.web.server.dto.ChangeDto;
import com.zhugeio.platform.scheduler.web.server.dto.FileParamPagerDto;
import com.zhugeio.platform.scheduler.web.server.dto.FileParamZipDto;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.utils.LocalFileUtils;
import com.zhugeio.platform.scheduler.web.server.utils.ZipUtils;
import com.zhugeio.platform.scheduler.web.server.vo.CommonEnumVo;
import com.zhugeio.platform.scheduler.web.server.vo.FileParamVo;
import com.zhugeio.platform.scheduler.web.server.vo.SimpleFileParamVo;
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
public class FileParamBizService {


    @Autowired
    private FileParamService fileParamService;
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
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private JobOnlineMapper jobOnlineMapper;

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
     * @param fileParamName
     * @param fileParamDesc
     * @param fileParamType
     */
    @Transactional(rollbackFor = Exception.class)
    public SimpleFileParamVo addFileParam(LoginUserDto userDto, MultipartFile file, String fileParamName, String fileParamDesc,
                                    ScriptType fileParamType){
        Assert.notNull(userDto);
        Assert.notNull(fileParamType);
        Long createUser = userDto.getLocalUserId();
        String username = StringUtils.isEmpty(userDto.getAlias()) ? "unnamed" : userDto.getAlias();
        String userDir =  String.format("%s_%s",username,createUser);

        if (file.isEmpty()){
            throw new BizException("脚本文件为空");
        }

        if (file.getSize() > Constants.maxFileParamSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    file.getSize(),Constants.maxFileParamSize));
        }
        if (!userDto.getIsAdmin() && fileParamType == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能添加系统脚本");
        }

        // 确保脚本名称唯一性
        ensureFileParamNameUnique(fileParamName,null);

        String fileExt = FileUtils.suffix(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-","");
        if (StringUtils.isEmpty(fileExt) || !allowUploadList.contains(fileExt.toLowerCase().trim())){
            throw new BizException(String.format("当前只支持上传以下类型的文件: %s",String.join(",",allowUploadList)));
        }

        String dfsResourcePath = DFSUtils.getDfsParamTenantResDir(userDir);
        // 脚本记录入库
        FileParam fileParam = new FileParam();
        fileParam.setCreateUser(createUser);
        fileParam.setUpdateUser(createUser);
        fileParam.setFileName(fileName);
        fileParam.setFileParamName(fileParamName);
        fileParam.setRemark(fileParamDesc);
        fileParam.setFileParamType(fileParamType);
        fileParam.setVersion(1);
        fileParam.setFileExt(fileExt);
        fileParam.setFileParamBasePath(dfsResourcePath);
        fileParamService.save(fileParam);
        Long fileParamId = fileParam.getId();
        Assert.nonNull(fileParamId, "生成脚本ID失败");

        String dfsFileName = String.format("%s_%s",fileName,fileParam.getVersion());
        // 上传文件
        uploadFileToDFS(dfsResourcePath,userDir,dfsFileName,file);
        return SimpleFileParamVo.builder().fileParamId(fileParamId).fileParamName(fileParamName).fileParamVersion(fileParam.getVersion()).build();
    }

    /**
     * 更新脚本
     * @param userDto
     * @param fileParamId
     * @param file
     * @param fileParamName
     * @param fileParamDesc
     * @param fileParamType
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileParam(LoginUserDto userDto,
                          Long fileParamId,
                          MultipartFile file,
                          String fileParamName,
                          String fileParamDesc,
                          ScriptType fileParamType){
        Assert.notNull(fileParamId);
        Assert.notNull(userDto);
        Long updateUser = userDto.getLocalUserId();

        // 文件大小校验
        if (file != null && !file.isEmpty() && file.getSize() > Constants.maxFileParamSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    file.getSize(),Constants.maxFileParamSize));
        }

        // 查询当前脚本原始信息
        FileParamVO fileParamVO = getFileParamById(fileParamId);
        if (fileParamVO == null){
            throw new BizException("脚本不存在");
        }

        if (!userDto.getIsAdmin() && fileParamType == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能修改系统脚本");
        }

        // 组装DFS上userDir
        String userDir = fileParamService.getUserDir(fileParamVO);
        Integer newVersion = fileParamVO.getVersion() + 1;

        // 文件扩展名检验
        if (file != null && !file.isEmpty()) {
            String fileExt = FileUtils.suffix(file.getOriginalFilename());
            String originExt = fileParamVO.getFileExt();
            if (!StringUtils.equalsIgnoreCase(fileExt, originExt)) {
                throw new BizException(
                        String.format("新文件扩展名必须和之前保持一致，新扩展名为[%s],原扩展名为[%s]", fileExt, originExt));
            }
        }
        // 权限检查
        checkEditPermission(fileParamVO,userDto);

        // 脚本记录入库
        FileParam newFileParam = new FileParam();
        newFileParam.setId(fileParamId);
        newFileParam.setIsDeleted(false);

        Map<String,Object> updateParams = Maps.newHashMap();
        updateParams.put("update_user",updateUser);
        if (StringUtils.isNotBlank(fileParamName)){
            ensureFileParamNameUnique(fileParamName,fileParamId);
            updateParams.put("file_param_name",fileParamName);
        }
        if (StringUtils.isNotBlank(fileParamDesc)){
            updateParams.put("remark",fileParamDesc);
        }
        if(fileParamType != null){
            updateParams.put("file_param_type",fileParamType);
            fileParamVO.setFileParamType(fileParamType);
        }
        if (file != null && !file.isEmpty()){
            // 有文件上传时，更新版本号
            log.info("需要更新文件，脚本版本号自增1，当前版本为{}",newVersion);
            updateParams.put("version",newVersion);
        }
        newFileParam.setUpdateParam(updateParams);

        fileParamService.edit(newFileParam);

        if (file != null && !file.isEmpty()){
            String dfsFileBasePath = fileParamVO.getFileParamBasePath();
            String dfsFileName = String.format("%s_%s",fileParamVO.getFileName(),newVersion);
            // 上传文件
            this.uploadFileToDFS(dfsFileBasePath, userDir,dfsFileName,file);
            //  触发对应任务为待审核状态
            if (fileParamVO.getFileParamType() == ScriptType.USER_LEVEL) {
                //editJobStatus(userDto, fileParamId);
                editJobFileParamVersion(userDto, fileParamId, newVersion);
            }

            if (fileParamVO.getFileParamType() == ScriptType.SYS_LEVEL) {
                editJobFileParamVersion(null, fileParamId, newVersion);
                editJobOnlineFileParamVersion(fileParamId, newVersion);
            }

        }
    }

    /**
     * 更新脚本内容
     * @param userDto
     * @param fileParamId
     * @param content
     */
    @Transactional(rollbackFor = Exception.class)
    public void editFileParam(LoginUserDto userDto,
                           Long fileParamId,
                           String content) {
        Assert.notNull(fileParamId);
        Assert.notNull(userDto);
        Long updateUser = userDto.getLocalUserId();

        // 文件大小校验
        if (content != null && content.getBytes().length > Constants.maxFileParamSize){
            throw new BizException(String.format("脚本大小(%s bytes)超过系统允许的最大值(%s bytes)",
                    content.getBytes().length,Constants.maxFileParamSize));
        }

        // 查询当前脚本原始信息
        FileParamVO fileParamVO = getFileParamById(fileParamId);
        if (fileParamVO == null){
            throw new BizException("脚本不存在");
        }

        if (!userDto.getIsAdmin() && fileParamVO.getFileParamType() == ScriptType.SYS_LEVEL){
            throw new BizException("非管理员不能修改系统脚本");
        }

        String userDir = fileParamService.getUserDir(fileParamVO);
        Integer newVersion = fileParamVO.getVersion() + 1;

        // 权限检查
        checkEditPermission(fileParamVO,userDto);

        // 脚本记录入库
        FileParam newFileParam = new FileParam();
        newFileParam.setId(fileParamId);
        newFileParam.setIsDeleted(false);

        Map<String,Object> updateParams = Maps.newHashMap();
        // 更新版本号
        updateParams.put("version",newVersion);
        updateParams.put("update_user",updateUser);
        newFileParam.setUpdateParam(updateParams);

        if(content == null){
            content = "";
        }

        fileParamService.edit(newFileParam);

        String dfsFileName = String.format("%s_%s",fileParamVO.getFileName(),newVersion);

        // 触发对应任务为待审核状态
        if (fileParamVO.getFileParamType() == ScriptType.USER_LEVEL) {
            //editJobStatus(userDto, fileParamId);
            editJobFileParamVersion(userDto, fileParamId, newVersion);
        }

        if (fileParamVO.getFileParamType() == ScriptType.SYS_LEVEL) {
            editJobOnlineFileParamVersion(fileParamId, newVersion);
        }

        // 上传文件
        uploadContentToDfs(fileParamVO.getFileParamBasePath(), userDir,dfsFileName,content);

    }

    private void editJobStatus(LoginUserDto userDto, Long fileParamId) {
        List<Job> jobs = jobMapper.listHasParamFiles();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        List<Long> jobIds = jobs.stream().filter(job -> {
            List<JobDto.FileParamsContent> fileParams = JSON.parseArray(job.getFileParamsJson(), JobDto.FileParamsContent.class);
            List<Long> valueIds = fileParams.stream().map(JobDto.FileParamsContent::getValue).collect(Collectors.toList());
            return valueIds.contains(fileParamId);
        }).map(BasePO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        jobService.editRedoingByIds(jobIds, userDto.getLocalUserId());
    }

    private void editJobFileParamVersion(LoginUserDto userDto, Long fileParamId, Integer newVersion) {
        List<Job> jobs = jobMapper.listHasParamFiles();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        for (Job job : jobs) {
            List<JobDto.FileParamsContent> fileParams = JSON.parseArray(job.getFileParamsJson(), JobDto.FileParamsContent.class);
            boolean isExist = false;
            for (JobDto.FileParamsContent fileParam : fileParams) {
                if (fileParam.getValue().equals(fileParamId)) {
                    fileParam.setVersion(newVersion);
                    isExist = true;
                }
            }
            if (!isExist) {
                continue;
            }
            Job jobDo = new Job();
            jobDo.setId(job.getId());
            Map<String, Object> updateParam = Maps.newHashMap();
            updateParam.put("file_params_json",JSON.toJSONString(fileParams));
            if (userDto != null) {
                updateParam.put("status", JobStatusEnum.DOING);
                updateParam.put("update_user", userDto.getLocalUserId());
            }
            jobDo.setUpdateParam(updateParam);
            jobService.edit(jobDo);
        }
    }

    private void editJobOnlineFileParamVersion(Long fileParamId, Integer newVersion) {
        List<JobOnline> jobs = jobOnlineMapper.listHasParamFiles();
        if (CollectionUtils.isEmpty(jobs)) {
           return;
        }
        for (JobOnline job : jobs) {
            List<JobDto.FileParamsContent> fileParams = JSON.parseArray(job.getFileParamsJson(), JobDto.FileParamsContent.class);
            boolean isExist = false;
            for (JobDto.FileParamsContent fileParam : fileParams) {
                if (fileParam.getValue().equals(fileParamId)) {
                    fileParam.setVersion(newVersion);
                    isExist = true;
                }
            }
            if (!isExist) {
                continue;
            }
            JobOnline jobOnline = new JobOnline();
            jobOnline.setId(job.getId());
            Map<String, Object> updateJobParams = Maps.newHashMap();
            updateJobParams.put("file_params_json",JSON.toJSONString(fileParams));
            jobOnline.setUpdateParam(updateJobParams);
            jobOnlineService.edit(jobOnline);
        }
    }


    /**
     * 脚本下载
     * @param fileParamId
     * @return
     */
    public Resource downloadFileParam(Long fileParamId, Integer version) throws IOException {
        Assert.notNull(fileParamId);
        FileParamVO  fileParamVO = getFileParamById(fileParamId);
        if (fileParamVO == null){
            log.error("download file not exist,  fileParam id {}", fileParamId);
            throw new BizException("脚本元数据不存在..");
        }
        if (version != null && fileParamVO.getVersion() < version) {
            throw new BizException("脚本版本不存在, 当前最新版本为: " + fileParamVO.getVersion());
        }
        if (version != null) {
            fileParamVO.setVersion(version);
        }

        String dfsFileName = fileParamService.getDfsFilePath(fileParamVO);
        log.info("fileParam dfs path is {} ", dfsFileName);

        String localFileName = this.copyDfsToLocal(dfsFileName,fileParamVO.getFileParamName());
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
    public Map listFileParamTypes(LoginUserDto userDto){
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
     * @param fileParamId
     */
    public void deleteFileParam(LoginUserDto userDto,Long fileParamId){
        Assert.notNull(userDto);
        Assert.notNull(fileParamId);

        FileParam fileParam = new FileParam();
        fileParam.setId(fileParamId);
        fileParam.setIsDeleted(false);
        FileParamVO  fileParamVO = fileParamService.get(fileParam);

        if (fileParamVO == null){
            throw new BizException("脚本已被删除,无需重复操作");
        }
        if (!userDto.getIsAdmin() && !userDto.getLocalUserId().equals(fileParamVO.getCreateUser())){
            throw new BizException(String.format("用户[%s]无权删除该脚本",userDto.getName()));
        }

        fileParam = new FileParam();
        fileParam.setId(fileParamId);
        Map<String, Object> updateParam = Maps.newHashMap();
        updateParam.put("is_deleted",true);
        fileParam.setUpdateParam(updateParam);
        fileParamService.edit(fileParam);
    }

    /**
     * 根据脚本ID获取脚本内容
     * @param fileParamId
     * @param version
     * @return
     */
    public FileParamContentVo getFileParamContentById(Long fileParamId, Integer version) {
        return fileParamService.getFileParamContentById(fileParamId, version, allowViewList);
    }


    /**
     * 分页查询
     * @param pagerDto
     * @return
     */
    public PageUtils<FileParamVo> queryByPage(FileParamPagerDto pagerDto){
        Assert.notNull(pagerDto);

        FileParam fileParam = new FileParam();
        fileParam.setIsDeleted(false);
        fileParam.setPageNo(pagerDto.getPageNo());
        fileParam.setPageSize(pagerDto.getPageSize());
        fileParam.setId(pagerDto.getFileParamId());
        fileParam.setFileParamName(pagerDto.getFileParamName());

        if (StringUtils.isNotBlank(pagerDto.getCreateUser()) ) {
            List<Long> userIds;
            if (StringUtils.isEmpty(pagerDto.getDepartName())) {
                userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), null);
            }else {
                userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), pagerDto.getDepartName());
            }
            if (CollectionUtils.isNotEmpty(userIds)) {
                fileParam.setIds(userIds);
                fileParam.setQueryListFieldName("create_user");
            } else {
                return new PageUtils<FileParamVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
        }else {
            if (StringUtils.isNotBlank(pagerDto.getDepartName())){
                List<Long> userIds = userService.listUserIdsByDepartName(pagerDto.getDepartName());
                if (CollectionUtils.isEmpty(userIds)){
                    return new PageUtils<FileParamVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
                }
                fileParam.setIds(userIds);
                fileParam.setQueryListFieldName("create_user");
            }
        }


        PageUtils<FileParam> pages = fileParamService.pageList(fileParam);
        // 返回数据
        if (pages.getSize() == 0) {
            return new PageUtils<FileParamVo>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
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

        List<FileParamVo> dataSourceVos = pages.getRows()
                .stream()
                .map(po -> {
                    FileParamVo fileParamVo = new FileParamVo();
                    if (po != null) {
                        BeanUtil.copyBeanNotNull2Bean(po, fileParamVo);
                    }else {
                        return fileParamVo;
                    }
                    boolean isModifiable = false;
                    if (StringUtils.isNotBlank(po.getFileExt())) {
                        isModifiable = allowViewList.contains(po.getFileExt().trim());
                    }
                    fileParamVo.setDesc(po.getRemark());
                    if(po.getCreateUser() != null) {
                        fileParamVo.setCreateUser(userService.getUserName(po.getCreateUser()));
                        fileParamVo.setDepartName(userService.getDepartName(po.getCreateUser()));
                    }
                    if (po.getFileParamType() != null){
                        fileParamVo.setFileParamType(po.getFileParamType().name());
                        fileParamVo.setFileParamTypeDesc(po.getFileParamType().getDesc());
                    }
                    String downloadFileName = "unknown_file";
                    if (StringUtils.isNotBlank(po.getFileParamName())){
                        downloadFileName = po.getFileParamName();
                        if (StringUtils.isNotBlank(po.getFileExt()) &&
                                !StringUtils.endsWithIgnoreCase(downloadFileName,po.getFileExt())){
                            downloadFileName = String.format("%s.%s",downloadFileName,po.getFileExt());
                        }
                    }
                    fileParamVo.setDownloadFileName(downloadFileName);
                    fileParamVo.setModifiable(isModifiable);
                    return fileParamVo;
                })
                .collect(Collectors.toList());
        return new PageUtils<FileParamVo>(dataSourceVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
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
     * @param fileParamVO
     * @param userDto
     */
    private void checkEditPermission(FileParamVO fileParamVO,LoginUserDto userDto){
        Long createUser = userDto.getLocalUserId();
        if (!createUser.equals(fileParamVO.getCreateUser()) && !userDto.getIsAdmin()){
            throw new BizException(String.format("用户[%s]无权更新当前脚本",userDto.getName()));
        }
    }


    /**
     * 根据脚本ID查询脚本信息
     * @param fileParamId
     * @return
     */
    private FileParamVO getFileParamById(Long fileParamId){
        FileParam fileParam = new FileParam();
        fileParam.setId(fileParamId);
        fileParam.setIsDeleted(false);
        return fileParamService.get(fileParam);
    }


    /**
     * 确保脚本名称全局唯一
     * @param fileParamName
     * @param excludeId
     */
    private void ensureFileParamNameUnique(String fileParamName,Long excludeId){
        Long fileParamId = fileParamService.getIdByFileParamName(fileParamName);
        if (fileParamId != null){
            if (!fileParamId.equals(excludeId)) {
                throw new BizException(String.format("当前已存在名称为【%s】的脚本，请保证脚本名称的唯一性", fileParamName));
            }
        }
    }


    
	public void changeFileParamOwner(ChangeDto changeDto, LoginUserDto userDto) {
		List<Long> fileParamIds = changeDto.getFileParamIds();
		List<FileParamVO> fileParamVos = fileParamService.listByIds(fileParamIds);
		
		if (BooleanUtils.isTrue(userDto.getIsAdmin())) {
			Map<Long, List<FileParamVO>> fileParamMap = fileParamVos.stream().collect(Collectors.groupingBy(FileParamVO::getCreateUser));
			for (Entry<Long, List<FileParamVO>> entry : fileParamMap.entrySet()) {
				ChangeDto subChangeDto = new ChangeDto();
				subChangeDto.setIsFileParam(true);
				subChangeDto.setTargetOwnerId(changeDto.getTargetOwnerId());
				subChangeDto.setOriginOwnerId(entry.getKey());
				List<Long> subFileParamIds = entry.getValue().stream().map(FileParamVO::getId).collect(Collectors.toList());
				subChangeDto.setFileParamIds(subFileParamIds);
				jobBizService.changeOwner(subChangeDto, userDto);
			}
		} else {
			for (FileParamVO fileParam : fileParamVos) {
				if (!fileParam.getCreateUser().equals(userDto.getLocalUserId())) {
					throw new BizException(ErrorCode.SCRIPT_NOT_PERMISSION.setParams(fileParam.getFileParamName()));
				}
			}
			changeDto.setOriginOwnerId(userDto.getLocalUserId());
			changeDto.setIsFileParam(true);
			jobBizService.changeOwner(changeDto, userDto);
		}
	}


    /**
     * 持久化脚本
     * @param files
     * @param userDto
     */
    private void persistFileParams(List<File> files,LoginUserDto userDto){
        List<FileParam> fileParams = Lists.newArrayList();
        files.forEach(f -> {
            String fileParamName = f.getName().trim();
            String localFilePath = f.getAbsolutePath();
            String fileExt = FileUtils.suffix(fileParamName);
            String fileName = System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-","");
            Long userId = userDto.getLocalUserId();
            String username = StringUtils.isEmpty(userDto.getAlias()) ? "unnamed_user" : userDto.getAlias();
            String userDir =  String.format("%s_%s",username,userId);
            Integer fileParamVersion = 1;

            String dfsFileBasePath = DFSUtils.getDfsParamTenantResDir(userDir);
            String dfsFileName = String.format("%s_%s",fileName,fileParamVersion);
            String dfsFilePath = DFSUtils.getDfsFilePath(dfsFileBasePath, dfsFileName);
            try {
                if (!DFSUtils.getInstance().exists(dfsFileBasePath)) {
                    DFSUtils.getInstance().mkdir(dfsFileBasePath);
                }
                log.info("开始上传文件{}到DFS路径:{}",fileParamName,dfsFilePath);
                DFSUtils.getInstance().copyLocalToDfs(localFilePath, dfsFilePath, true, true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BizException("文件上传失败:" + e.getMessage());
            }

            // 脚本记录入库
            FileParam fileParam = new FileParam();
            fileParam.setCreateUser(userId);
            fileParam.setUpdateUser(userId);
            fileParam.setFileName(fileName);
            fileParam.setFileParamName(fileParamName);
            fileParam.setRemark("批量添加");
            fileParam.setFileParamType(ScriptType.USER_LEVEL);
            fileParam.setVersion(fileParamVersion);
            fileParam.setFileParamBasePath(dfsFileBasePath);
            fileParam.setFileExt(fileExt);
            fileParams.add(fileParam);
        });
        fileParamService.saveBatch(fileParams);
    }


}
