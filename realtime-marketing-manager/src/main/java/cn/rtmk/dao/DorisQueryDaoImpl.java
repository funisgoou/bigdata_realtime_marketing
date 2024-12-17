package cn.rtmk.dao;

import org.roaringbitmap.RoaringBitmap;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.HashMap;

@Repository
public class DorisQueryDaoImpl implements DorisQueryDao {
    Connection conn;
    Statement statement;
    Jedis jedis;

    public DorisQueryDaoImpl() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://doitedu:9030/test", "root", "");
        statement = conn.createStatement();
        jedis = new Jedis("doitedu", 6379);
    }

    @Override
    public void queryActionCount(String sql, String ruleId, String conditionId, RoaringBitmap bitmap) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        HashMap<String, String> guidAndCount = new HashMap<>();
        while (resultSet.next()) {
            int guid = resultSet.getInt("guid");
            //ruleId:conditionId:guId->cnt
            if (bitmap.contains(guid)) {
                long cnt = resultSet.getLong("cnt");
                guidAndCount.put(guid + "", cnt + "");
                //将查询到的事件次数条件结果，攒够一批次，发布到规则引擎用的redis存储中，作为未来滚动计算的初始值
                if(guidAndCount.size() == 1000){
                    jedis.hmset(ruleId + ":" + conditionId, guidAndCount);
                    guidAndCount.clear();
            }
        }
        }
        //将查最后不满1000的批次，发布到规则引擎用的redis存储中
        if(guidAndCount.size() > 0){
            jedis.hmset(ruleId + ":" + conditionId, guidAndCount);
        }
    }
}



