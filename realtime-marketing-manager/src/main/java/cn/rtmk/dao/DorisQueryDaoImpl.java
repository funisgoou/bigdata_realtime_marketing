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
        statement= conn.createStatement();
        jedis= new Jedis("doitedu", 6379);
    }
    @Override
    public void queryActionCount(String sql, String ruleId, String conditionId, RoaringBitmap bitmap) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        HashMap<String, String> guidAndCount = new HashMap<>();
        while (resultSet.next()) {
            int guid=resultSet.getInt("guid");
            //ruleId:conditionId:guId->cnt
            if (bitmap.contains(guid)){
                long cnt = resultSet.getLong("cnt");
                guidAndCount.put(guid+"",cnt+"");
            }
        }
        //将查询到的事件次数条件发布到redis中，作为未来滚动计算的初始值
        jedis.hmset(ruleId+":"+conditionId,guidAndCount);
    }

}
