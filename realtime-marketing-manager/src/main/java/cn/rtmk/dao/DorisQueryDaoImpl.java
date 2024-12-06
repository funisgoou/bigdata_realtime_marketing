package cn.rtmk.dao;

import org.roaringbitmap.RoaringBitmap;
<<<<<<<< HEAD:realtime-marketing-manager/src/main/java/cn/rtmk/dao/DorisQueryDaoImpl.java
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
========
>>>>>>>> df534a4d0e902fc2678d02000b76a482d13e1b76:realtime-marketing-manager/src/main/java/cn/rulemgmt/dao/DorisQueryDao.java

import java.sql.SQLException;

<<<<<<<< HEAD:realtime-marketing-manager/src/main/java/cn/rtmk/dao/DorisQueryDaoImpl.java
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

========
public interface DorisQueryDao {
    void queryActionCount(String sql, String ruleId, String conditionId, RoaringBitmap bitmap) throws SQLException;
>>>>>>>> df534a4d0e902fc2678d02000b76a482d13e1b76:realtime-marketing-manager/src/main/java/cn/rulemgmt/dao/DorisQueryDao.java
}
