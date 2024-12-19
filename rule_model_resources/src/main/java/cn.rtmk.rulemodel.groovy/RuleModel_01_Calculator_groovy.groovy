package cn.rtmk.rulemodel.groovy;

import cn.rtmk.commom.interfaces.RuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import cn.rtmk.commom.utils.UserEventComparator;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.flink.util.Collector;
import org.roaringbitmap.RoaringBitmap;
import redis.clients.jedis.Jedis;

import java.text.ParseException;

/**
 * 事件次数类条件的运算机逻辑
 */
public class RuleModel_01_Calculator_groovy implements RuleCalculator {
    private Jedis jedis;
    private JSONObject ruleDefineParamJsonObject;
    private JSONObject eventCountConditionParam;
    private String ruleId;
    private JSONArray eventParams;
    private RoaringBitmap profileUserBitmap;
    private Collector<JSONObject> out;
    JSONObject resultObject;
    /**
     * 规则运算机的初始化方法
     *
     * @param jedis
     * @param ruleDefineParamJsonObject
     */
    @Override
    void init(Jedis jedis, JSONObject ruleDefineParamJsonObject, RoaringBitmap profileUserBitmap, Collector<JSONObject> out) {
        this.jedis = jedis;
        this.ruleDefineParamJsonObject = ruleDefineParamJsonObject;
        this.profileUserBitmap = profileUserBitmap;
        this.out = out;
        //获取规则id
        ruleId = ruleDefineParamJsonObject.getString("ruleId");
        //获取事件次数条件
        this.eventCountConditionParam = ruleDefineParamJsonObject.getJSONObject("actionCountCondition");
        //获取事件条件参数
        eventParams = eventCountConditionParam.getJSONArray("eventParams");
        //构造一个匹配结果输出对象
        resultObject = new JSONObject();
        resultObject.put("ruleId", ruleId);
    }

    /**
     * 输入事件的规则处理入口方法
     * @param userEvent 输入的用户行为事件
     * @throws ParseException
     */
    @Override
    public void process(UserEvent userEvent) throws ParseException {
        //判断本事件的行为人，是否属于本规则的画像人群
        if (profileUserBitmap.contains(userEvent.getGuid())) {
            //取出规则的触发事件条件参数Json
            JSONObject ruleTrigEvent = ruleDefineParamJsonObject.getJSONObject("ruleTrigEvent");
            //判断用户行为事件，是否是本规则的触发事件，则进行规则的匹配判断
            if (UserEventComparator.userEventIsEqualParam(userEvent, ruleTrigEvent)) {
                //如果是触发事件，则判断本行为人是否满足了本规则的所有条件
                boolean isMatch = isMatch(userEvent.getGuid());
//                log.info("用户:{},触发事件{},规则:{},规则匹配结果:{}",userEvent.getGuid(),userEvent.getEventId(),ruleEntry.getKey(),isMatch);
                //如果已满足，则输出本规则的触达信息
                if (isMatch) {
                    resultObject.put("guid", userEvent.getGuid());
                    resultObject.put("matchTime", System.currentTimeMillis());
                    out.collect(resultObject);
                }
            }
            //判断用户行为事件，如果本事件不是规则的触发事件，则进行规则的条件统计运算
            else {
                //做规则运算
                calc(userEvent);
//                log.info("收到用户:{},行为事件:{}，处理规则:{}", userEvent.getGuid(), userEvent.getEventId(), ruleEntry.getKey());
            }
        }

    }

    /**
     * 规则条件的实时运算
     *
     * @param eventBean 输入的一次用户行为
     */
    @Override
    public void calc(UserEvent eventBean) throws ParseException {
        long eventTime = eventBean.getEventTime();
        for (int i = 0; i < eventParams.size(); i++) {
            JSONObject eventParam = eventParams.getJSONObject(i);
            //取出事件条件列表中的开始时间和结束时间
            String windowStart = eventParam.getString("windowStart");
            String windowEnd = eventParam.getString("windowEnd");

            long startTime = DateUtils.parseDate(windowStart, "yyyy-MM-dd HH:mm:ss").getTime();
            long endTime = DateUtils.parseDate(windowEnd, "yyyy-MM-dd HH:mm:ss").getTime();

            //1.先判断输入的用户行为事件，是否处于规则参数约定的计算时间窗口内
            if (eventTime >= startTime && eventTime <= endTime) {
                //取出本条件的条件id
                Integer conditionId = eventParam.getInteger("conditionId");

                //2.判断当前输入的事件id，是否等于：条件参数中要求的事件id
                if (UserEventComparator.userEventIsEqualParam(eventBean, eventParam))
                //如果是，就需要去redis中，给这个用户的，这个规则的，这个条件的次数+1
                {
                    jedis.hincrBy(ruleId + ":" + conditionId, eventBean.getGuid() + "", 1);
                }
            }
        }
    }

    /**
     * 判断某用户是否满足了该条件 res0 && (res1 || res2)
     *
     * @param guid
     * @return
     */
    @Override
    public boolean isMatch(int guid) {
        //遍历每个事件条件参数
        for (int i = 0; i < eventParams.size(); i++) {
            //取出第一个条件参数
            JSONObject eventParam = eventParams.getJSONObject(i);
            //取出该条件的id
            Integer conditionId = eventParam.getInteger("conditionId");
            //取出该事件要求发生的次数
            Integer eventCount = eventParam.getInteger("eventCount");
            //去redis中查询该用户，该条件的实际发生次数
            String realCountStr = jedis.hget(ruleId + ":" + conditionId, guid + "");
            int realCount = Integer.parseInt(realCountStr == null ? "0" : realCountStr);
            //判断该用户，该条件的实际发生次数是否满足要求
        }
        return false;
    }
}
