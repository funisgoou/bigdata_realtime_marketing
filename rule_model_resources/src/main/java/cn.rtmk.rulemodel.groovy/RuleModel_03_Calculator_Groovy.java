package cn.rtmk.rulemodel.groovy;

import cn.rtmk.commom.interfaces.TimeRuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import cn.rtmk.commom.utils.UserEventComparator;
import com.alibaba.fastjson.JSONObject;

import groovy.util.logging.Slf4j;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.streaming.api.TimerService;

import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 事件次数类条件的运算机逻辑
 */
@Slf4j
class RuleModel_03_Calculator_Groovy extends TimeRuleCalculator {
    private static final Logger log = LoggerFactory.getLogger(RuleModel_03_Calculator_Groovy.class);
    JSONObject ruleDefineParamJsonObject;
    String ruleId;
    RoaringBitmap profileUserBitmap;
    JSONObject trigEventJsonObject;
    JSONObject checkEventJsonObject;
    long intervalTime;
    int maxMatchCount;
    String checkEventAttribute;
    Jedis jedis;

    @Override
    public void init(JSONObject ruleDefineParamJsonObject, RoaringBitmap profileUserBitmap) {
        this.jedis = new Jedis("doitedu", 6379);

        this.ruleDefineParamJsonObject = ruleDefineParamJsonObject;
        this.profileUserBitmap = profileUserBitmap;

        this.trigEventJsonObject = ruleDefineParamJsonObject.getJSONObject("ruleTrigEvent");
        this.checkEventJsonObject = ruleDefineParamJsonObject.getJSONObject("checkEvent");
        this.checkEventAttribute = checkEventJsonObject.getString("eventAttribute");
        this.intervalTime = ruleDefineParamJsonObject.getLong("interval_time");
        this.maxMatchCount = ruleDefineParamJsonObject.getInteger("rule_match_count");
        this.ruleId = ruleDefineParamJsonObject.getString("ruleId");
    }

    @Override
    public List<JSONObject> process(UserEvent userEvent) throws Exception {
        return null;
    }

    @Override
    public List<JSONObject> process(UserEvent userEvent, MapState<String, Long> timerState, TimerService timerService) throws Exception {
        List<JSONObject> resList = new ArrayList<JSONObject>();
        //判断是否是触发事件
        if (UserEventComparator.userEventIsEqualParam(userEvent, trigEventJsonObject)) {
            //计算出要注册的定时器的时间
            long registerTime = timerService.currentProcessingTime() + intervalTime;
            //注册定时器
            timerService.registerProcessingTimeTimer(registerTime);
            //从事件中拿到规则检查条件的事件属性
            String checkEventAttributeValue = userEvent.getProperties().get(this.checkEventAttribute);
            //将定时器注册信息，记录到timerState中
            timerState.put(ruleId + ":" + checkEventAttributeValue, registerTime);
            JSONObject resObj = new JSONObject();
            resObj.put("ruleId", ruleId);
            resObj.put("resType", "timerReg");
            resObj.put("guid", userEvent.getGuid());
            resObj.put("checkEventAttribute", checkEventAttribute);
            resObj.put("checkEventAttributeValue", checkEventAttributeValue);
            resObj.put("registerTimestamp", timerService.currentProcessingTime());
            resObj.put("registeredTimeStamp", registerTime);
            resList.add(resObj);
        }
        //判断是否是检查事件
        if (userEvent.getEventId() == checkEventJsonObject.getString("eventId")) {
            //从事件中拿到规则检查条件的事件属性
            String checkEventAttributeValue = userEvent.getProperties().get(this.checkEventAttribute);
            //从状态中，查找到之前注册的定时器时间
            Long registerTime = timerState.get(ruleId + ":" + checkEventAttributeValue);

            if (registerTime != null) {
                //timerService.deleteProcessingTimeTimer(registerTime);
                timerState.remove(ruleId + ":" + checkEventAttributeValue);
            }
            JSONObject resObj = new JSONObject();
            resObj.put("ruleId", ruleId);
            resObj.put("resType", "timerDel");
            resObj.put("guid", userEvent.getGuid());
            resObj.put("checkEventAttribute", checkEventAttribute);
            resObj.put("checkEventAttributeValue", checkEventAttributeValue);
            resObj.put("timestamp", timerService.currentProcessingTime());
            resObj.put("registerTime", registerTime);
            resList.add(resObj);
        }
        return resList;
    }

    /**
     * @param timestamp 定时器触发时间
     */
    @Override
    public List<JSONObject> onTimer(long timestamp, int guid, MapState<String, Long> timerState, TimerService timerService) throws Exception {
        List<JSONObject> onTimerResList = new ArrayList<JSONObject>();
        log.info("定时器触发了，时间为:{}",timestamp);
        Iterable<Map.Entry<String, Long>> entries = timerState.entries();
        String attributeValue = "";
        for (Map.Entry<String, Long> entry : entries) {
            if (entry.getValue() == timestamp) {
                String[] keySplit = entry.getKey().split(":");
                attributeValue = keySplit[1];
                log.info("找到了一个在此时间点注册了一个定时器的规则，规则:{},订单号:{}",ruleId,attributeValue);

                //查询出该用户属性（订单）的规则触达次数
                String realMatchCountStr = jedis.hget(ruleId + ":mcnt", guid + ":" + attributeValue);
                int realMatchCount = realMatchCountStr == null ? 0 : Integer.parseInt(realMatchCountStr);
                log.info("找到了这个订单的实际提醒次数，规则:{},订单号：{},触达次数:{}",ruleId,attributeValue,realMatchCount);

                //如果尚未达到触达上限
                if (realMatchCount < maxMatchCount) {
                    log.info("这个订单的实际提醒次数没有达到上限:{}，规则:{},订单号：{},触达次数:{}",maxMatchCount,ruleId,attributeValue,realMatchCount);
                    //封装要返回的触达结果
                    JSONObject resObj = new JSONObject();
                    resObj.put("ruleId", ruleId);
                    resObj.put("resType", "match");
                    resObj.put("guid", guid);
                    resObj.put("attributeValue", attributeValue);
                    resObj.put("timestamp", timerService.currentProcessingTime());
                    onTimerResList.add(resObj);

                    //更新redis中的触达次数
                    jedis.hincrBy(ruleId + ":mcnt", guid + ":" + attributeValue, 1);

                    //再次注册定时器
                    long registerTime = this.intervalTime + timestamp;
                    timerService.registerProcessingTimeTimer(registerTime);
                    timerState.put(ruleId + ":" + attributeValue, registerTime);
                    log.info("再次注册定时器:规则:{},订单号：{},注册时间:{}",ruleId,attributeValue,registerTime);
                }
                break;
            }
        }


        return onTimerResList;
    }
}
