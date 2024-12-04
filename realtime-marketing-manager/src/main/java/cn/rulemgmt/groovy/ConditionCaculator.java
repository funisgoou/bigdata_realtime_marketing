package cn.rulemgmt.groovy;

import cn.rulemgmt.pojo.EventBean;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

public interface ConditionCaculator {
    void init(Jedis jedis, JSONObject ruleDefineParamJsonObject);

    void calc(EventBean eventBean);

    boolean isMatch(int guid);
}
