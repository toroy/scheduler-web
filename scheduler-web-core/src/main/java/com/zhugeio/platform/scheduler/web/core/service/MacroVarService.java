package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.MacroVarMapper;
import com.zhugeio.platform.scheduler.dal.po.MacroVar;
import com.zhugeio.platform.scheduler.web.core.vo.MacroVarVO;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
public class MacroVarService {

    @Resource
    private MacroVarMapper macroVarMapper;


    public int save(MacroVar macroVar) {
        Assert.notNull(macroVar);
        Assert.notNull(macroVar.getVarName(), "宏变量名称");
        Assert.notNull(macroVar.getVarExpr(), "宏变量表达式");
        return macroVarMapper.save(macroVar);
    }

    public int edit(MacroVar macroVar) {
        Assert.notNull(macroVar);
        Date updateTime = new Date();
        MacroVar condition = MacroVar.builder()
                .id(macroVar.getId()).build();
        Map<String, Object> updateParams = Maps.newHashMap();
        if (StringUtils.isNotBlank(macroVar.getVarName())) {
            updateParams.put("var_name", macroVar.getVarName());
        }
        if (StringUtils.isNotBlank(macroVar.getVarExpr())) {
            updateParams.put("var_expr", macroVar.getVarExpr());
        }
        return macroVarMapper.update(updateParams, updateTime, condition);
    }

    public int remove(Long id) {
        Assert.notNull(id, "配置ID");
        MacroVar condition = MacroVar.builder().id(id).build();
        return macroVarMapper.delete(condition);
    }

    public List<MacroVarVO> listGlobalMacroVar() {
        MacroVar macroVar = MacroVar.builder().build();
        return macroVarMapper.list(macroVar)
                .stream()
                .filter(po -> StringUtils.isNotBlank(po.getVarName()) && StringUtils.isNotBlank(po.getVarExpr()))
                .map(po ->
                        MacroVarVO.builder()
                                .varName(po.getVarName())
                                .varExpr(po.getVarExpr())
                                .build()
                )
                .collect(Collectors.toList());

    }

    public MacroVarVO getVarByName(String varName) {
        MacroVar macroVar = macroVarMapper.getByName(varName);
        return MacroVarVO.builder()
                .varName(varName)
                .varExpr(macroVar.getVarExpr())
                .build();
    }
}
