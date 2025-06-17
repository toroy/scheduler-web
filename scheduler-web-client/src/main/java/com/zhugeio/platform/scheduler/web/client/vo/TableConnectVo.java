package com.zhugeio.platform.scheduler.web.client.vo;

import com.zhugeio.platform.scheduler.web.client.dto.TableConnectDto;
import com.zhugeio.platform.scheduler.web.client.enums.DbTypeEnum;
import lombok.Data;

@Data
public class TableConnectVo extends TableConnectDto {

	private static final long serialVersionUID = -6817761651365154790L;
	
	/**
	 * 数据库用户名
	 */
	private String dsUser;
	
	/**
	 * 数据库密码
	 */
	private String dsPassword;
	
	/**
	 * 数据库属性
	 */
	private DbTypeEnum dsType;
	
	/**
	 * 数据库链接信息
	 */
	private String dsUrl;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (obj instanceof TableConnectVo) {
			TableConnectVo vo = (TableConnectVo) obj;

			// 比较每个属性的值一致时才返回true
			if (vo.getDsType().equals(this.getDsType())
					&& vo.dsUrl.equals(this.dsUrl)
					&& vo.getTableName().equals(this.getTableName())
					&& vo.getDbName().equals(this.getDbName()))
				return true;
		}
		return false;
	}


}
