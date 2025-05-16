package com.bigdata.platform.scheduler.web.core.enums;

import com.bigdata.platform.scheduler.dal.enums.DbFeatureEnum;
import com.bigdata.platform.scheduler.dal.enums.JobCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JobPageType {

	DBETL(JobCategoryEnum.COLLECT, DbFeatureEnum.COLLECTION),
	CAL(JobCategoryEnum.CAL, null),
	DBSYNC(JobCategoryEnum.REFLUE,DbFeatureEnum.SYNC),
	TASK(null, null);
	
	private JobCategoryEnum type;

	private DbFeatureEnum dbFeatureEnum;
}
