package cn.rtmk.commom.interfaces;


import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * 规则运算机的统一接口
 */
public interface RuleConditionCaculator {
    /**
     * 运算机初始化
     * @param jedis redis客户端
     * @param ruleDefineParamJsonObject 规则参数Json对象
     */
    void init(Jedis jedis, JSONObject ruleDefineParamJsonObject);

    /**
     * 规则条件运算
     * @param userEvent  用户事件
     */
    void calc(UserEvent userEvent);

    /**
     * 规则条件是否满足的判断逻辑
     * @param guid 用户标识
     * @return 是否满足
     */
    boolean isMatch(int guid);
}
