package com.bigdata.platform.scheduler.web.core.jdbc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Mongodb操作类
 * @author 陈骞
 * @Date 2020年5月19日
 *
 */
@Service
public class MongodbOperations extends BaseOperations implements Operations {


    @Override
    public void init(DbDto dbDto, String dbUrl) {
        super.init(dbDto, dbUrl);
    }


    @Override
    public List<TableDto> listTables(String dbUrl) {
        return Lists.newArrayList();
    }


    @Override
    public List<ColumnDto> listColumns(String dbUrl, String tableName) {
        return Lists.newArrayList();
    }


    @Override
    public List<ColumnDto> listColumns(String dbUrl, String dbName, String tableName) {
        Assert.notNull(dbUrl);
        Assert.notBlank(tableName, "表名");
        if (StringUtils.isBlank(dbName)) {

        }

        MongoClientURI uri = new MongoClientURI(dbUrl);
        MongoClient mongoClient = new MongoClient(uri);
        MongoCollection<Document> readCollect = mongoClient
                .getDatabase(dbName).getCollection(tableName);
        JSONArray jsonArray = new JSONArray();
        Block<Document> processBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                jsonArray.add(JSON.parse(document.toJson()));
            }
        };
        // 倒序查询：可根据mongoId来
        Bson sort = Filters.eq("_id", -1);
        readCollect.find().sort(sort).limit(10).forEach(processBlock);

        Map<String, String> fieldTypeMap = Maps.newHashMap();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jobj = jsonArray.getJSONObject(i);
            for (Map.Entry<String, Object> entry : jobj.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue().toString();

                if (fieldTypeMap.containsKey(key)) {
                    continue;
                }
                if (NumberUtils.isDigits(val)) {
                    fieldTypeMap.put(key, "long");
                } else if (NumberUtils.isDigits(val.replaceAll("[.]", ""))) {
                    fieldTypeMap.put(key, "double");
                } else {
                    fieldTypeMap.put(key, "string");
                }
            }
        }
        List<ColumnDto> columnDtos = Lists.newArrayList();
        for (Map.Entry<String, String> entry : fieldTypeMap.entrySet()) {
            ColumnDto columnDto = new ColumnDto();
            columnDto.setName(entry.getKey());
            columnDto.setType(entry.getValue());
            columnDto.setDesc("");
            columnDtos.add(columnDto);
        }
        return columnDtos;
    }


    @Override
    public void close(String dbUrl) {
        Assert.notNull(dbUrl);
        super.close(dbUrl);
    }

}
