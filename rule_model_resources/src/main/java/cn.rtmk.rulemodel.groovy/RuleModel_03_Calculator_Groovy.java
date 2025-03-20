package cn.rtmk.rulemodel.groovy;

import cn.rtmk.commom.interfaces.TimeRuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import cn.rtmk.commom.utils.UserEventComparator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.streaming.api.TimerService;

import org.roaringbitmap.RoaringBitmap;

import java.util.List;

/**
 * 事件次数类条件的运算机逻辑
 */
class RuleModel_03_Calculator_Groovy extends TimeRuleCalculator {
    JSONObject ruleDefineParamJsonObject;
    String ruleId;
    RoaringBitmap profileUserBitmap;
    MapState<String, Long> timerState;
    TimerService timerService;
    JSONObject trigEventJsonObject;
    JSONObject checkEventJsonObject;
    long intervalTime;
    int maxMatchCount;
    String checkEventAttribute;

    @Override
    public void setTimeService(MapState<String, Long> timerState, TimerService timerService) {
      this.timerService=timerService;
      this.timerState=timerState;
    }

    @Override
    public List<JSON> onTimer() {
        return null;
    }

    @Override
    public void init(JSONObject ruleDefineParamJsonObject, RoaringBitmap profileUserBitmap) {
        this.ruleDefineParamJsonObject=ruleDefineParamJsonObject;
        this.profileUserBitmap=profileUserBitmap;

        this.trigEventJsonObject = ruleDefineParamJsonObject.getJSONObject("ruleTrigEvent");
        this.checkEventJsonObject = ruleDefineParamJsonObject.getJSONObject("checkEvent");
        this.checkEventAttribute = checkEventJsonObject.getString("eventAttribute");
        this.intervalTime = ruleDefineParamJsonObject.getLong("interval_time");
        this.maxMatchCount = ruleDefineParamJsonObject.getInteger("rule_match_count");
        this.ruleId = ruleDefineParamJsonObject.getString("ruleId");
    }

    @Override
    public List<JSONObject> process(UserEvent userEvent) throws Exception {
        JSONObject resObj = new JSONObject();
        //判断是否是触发事件
        if (UserEventComparator.userEventIsEqualParam(userEvent, trigEventJsonObject)) {
            //计算出要注册的定时器的时间
            long registerTime = timerService.currentProcessingTime() + intervalTime;
            //注册定时器
            timerService.registerProcessingTimeTimer(registerTime);
            //从事件中拿到规则检查条件的事件属性
            String checkEventAttributeValue = userEvent.getProperties().get(this.checkEventAttribute);
            //将定时器注册信息，记录到timerState中
            timerState.put(ruleId+":"+checkEventAttributeValue, registerTime);
            resObj.put("ruleId", ruleId);
            resObj.put("resType","timerReg");
            resObj.put("guid",userEvent.getGuid());
            resObj.put("timestamp",System.currentTimeMillis());
            

        }
        //判断是否是检查事件
        if(UserEventComparator.userEventIsEqualParam(userEvent, checkEventJsonObject)){
            //从事件中拿到规则检查条件的事件属性
            String checkEventAttributeValue = userEvent.getProperties().get(this.checkEventAttribute);
            //从状态中，查找到之前注册的定时器时间
            Long registerTime = timerState.get(ruleId + ":" + checkEventAttributeValue);
            if(registerTime!=null){
                timerService.deleteProcessingTimeTimer(registerTime);
                timerState.remove(ruleId + ":" + checkEventAttributeValue);
            }
        }
        return null;
    }
}
