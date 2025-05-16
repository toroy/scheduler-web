package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.web.core.service.MacroVarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiejiajun
 */
@Service
public class MacroVarBizService {

    @Autowired
    private MacroVarService macroVarService;


    public String getVarByName(String varName) {
        Assert.notBlank(varName,"常量名称");
        return macroVarService.getVarByName(varName).getVarExpr();
    }
}
