package test.mongodb;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.clubfactory.platform.scheduler.web.core.jdbc.client.MongodbOperations;
import com.clubfactory.platform.scheduler.web.core.jdbc.client.PhoenixOperations;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DbClientTest {

    @Test
    public void mongodb() {
        MongodbOperations mo = new MongodbOperations();
        String url = "mongodb://mongowriter:1kiouk89-epiCLOUDS2018@123.207.91.167:27017";
        System.out.println(mo.listColumns(
                url,
                "amazon",
                "category_item"
        ));
    }


    @Test
    public void phoenix() {
        PhoenixOperations mo = new PhoenixOperations();
        System.out.println(mo.listColumns(
                "jdbc:phoenix:localhost:2181",
                "student.baseinfo"
        ));
    }


}
