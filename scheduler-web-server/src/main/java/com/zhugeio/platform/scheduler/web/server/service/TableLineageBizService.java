package com.zhugeio.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.common.utils.ParamUtils;
import com.zhugeio.platform.scheduler.common.utils.placeholder.MacroVarConvertUtils;
import com.zhugeio.platform.scheduler.dal.enums.JobCategoryEnum;
import com.zhugeio.platform.scheduler.dal.enums.LineageTypeEnum;
import com.zhugeio.platform.scheduler.dal.po.CollectDb;
import com.zhugeio.platform.scheduler.dal.po.JobOnline;
import com.zhugeio.platform.scheduler.dal.po.TableLineage;
import com.zhugeio.platform.scheduler.dal.po.TableLineageDepend;
import com.zhugeio.platform.scheduler.web.core.constant.Fields;
import com.zhugeio.platform.scheduler.web.core.service.*;
import com.zhugeio.platform.scheduler.web.core.service.basic.TableLineageBasicService;
import com.zhugeio.platform.scheduler.web.server.dto.SystemParamDto;
import com.clubfactory.platform.sqlparser.lineage.entity.TableLineageSp;
import com.clubfactory.platform.sqlparser.lineage.utils.LineageUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 血缘关系业务处理服务
 *
 * @author zhoulijiang
 * @date 2021/8/30 8:31 下午
 **/
@Service
@Slf4j
public class TableLineageBizService {

    @Resource
    TableOnlineLineageService tableOnlineLineageService;
    @Resource
    CollectDbService collectDbService;
    @Resource
    TableLineageService tableLineageService;
    @Resource
    JobOnlineService jobOnlineService;
    @Resource
    TableLineageDependService tableLineageDependService;
    @Resource
    TableOnlineLineageDependService tableOnlineLineageDependService;
    @Resource
    TableLineageBasicService tableLineageBasicService;

    private static final String HIVE = "HIVE";
    private static final String HIVE_VAR = "--hivevar";

    /**
     *  解析放到审核时，是因为用户只改脚本不改任务，触发不了任务的解析
     *  解析血缘关系放到线下表
     *  将所有血缘线下表复制到线上表
     *
     * @param jobOnlines 申请上线的线上任务/线上任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseLineage(List<JobOnline> jobOnlines) {
        if (CollectionUtils.isEmpty(jobOnlines)) {
            return;
        }
        List<Long> ids = jobOnlines.stream().map(JobOnline::getJobId).collect(Collectors.toList());
        Map<Long, CollectDb> collectDbMap = collectDbService.getCollectDbMap();
        for (JobOnline jobOnline : jobOnlines) {
            if (jobOnline.getCategroy() == JobCategoryEnum.CAL) {
                this.parseCalSql(jobOnline, jobOnline.getCreateUser());
            } else {
                this.parseCollectOrReflue(collectDbMap, jobOnline, jobOnline.getCreateUser());
            }
        }
        this.updateOnlineLineage(ids);
    }

    public void parseCalSql(JobOnline jobOnline, Long userId) {
        if (!StringUtils.equals(jobOnline.getType(), HIVE)) {
            return;
        }
        Long jobId = jobOnline.getJobId();
        // 先删
        tableLineageBasicService.delAllBy(jobId);
        String content = tableLineageBasicService.getScriptContent(jobOnline.getScriptId());

        List<TableLineageSp> tls = getTableLineageSps(jobOnline.getArgsParam(), jobOnline.getParams(), content);

        for (TableLineageSp tlsp : tls) {
            String outTable = tlsp.getOutputTable();
            List<String> inputTables = tlsp.getInputTables();
            if (StringUtils.isBlank(outTable)) {
                log.error("jobId:" + jobId + " 's outTable can't be empty");
                continue;
            } else if (CollectionUtils.isEmpty(inputTables)) {
                log.error("jobId:" + jobId + " 's inputTables can't be empty");
                continue;
            }
            CollectDb targetDb = collectDbService.getById(jobOnline.getDbTargetId());
            Long outTlId = tableLineageBasicService.createTableLineage(
                    jobId, outTable, LineageTypeEnum.CHILD, userId, targetDb);

            List<Long> inputIds = new ArrayList<>();
            for (String inputTbl : inputTables) {
                inputIds.add(tableLineageBasicService.createTableLineage(
                        jobId, inputTbl, LineageTypeEnum.PARENT, userId, targetDb));
            }
            tableLineageDependService.createDepends(outTlId, inputIds, userId, jobId);
        }

    }

    public List<TableLineageSp> getTableLineageSps(String argsParam, String params, String content) {
        List<TableLineageSp> tls = Lists.newArrayList();
        try {
            String newContent = convertVariable(content, new Date(), argsParam, params);
            tls = LineageUtils.getTableLineagesFromSqls(newContent);
            if (CollectionUtils.isEmpty(tls)) {
                log.warn("convertVariable error", content, argsParam, params);
                tls = LineageUtils.getTableLineagesFromSqls(content);
            }
        } catch (Exception e) {
            log.error("convertVariable error", content, argsParam, params, e);
        }
        return tls;
    }


    public String convertVariable(String taskScriptContent, Date taskTime, String argsParam, String params) {
        Map<String, String> argsMap = Maps.newHashMap();
        if (StringUtils.isNotBlank(argsParam) && argsParam.contains(HIVE_VAR)) {
            String[] hiveVarArr = argsParam.split(HIVE_VAR);
            for (String hiveVar : hiveVarArr) {
                String[] val = hiveVar.split("=");
                if (val.length == 2 && val[0] != null && val[1] != null) {
                    argsMap.put(val[0].trim(), val[1].trim());
                }
            }
        }
        if (StringUtils.isNotBlank(params) && !StringUtils.equalsIgnoreCase(params,"null")) {
            Map<String, String> systemParamMap = JSON.parseArray(params, SystemParamDto.class).
                    stream()
                    .filter(dto -> StringUtils.isNotBlank(dto.getName()) && StringUtils.isNotBlank(dto.getValue()))
                    .collect(Collectors.toMap(SystemParamDto::getName, SystemParamDto::getValue, (o, n) -> n));
            argsMap.putAll(systemParamMap);
        }

        Map<String,String> paramsMap = ParamUtils.convert(argsMap, taskTime);
        if (MapUtils.isNotEmpty(paramsMap) && StringUtils.isNotBlank(taskScriptContent)) {
            taskScriptContent = MacroVarConvertUtils.convertParameterPlaceholders(taskScriptContent, paramsMap);
        }
        return taskScriptContent;
    }


    public void parseCollectOrReflue(Map<Long, CollectDb> collectDbMap, JobOnline jobOnline, Long userId) {
        CollectDb srcDb = collectDbMap.get(jobOnline.getDbSourceId());
        CollectDb targetDb = collectDbMap.get(jobOnline.getDbTargetId());
        Long jobId = jobOnline.getJobId();

        // 先删
        tableLineageBasicService.delAllBy(jobId);

        Long parentTlId = tableLineageBasicService.createTableLineage(
                jobId, jobOnline.getSourceTable(), LineageTypeEnum.PARENT, userId, srcDb);
        List<Long> parentIds = new ArrayList<>();
        parentIds.add(parentTlId);

        Long childTlId = tableLineageBasicService.createTableLineage(
                jobId, jobOnline.getTargetTable(), LineageTypeEnum.CHILD, userId, targetDb);

        tableLineageDependService.createDepends(childTlId, parentIds, userId, jobId);
    }



    public void genAllJobTableLineage() {
        List<JobOnline> jobOnlines = jobOnlineService.list();
        List<List<JobOnline>> subLists = Lists.partition(jobOnlines, 100);
        for (List<JobOnline> subJobs : subLists) {
            parseLineage(subJobs);
        }
    }

    public void updateOnlineLineage(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        saveOnlineLineage(jobIds);
        saveOnlineLineageDepend(jobIds);
    }

    /**
     * 先物理删，后加
     *
     * @param jobIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOnlineLineageDepend(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        tableOnlineLineageDependService.physicsRemoveByJobIds(jobIds);

        List<TableLineageDepend> tlds = tableLineageDependService.listPoByField(Fields.JOB_ID, jobIds);
        tableOnlineLineageDependService.saveBatchFrom(tlds);
    }

    /**
     * 先逻辑删后加
     *
     * @param jobIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOnlineLineage(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }

        tableOnlineLineageService.logicRemoveByJobIds(jobIds);

        List<TableLineage> tls = tableLineageService.listPoByField(Fields.JOB_ID, jobIds);
        tableOnlineLineageService.saveBatchFrom(tls);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOnlineLineage(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return;
        }
        tableOnlineLineageService.logicRemoveByJobIds(jobIds);
        tableOnlineLineageDependService.physicsRemoveByJobIds(jobIds);
    }

    public static void main(String[] args) {
        TableLineageBizService tableLineageBizService = new TableLineageBizService();
        String content = "create table IF NOT EXISTS ods_meta.meta_demo2 as  \n" +
                "     select\n" +
                "            t2.pay_ch_date\n" +
                "            ,t2.order_type\n" +
                "            ,t2.user_id\n" +
                "            ,t2.shop_id\n" +
                "            ,t2.so_name\n" +
                "            ,t2.country_code\n" +
                "            ,t2.pay_total\n" +
                "            ,t2.product_qty\n" +
                "            ,t2.product_gmv\n" +
                "            ,t2.shipping_fee\n" +
                "            ,t2.shipping_tax_fee\n" +
                "            ,t2.tax_fee\n" +
                "            ,t2.insurance_fee\n" +
                "            ,t2.insurance_tax_fee\n" +
                "            ,t2.pay_total * nvl(t3.rate_all,0) pay_rate_all\n" +
                "            ,t2.pay_total * nvl(t3.rate_not_tax,0) pay_rate_not_tax\n" +
                "\n" +
                "            ,t1.one_id\n" +
                "            ,t1.user_type\n" +
                "            ,t1.one_channel\n" +
                "            ,t1.one_fstorder_country_code\n" +
                "            ,t1.bi_one_channel\n" +
                "            ,t1.user_fstorder_date\n" +
                "            ,t1.one_fstorder_date\n" +
                "       from (\n" +
                "            select t.one_id\n" +
                "                   ,t.user_id\n" +
                "                   ,t.user_type\n" +
                "                   ,t.one_channel\n" +
                "                   ,t.one_fstorder_country_code\n" +
                "                   ,t.bi_one_channel\n" +
                "                   ,date(t.user_fstorder_time) user_fstorder_date\n" +
                "                   ,date(t.one_fstorder_time)  one_fstorder_date\n" +
                "              from dw_dwd.user_payment_one_user_relation_df t\n" +
                "             where t.date_id = '${bizData}'\n" +
                "               and (t.user_type not in ('wholee msite:email','wholee msite:email') or t.user_type is null)\n" +
                "               and t.one_id not in ('7465205332','7307877296')\n" +
                "          group by t.one_id\n" +
                "                   ,t.user_id\n" +
                "                   ,t.user_type\n" +
                "                   ,t.one_channel\n" +
                "                   ,t.one_fstorder_country_code\n" +
                "                   ,t.bi_one_channel\n" +
                "                   ,date(t.user_fstorder_time)\n" +
                "                   ,date(t.one_fstorder_time)\n" +
                "             limit 10\n" +
                "            ) t1\n" +
                "  left join (\n" +
                "             select\n" +
                "                    t1.pay_ch_date\n" +
                "                    ,'APP_MD' order_type\n" +
                "                    ,t1.user_id\n" +
                "                    ,'0' shop_id\n" +
                "                    ,t1.order_name so_name\n" +
                "                    ,t1.country_code\n" +
                "                    ,sum(t1.origin_real_total + t1.origin_shipping_fee)  pay_total\n" +
                "                    ,sum(t1.origin_qty) product_qty\n" +
                "                    ,sum(t1.origin_real_total) product_gmv\n" +
                "                    ,sum(t1.origin_shipping_fee) shipping_fee\n" +
                "                    ,sum(0) shipping_tax_fee\n" +
                "                    ,sum(t1.origin_tax_fee) tax_fee\n" +
                "                    ,sum(0) insurance_fee\n" +
                "                    ,sum(0) insurance_tax_fee\n" +
                "               from analysts.wholee_sale_order_line_md t1\n" +
                "              where t1.month_id = '${bizData}'\n" +
                "                and t1.is_delivery = '0'\n" +
                "                and t1.is_cheating = 0\n" +
                "                and t1.pay_ch_date >= '2020-07-13'\n" +
                "           group by t1.pay_ch_date\n" +
                "                    ,'APP_MD'\n" +
                "                    ,t1.user_id\n" +
                "                    ,'0'\n" +
                "                    ,t1.order_name\n" +
                "                    ,t1.country_code\n" +
                "          union all\n" +
                "             select\n" +
                "                    t1.so_pay_ch_date pay_ch_date\n" +
                "                    ,'APP' order_type\n" +
                "                    ,t1.user_id\n" +
                "                    ,'0' shop_id\n" +
                "                    ,t1.so_name\n" +
                "                    ,t1.country_code\n" +
                "                    ,sum(t1.product_gmv + t1.shipping_fee + t1.shipping_tax_fee + t1.tax_fee + t1.insurance_fee + t1.insurance_tax_fee)  pay_total\n" +
                "                    ,sum(t1.product_qty) product_qty\n" +
                "                    ,sum(t1.product_gmv) product_gmv\n" +
                "                    ,sum(t1.shipping_fee + t1.shipping_tax_fee) shipping_fee\n" +
                "                    ,sum(t1.shipping_tax_fee) shipping_tax_fee\n" +
                "                    ,sum(t1.tax_fee) tax_fee\n" +
                "                    ,sum(t1.insurance_fee + t1.insurance_tax_fee) insurance_fee\n" +
                "                    ,sum(t1.insurance_tax_fee) insurance_tax_fee\n" +
                "               from analysts.func_tb_wholee_all_cac_app_01 t1\n" +
                "              where t1.pt = '${bizData}'\n" +
                "           group by t1.so_pay_ch_date\n" +
                "                    ,'APP'\n" +
                "                    ,t1.user_id\n" +
                "                    ,'0'\n" +
                "                    ,t1.so_name\n" +
                "                    ,t1.country_code\n" +
                "          union all\n" +
                "             select\n" +
                "                    t1.so_pay_ch_date pay_ch_date\n" +
                "                    ,'混投' order_type\n" +
                "                    ,t1.customer_id user_id\n" +
                "                    ,t1.shop_id\n" +
                "                    ,t1.shopify_order_name so_name\n" +
                "                    ,t1.country_code\n" +
                "                    ,sum(total_pay_price_usd)  pay_total\n" +
                "                    ,sum(t1.product_qty) product_qty\n" +
                "                    ,sum(t1.product_gmv) product_gmv\n" +
                "                    ,sum(t1.shipping_fee_usd) shipping_fee\n" +
                "                    ,sum(0.00) shipping_tax_fee\n" +
                "                    ,sum(t1.total_tax_usd) tax_fee\n" +
                "                    ,sum(0.00) insurance_fee\n" +
                "                    ,sum(0.00) insurance_tax_fee\n" +
                "               from analysts.func_tb_wholee_all_cac_msite_${t1} t1\n" +
                "              where t1.pt = '${bizData}'\n" +
                "           group by t1.so_pay_ch_date\n" +
                "                    ,'混投'\n" +
                "                    ,t1.customer_id\n" +
                "                    ,t1.shop_id\n" +
                "                    ,t1.shopify_order_name\n" +
                "                    ,t1.country_code\n" +
                "          union all\n" +
                "             select\n" +
                "                    t1.so_pay_ch_date pay_ch_date\n" +
                "                    ,'Amoeba' order_type\n" +
                "                    ,t1.customer_id user_id\n" +
                "                    ,t1.shop_id\n" +
                "                    ,t1.shopify_order_name so_name\n" +
                "                    ,t1.country_code\n" +
                "                    ,sum(total_pay_price_usd)  pay_total\n" +
                "                    ,sum(t1.product_qty) product_qty\n" +
                "                    ,sum(t1.product_gmv) product_gmv\n" +
                "                    ,sum(t1.shipping_fee_usd) shipping_fee\n" +
                "                    ,sum(0.00) shipping_tax_fee\n" +
                "                    ,sum(t1.total_tax_usd) tax_fee\n" +
                "                    ,sum(0.00) insurance_fee\n" +
                "                    ,sum(0.00) insurance_tax_fee\n" +
                "               from analysts.func_tb_wholee_all_cac_amoeba_01 t1\n" +
                "              where t1.pt = '${bizData}'\n" +
                "           group by t1.so_pay_ch_date\n" +
                "                    ,'Amoeba'\n" +
                "                    ,t1.customer_id\n" +
                "                    ,t1.shop_id\n" +
                "                    ,t1.shopify_order_name\n" +
                "                    ,t1.country_code\n" +
                "              limit 10\n" +
                "            ) t2\n" +
                "         on t1.user_id = t2.user_id\n" +
                "  left join (\n" +
                "            select t.data_month\n" +
                "                   ,case when t.brand = 'app'    then 'App'\n" +
                "                         when t.brand = 'amoeba' then 'Amoeba'\n" +
                "                         when t.brand = 'msite'  then '混投'\n" +
                "                       else 'Other'\n" +
                "                    end brand\n" +
                "                   ,t.rate_all\n" +
                "                   ,t.rate_not_tax\n" +
                "              from analysts.kxw_wholee_all_month_gross_rate t\n" +
                "             where t.data_date = '2021-08-30' \n" +
                "             limit 10\n" +
                "            ) t3\n" +
                "         on t2.order_type = t3.brand\n" +
                "        and substring(t2.pay_ch_date,1,7) = t3.data_month\n" +
                ";";
        List<TableLineageSp> tls = tableLineageBizService.getTableLineageSps("--hivevar bizData=${DT}  --hivevar  t1=${DT}", "[{\"name\":\"a\",\"value\":\"b\"},{\"name\":\"c\",\"value\":\"d\"}]",content);
        System.out.println(JSON.toJSONString(tls, true));

    }
}
