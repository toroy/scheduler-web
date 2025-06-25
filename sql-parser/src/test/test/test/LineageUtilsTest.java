package test;

import com.zhugeio.platform.sqlparser.lineage.entity.TableLineageSp;
import com.zhugeio.platform.sqlparser.lineage.utils.LineageUtils;
import com.zhugeio.platform.sqlparser.lineage.utils.basic.LineageBasic;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineageUtilsTest {
    
    @Test
    public void parseSqlContainsWith() throws Exception {
        String sql = "use ods_kafka; with with1 as ( select f1 from src1 where key = '5'), with2 as ( select f2 from src2 a inner join src3 b on a.id = b.id) " +
                        "\n--as\n insert overwrite table temp.dt_mobile_play_d_tmp2 partition(dt='2018-07-17') select * from with1 cross join with2;";
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        System.out.println();
    }
    
    
    @Test
    public void getTableLineagesFromSqls1() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_1.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "dw_ods.order_center_trade_pay_df"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "dw_dwd.sale_payment_pay_df".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }

    @Test
    public void getTableLineagesFromSqls2() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_2.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "dw_dwd.sale_fulfillment_order_line_df",
                "dw_dwd.sale_fulfillment_order_df",
                "dw_dwd.logistics_forward_track_info_df",
                "jiayundw_dm.sale_transfer_order_info_df",
                "dw_dwd.sale_fulfillment_cancel_info_da",
                "dw_dwd.sale_rating_line_dd",
                "dw_dws.sale_refund_order_sku_success_df",
                "dw_dws.sale_refund_order_sku_reason_success_df",
                "dw_dwd.sale_order_order_line_df",
                "dw_dws.sale_order_order_df",
                "dw_dim.product_sku_info_df",
                "dw_dwd.sale_payment_order_df",
                "dw_dim.shipping_address",
                "dw_dwd.sale_order_cancel_info_da",
                "dw_dws.sale_retrun_order_sku_success_df",
                "jiayundw_dws.datapark_user_merge_result_ma"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "dw_dm.sale_order_order_line_df".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }

    @Test
    public void getTableLineagesFromSqls3() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_3.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "dw_ods.order_center_order_state_history_da",
                "dw_ods.order_center_cancel_code_constant_df",
                "dw_ods.cf_oms_operator_history_dd"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "dw_dwd.sale_order_cancel_info_da".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }

    @Test
    public void getTableLineagesFromSqls4() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_4.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "ods_purchase_india.shipping_ticket",
                "ods_purchase_cn.shipping_ticket"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "dw_ods.branch_table_shipping_ticket_df".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }


    @Test
    public void getTableLineagesFromSqls5() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_5.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        boolean as11 = "dw_dim.product_sku_info_df".equals(tls.get(0).getOutputTable());
        boolean as12 = tls.get(0).getInputTables().containsAll(Arrays.asList(
                "dw_dim.product_basic_info_df", "ods_price.price_purchase_price",
                "dw_tmp.dim_sku_weight", "dw_ods.odoo_product_product_df",
                "dw_dim.base_exchange_rate_dd", "dw_ods.price_price_sku_price_df",
                "dw_tmp.dim_sku_attribute", "ods_odoo.product_product_offline_reason"
        ));
        System.out.println(as11 && as12);

        boolean as21 = "dw_tmp.dim_sku_weight".equals(tls.get(1).getOutputTable());
        boolean as22 = tls.get(1).getInputTables().containsAll(Arrays.asList(
                "dw_dim.product_basic_info_df", "dw_ods.odoo_product_product_df",
                "ods_price.price_product_weight", "ods_price.price_subcategory_weight",
                "ods_price.price_sku_weight_aggregate"
        ));
        System.out.println(as21 && as22);

        boolean as31 = "dw_tmp.dim_sku_attribute".equals(tls.get(2).getOutputTable());
        boolean as32 = tls.get(2).getInputTables().containsAll(Arrays.asList(
                "ods_odoo.product_attribute_value", "dw_ods.odoo_product_product_df",
                "ods_odoo.product_attribute_value_product_product_rel", "ods_odoo.product_attribute"
        ));
        System.out.println(as31 && as32);
    }


    @Test
    public void getTableLineagesFromSqls6() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_6.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "dw_ods.branch_table_sale_logistics_df", "dw_dwd.sale_fulfillment_order_df",
                "dw_dwd.sale_fulfillment_order_df", "dw_dwd.sale_fulfillment_order_df",
                "dw_ods.order_center_fulfillment_order_df", "dw_ods.order_center_state_history_da",
                "dw_ods.seller_order_shipping_workflow_df", "dw_ods.seller_order_shipping_workflow_history_df",
                "dw_ods.order_center_fulfillment_order_df", "dw_ods.branch_table_sale_logistics_df",
                "dw_dwd.sale_fulfillment_order_df", "dw_dwd.sale_fulfillment_order_df",
                "dw_ods.branch_table_logistics_attribute_df", "dw_ods.club_wms_prod_shipping_container_header_df",
                "dw_ods.branch_table_stock_picking_df", "dw_ods.branch_table_stock_picking_log_da",
                "dw_ods.club_wms_prod_shipment_header_df", "dw_ods.club_wms_prod_shipment_history_df"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "dw_dwd.logistics_sale_logistics_df".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }


    @Test
    public void getTableLineagesFromSqls7() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_7.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "analysts.cbb_tbl_warehouse_date_df", "analysts.cbb_tbl_warehouse_date_df",
                "analysts.sc_kpi_dtl_wh_po_ib_df", "analysts.sc_kpi_dtl_wo_to_ob_df",
                "analysts.sc_kpi_dtl_wh_ob_df"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "analysts.sc_kpi_dw_wh_df".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }


    @Test
    public void getTableLineagesFromSqls8() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_8.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        List<TableLineageSp> tls = LineageUtils.getTableLineagesFromSqls(sql);
        Set<String> set = new HashSet<>(Arrays.asList(
                "jiayundw_dwd.flow_user_trace_click_da", "jiayundw_dim.product_basic_info_df",
                "jiayundw_dwd.flow_user_trace_click_da", "jiayundw_dim.product_basic_info_df"
        ));
        boolean as = tls.get(0).getInputTables().containsAll(set);
        boolean as2 = "analysts.search_detail_click_02".equalsIgnoreCase(tls.get(0).getOutputTable());
        System.out.println(as && as2);
    }


    @Test
    public void testRegex() throws Exception {
        Pattern r = Pattern.compile(LineageBasic.REGEX_REPLACE_PARAMS);
        String str = "dw_tmp.sale_order_cancel_${dt_nodash_1}_info_da${dt_nodash_1}_${dt_nodash_2}_01";
        Matcher m = r.matcher(str);
        Set<String> set = new HashSet<>();
        while (m.find()) {
            set.add(m.group());
        }
        System.out.println(set);
//        System.out.println(str.replaceAll(LineageBasic.REGEX_REPLACE_PARAMS, LineageBasic._VAR_PLACEHOLDER_));
    }




}
