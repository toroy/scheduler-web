package com.bigdata.platform.scheduler.web.server.service;

import java.util.List;

import javax.annotation.Resource;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.bigdata.platform.scheduler.dal.enums.DbType;
import com.bigdata.platform.scheduler.dal.po.CollectDb;
import com.bigdata.platform.scheduler.web.core.jdbc.client.HiveOperations;
import com.bigdata.platform.scheduler.web.core.jdbc.client.MysqlOperations;
import com.bigdata.platform.scheduler.web.core.jdbc.client.PostgresqlOperations;
import com.bigdata.platform.scheduler.web.core.jdbc.client.RedshiftOperations;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.bigdata.platform.scheduler.web.core.service.CollectDbService;
import com.bigdata.platform.scheduler.web.core.vo.CollectDbVO;


public class JdbcTest extends BaseTest {

	@Resource
	HiveOperations hiveOperations;
	@Resource
	MysqlOperations mysqlOperations;
	@Resource
	PostgresqlOperations postgresqlOperations;
	@Resource
	RedshiftOperations redshiftOperations;
	@Resource
	CollectDbService collectDbService;
	
	@Test
	public void getColumns() {
		List<CollectDbVO>  dbs = collectDbService.list(new CollectDb());
		
		for (CollectDbVO db : dbs) {
			DbDto dbDto = new DbDto();
			dbDto.setDbType(db.getDsType());
			dbDto.setDbUrl(db.getDsUrl());
			dbDto.setDbUser(db.getDsUser());
			dbDto.setDbPwd(db.getDsPassword());
			
			List<ColumnDto> columnDtos = Lists.newArrayList();
			String dbUrl = db.getDsUrl();
			if (db.getDsType() == DbType.MYSQL) {
				mysqlOperations.init(dbDto, dbUrl);
				columnDtos = mysqlOperations.listColumns(dbUrl, "sc_user");
			} else if (db.getDsType() == DbType.POSTGRESQL) {
				postgresqlOperations.init(dbDto, dbUrl);
				columnDtos = postgresqlOperations.listColumns(dbUrl, "proxy_user");
			} else if (db.getDsType() == DbType.HIVE) {
				hiveOperations.init(dbDto, dbUrl);
				columnDtos = hiveOperations.listColumns(dbUrl, "test");
			} else 
			if (db.getDsType() == DbType.REDSHIFT) {
				redshiftOperations.init(dbDto, dbUrl);
				columnDtos = redshiftOperations.listColumns(dbUrl, "test.eor_ior_tmp_p2");
			}
			
			Assert.assertNotSame(columnDtos.size(), 0);
			System.out.println(db.getDsType()+ ": " + JSON.toJSONString(columnDtos));
		}
		
	}
	
	@Test
	public void getHiveColumn() {
		DbDto dbDto = new DbDto();
		dbDto.setDbType(DbType.HIVE);
		dbDto.setDbUrl("jdbc:hive2://ec2-52-13-15-144.us-west-2.compute.amazonaws.com:10000/default");
		dbDto.setDbUser("hive");
		dbDto.setDbPwd("");
		
		hiveOperations.init(dbDto, dbDto.getDbUrl());
		List<ColumnDto> columnDtos = hiveOperations.listColumns(dbDto.getDbUrl(), "ods_order_center.cancel_code_constant");
		System.out.println(JSON.toJSONString(columnDtos));
	}
}
