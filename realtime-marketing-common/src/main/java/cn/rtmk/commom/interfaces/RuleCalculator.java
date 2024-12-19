package cn.rtmk.commom.interfaces;


import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.util.Collector;
import org.roaringbitmap.RoaringBitmap;
import redis.clients.jedis.Jedis;

import java.text.ParseException;

/**
 * 规则运算机的统一接口
 */
public interface RuleCalculator {
    /**
     * 运算机初始化
     * @param jedis redis客户端
     * @param ruleDefineParamJsonObject 规则参数Json对象
     * @param profileUserBitmap  人群画像bitmap
     * @param out flink的结果输出器
     */

    void init(Jedis jedis, JSONObject ruleDefineParamJsonObject, RoaringBitmap profileUserBitmap, Collector<JSONObject> out);

    /**
     * 对输入事件进行规则处理的入口方法
     * @param userEvent  输入的用户行为事件
     */
    void process(UserEvent userEvent) throws ParseException;

    /**
     * 规则条件运算
     * @param userEvent  用户事件
     */
    void calc(UserEvent userEvent) throws ParseException;

    /**
     * 规则条件是否满足的判断逻辑
     * @param guid 用户标识
     * @return 是否满足
     */
    boolean isMatch(int guid);
}
