package cn.rtmk.dao;

import cn.rtmk.commom.pojo.ActionSeqParam;
import cn.rtmk.commom.pojo.EventParam;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
                if (guidAndCount.size() == 1000) {
                    jedis.hmset(ruleId + ":" + conditionId, guidAndCount);
                    guidAndCount.clear();
                }
            }
        }
        //将查最后不满1000的批次，发布到规则引擎用的redis存储中
        if (guidAndCount.size() > 0) {
            jedis.hmset(ruleId + ":" + conditionId, guidAndCount);
        }
    }

    @Override
    public void queryActionSeq(String sql, String ruleId, ActionSeqParam actionSeqParam, RoaringBitmap bitmap) throws SQLException {
        List<EventParam> eventParams = actionSeqParam.getEventParams();
        String redisSeqStepKey = ruleId + ":" + actionSeqParam.getConditionId() + "step";
        String redisSeqCntKey = ruleId + ":" + actionSeqParam.getConditionId() + "cnt";

        // 3,"e2_2022-08-01 14:32:35^e3_2022-08-01 14:33:35^e1_2022-08-01 14:34:35"
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            int guid = resultSet.getInt(1);
          if (bitmap.contains(guid)){
              String actionSeqStr = resultSet.getString(2);
              String[] split = actionSeqStr.split("\\^");
              //对事件序列排序
              Arrays.sort(split, new Comparator<String>() {
                  @Override
                  public int compare(String o1, String o2) {
                      String[] split1 = o1.split("_");
                      String[] split2 = o2.split("_");
                      return split1[1].compareTo(split2[1]);
                  }
              });
              //条件序列中的比较位置
              int k = 0;
              int matchCount = 0;
              //遍历查询出来的行为序列
              for (int i = 0; i < split.length; i++) {
                  if (split[i].split("_")[0].equals(eventParams.get(k).getEventId())) {
                      k++;
                      if (k == eventParams.size()) {
                          k = 0;
                          matchCount++;
                      }
                  }
              }
              //往redis插入该用户的待完成序列的已到达步骤号
              jedis.hset(redisSeqStepKey, guid + "", k + "");
              //往redis插入该用户的序列条件的已完成次数
              jedis.hset(redisSeqCntKey, guid + "", matchCount + "");
          }
        }

    }
}




