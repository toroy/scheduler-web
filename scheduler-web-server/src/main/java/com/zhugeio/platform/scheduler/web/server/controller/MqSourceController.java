package com.zhugeio.platform.scheduler.web.server.controller;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.MqBizService;
import com.zhugeio.platform.scheduler.web.server.vo.MqEnumVo;
import com.zhugeio.platform.scheduler.web.server.vo.MqQueryVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.web.core.dto.MqDto;
import com.zhugeio.platform.scheduler.web.server.dto.MqQueryDto;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "实时接入中心")
@RestController
@RequestMapping("/mqSource")
public class MqSourceController {
	
	@Resource
	MqBizService mqBizService;

	@ApiOperation(value="实时接入列表", notes="实时接入列表")
	@PostMapping("listByPage")
    public BaseResult<PageUtils<MqQueryVo>> listByPage(@RequestBody MqQueryDto queryDto) {
		LoginUserDto userDto = LocalUser.get();
		PageUtils<MqQueryVo> pages = mqBizService.listByPage(queryDto, userDto);
    	return new BaseResult<PageUtils<MqQueryVo>>(pages);
    }
	
	@ApiOperation(value="新增", notes="新增")
	@PostMapping("save")
    public BaseResult<Boolean> save(@RequestBody MqDto mqDto) {
		LoginUserDto userDto = LocalUser.get();
		Boolean isSuccess = mqBizService.save(mqDto, userDto);
    	return new BaseResult<Boolean>(isSuccess);
    }
	
	@ApiOperation(value="修改", notes="修改")
	@PostMapping("edit")
    public BaseResult<Boolean> edit(@RequestBody MqDto mqDto) {
		LoginUserDto userDto = LocalUser.get();
		Boolean isSuccess = mqBizService.edit(mqDto, userDto);
    	return new BaseResult<Boolean>(isSuccess);
    }
	
	@ApiOperation(value="获取该页面所有枚举列表", notes="获取该页面所有枚举列表")
	@GetMapping("listEnums")
    public BaseResult<MqEnumVo> listEnums() {
		MqEnumVo vo = mqBizService.listEnums();
    	return new BaseResult<MqEnumVo>(vo);
    }
	
	@ApiOperation(value="获取topicName", notes="获取topicName")
	@GetMapping("getTopicName")
    public BaseResult<String> getTopicName() {
		String vo = mqBizService.getTopicName();
    	return new BaseResult<String>(vo);
    }
	
	@ApiOperation(value="详情", notes="详情")
	@GetMapping("detail")
    public BaseResult<MqDto> detail(Long id) {
		LoginUserDto userDto = LocalUser.get();
		MqDto mqDto = mqBizService.get(id, userDto);
    	return new BaseResult<MqDto>(mqDto);
    }
	
	@ApiOperation(value="生成采集任务", notes="生成采集任务")
	@PostMapping("genJob")
	public BaseResult<Boolean> genJob(@RequestBody MqDto mqDto) {
		LoginUserDto userDto = LocalUser.get();
		Boolean isSuccess = mqBizService.genJob(mqDto.getId(), userDto);
    	return new BaseResult<Boolean>(isSuccess);
	}
}
