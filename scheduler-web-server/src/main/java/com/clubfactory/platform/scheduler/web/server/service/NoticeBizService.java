package com.clubfactory.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.clubfactory.platform.common.constant.DateFormatPattern;
import com.clubfactory.platform.common.util.DateUtil;
import com.clubfactory.platform.scheduler.dal.enums.JobTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.PriorityEnum;
import com.clubfactory.platform.scheduler.dal.po.Job;
import com.clubfactory.platform.scheduler.dal.po.JobOnline;
import com.clubfactory.platform.scheduler.dal.po.Task;
import com.clubfactory.platform.scheduler.web.core.constant.SysConfigConstant;
import com.clubfactory.platform.scheduler.web.core.service.MachineService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.utils.OkHttp3Utils;
import com.clubfactory.platform.scheduler.web.core.utils.SysConfigUtil;
import com.clubfactory.platform.scheduler.web.server.constant.MsgTypeEnum;
import com.clubfactory.platform.scheduler.web.server.constant.NoticeConstant;
import com.clubfactory.platform.scheduler.web.server.dto.IMDto;
import com.clubfactory.platform.scheduler.web.server.dto.IMDto.Markdown;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NoticeBizService {

	@Resource
	UserService userService;
	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	MachineService machineService;
	
	ExecutorService executor = new ThreadPoolExecutor(200, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2000));
	
	private static final String MACHINE_DEFAULT_NAME = "系统随机";
	private static final String WEB_URL = "%s/#/app/job-management/job-detail/%s";
	private static final String SCRIPT_URL = "%s/script/getContentById?id=%s";
	private static final String CHECK_URL = "%s/job/editSuccessByJobId?jobId=%s";
	
	public void checkBatchSuccess(LoginUserDto userDto, List<Job> jobs) {
		String checkUser = userService.getUserName(userDto.getLocalUserId()); 
		for (Job job : jobs) {
			// DQC的不通知
			if (job.getJobType() == JobTypeEnum.DQC) {
				continue;
			}
			executor.submit(new Runnable() {
				@Override
				public void run() {
					checkManageSuccess(job, checkUser);
		            checkUserSuccess(job, checkUser);
				}
			});
		}
	}

	private void checkManageSuccess(Job job, String checkUser) {
		String host = SysConfigUtil.getByKey(SysConfigConstant.WEB_HOST);
		String msg = String.format(NoticeConstant.CHECK_SUCCESS_BODY
				, job.getName()
				, String.format(WEB_URL, host, job.getId())
				, checkUser);
        sendMarkdownMsg(SysConfigUtil.getByKey(SysConfigConstant.ADMIN_IM_URL), msg);
	}

    private void checkUserSuccess(Job job, String checkUser) {
        String msg = String.format(NoticeConstant.CHECK_USER_SUCCESS_BODY
                , job.getName()
                , job.getId());

        // 获取对应用户
        String uid = userService.getUid(job.getUpdateUser());
        sendTextMsg(SysConfigUtil.getByKey(SysConfigConstant.USER_IM_URL), msg, uid);
    }

	public void noticeCheck(List<Task> tempTasks, Job job, JobOnline jobOnline, LoginUserDto userDto) {
		Boolean isExistTemp = false;
		String startDate = null;
		Long dur = null;
		if (CollectionUtils.isNotEmpty(tempTasks)) {
			isExistTemp = true;
			Task tempTask = tempTasks.stream().filter(task -> task.getEndTime() != null)
					.max(Comparator.comparing(Task::getEndTime)).orElse(new Task());
			startDate = DateUtil.format(tempTask.getStartTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
			dur = (tempTask.getEndTime().getTime() - tempTask.getExecTime().getTime()) / 1_000;
		}

		Long checkUserId = Optional.ofNullable(jobOnline).orElse(new JobOnline()).getCheckUser();
		String checkUserName = null;
		if (checkUserId != null) {
			checkUserName = userService.getUserName(checkUserId);
		}

		String machineName = MACHINE_DEFAULT_NAME;
		if (job.getMachineId() != 0) {
			 machineName = machineService.getMachineById(job.getMachineId()).getName();
		}
		String host = SysConfigUtil.getByKey(SysConfigConstant.WEB_HOST);
		
		String targetName = jobDetailBizService.getTargetName(job.getId(), job.getCategroy());
		
		String msg = String.format(NoticeConstant.CHECK_BODY
				, job.getName()
				, String.format(WEB_URL, host, job.getId())
				, userService.getUserName(userDto.getLocalUserId())
				, job.getId()
				, userService.getUserName(job.getCreateUser())
				, job.getCategroy().getDesc()
				, job.getType()
				, targetName
				, checkUserName
				, job.getRunOnTmpEmr()
				, machineName
				, PriorityEnum.getByCode(job.getPriority()).getDesc()
				, isExistTemp
				, startDate
				, dur
				, String.format(SCRIPT_URL, host, job.getScriptId())
				, String.format(CHECK_URL, host, job.getId()));

        sendMarkdownMsg(SysConfigUtil.getByKey(SysConfigConstant.ADMIN_IM_URL), msg);
	}

	private void sendMarkdownMsg(String address, String content) {
		IMDto dto = new IMDto();
		dto.setMsgtype(MsgTypeEnum.MARKDOWN.name().toLowerCase());
		Markdown markdown = new Markdown();
		markdown.setContent(content);
		dto.setMarkdown(markdown);
        sendMsg(address, dto);
    }

    private void sendTextMsg(String address, String content, String userId) {
        IMDto dto = new IMDto();
        dto.setMsgtype(MsgTypeEnum.TEXT.name().toLowerCase());
        IMDto.Text text = new IMDto.Text();
        text.setContent(content);
        text.setMentionedList(Lists.newArrayList(userId));
        dto.setText(text);
        sendMsg(address, dto);
    }

    private void sendMsg(String address, IMDto dto) {
        String dtoString = JSON.toJSONString(dto);
        Response data = null;
        try {
        	data = OkHttp3Utils.post(address, dtoString);
            if (data == null || !data.isSuccessful()) {
                log.error("address:{}, dto:{}, res:{}", address, dtoString, data);
            }
		} catch (Exception e) {
			log.error("address post error : {}",address, e);
		} finally {
			if (data != null) {
				data.close();	
			}
		}
    }

}
