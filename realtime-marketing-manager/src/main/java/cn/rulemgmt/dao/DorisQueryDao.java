package cn.rulemgmt.dao;

import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.HashMap;

@Repository
public class DorisQueryDao {
    Connection conn;
    Statement statement;
    Jedis jedis;
    public DorisQueryDao() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://doitedu:9030/test", "root", "");
        statement= conn.createStatement();
        jedis= new Jedis("doitedu", 6379);
    }
    public void queryActionCount(String sql,String ruleId,String conditionId) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        HashMap<String, String> guidAndCount = new HashMap<>();
        while (resultSet.next()) {
            int guid=resultSet.getInt("guid");
            long cnt = resultSet.getInt("cnt");
            //ruleId:conditionId:guId->cnt
            guidAndCount.put(guid+"",cnt+"");
        }
        //将查询到的事件次数条件发布到redis中，作为未来滚动计算的初始值
        jedis.hmset(ruleId+":"+conditionId,guidAndCount);
    }

    public static void main(String[] args) throws SQLException {
        DorisQueryDao dorisQueryDao = new DorisQueryDao();
        String sql="select\n" +
                "guid,\n" +
                "count(1) as cnt\n" +
                "from mall_app_events_detail\n" +
                "where 1=1\n" +
                "and event_time>=\"2022-08-01 00:00:00\"\n" +
                "and event_time<=\"2022-08-31 12:00:00\"\n" +
                "and event_id=\"e2\"\n" +
                "and get_json_string(propJson,'$.pageId')='page001'\n" +
                "and get_json_string(propJson,'$.itemId')='item002'\n" +
                "group by guid";
        dorisQueryDao.queryActionCount(sql,"rule001","1");
    }
}
