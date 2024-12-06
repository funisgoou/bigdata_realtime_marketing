package cn.rtmk.rulemodel.groovy;

import cn.rtmk.commom.interfaces.RuleConditionCaculator;
import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * 事件次数类条件的运算机逻辑
 */
public class RuleModel01Caculator implements RuleConditionCaculator {
    private Jedis jedis;
    private JSONObject ruleDefineParamJsonObject;
    private JSONObject eventCountConditionParam;
    private String ruleId;
    private JSONArray eventParams;

    /**
     * 规则运算机的初始化方法
     * @param jedis
     * @param ruleDefineParamJsonObject
     */
    @Override
    public void init(Jedis jedis, JSONObject ruleDefineParamJsonObject){
        this.jedis = jedis;
        this.ruleDefineParamJsonObject = ruleDefineParamJsonObject;
        //获取规则id
        ruleId = ruleDefineParamJsonObject.getString("ruleId");
        //获取事件次数条件
        this.eventCountConditionParam = ruleDefineParamJsonObject.getJSONObject("actionCountCondition");
        //获取事件条件参数
        eventParams=eventCountConditionParam.getJSONArray("eventParams");
    }

    /**
     * 规则条件的实时运算
     * @param eventBean 输入的一次用户行为
     */
    @Override
    public void calc(UserEvent eventBean){
        for (int i = 0; i < eventParams.size(); i++){
            JSONObject eventParam = eventParams.getJSONObject(i);
            Integer conditionId = eventParam.getInteger("conditionId");
            //判断当前输入的事件，是否满足该事件条件的参数要求
            if (eventBean.getEventId().equals(eventParam.getString("eventId"))){
                //判断当前输入事件的事件属性是否等于条件中要求的事件属性
                JSONArray attributeParams = eventParam.getJSONArray("attributeParams");
                //对每一个属性条件进行判断
                boolean b = judgeEventAttribute(eventBean, attributeParams);

                //如果代码到了这里，说明当前输入的事件id和属性都满足了该事件条件的所有属性条件
                //那么就需要去redis中，给这个用户的，这个规则的，这个条件的次数+1
                if(b){
                    jedis.hincrBy(ruleId+":"+conditionId,eventBean.getGuid()+"",1);
                }
            }
        }
    }

    /**
     * 判断当前输入的事件，是否满足该事件条件的所有属性条件
     * @param eventBean
     * @param attributeParams
     * @return
     */
    private boolean judgeEventAttribute(UserEvent eventBean, JSONArray attributeParams) {
        for (int j = 0; j < attributeParams.size(); j++){
            JSONObject attributeParam = attributeParams.getJSONObject(j);
            String attributeName = attributeParam.getString("attributeName");
            String compareType = attributeParam.getString("compareType");
            String compareValue = attributeParam.getString("compareValue");
            String eventAttributeValue= eventBean.getProperties().get(attributeName);
            if("=".equals(compareType)&& !(compareValue.compareTo(eventAttributeValue)==0)){
                return false;
            }
            if (">".equals(compareType)&& !(compareValue.compareTo(eventAttributeValue)>0)){
                return false;
            }
            if ("<".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)<0)){
                return false;
            }
            if (">=".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)>=0)){
                return false;
            }
            if ("<=".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)<=0)){
                return false;
            }
        }
        return true;
    }


    /**
     * 判断某用户是否满足了该条件 res0 && (res1 || res2)
     * @param guid
     * @return
     */
    @Override
    public boolean isMatch(int guid){
        //取出第n个条件参数
        JSONObject eventParam_0 = eventParams.getJSONObject(0);
        //取出该条件的id
        Integer conditionId_0 = eventParam_0.getInteger("conditionId");
        //取出该事件要求发生的次数
        Integer eventCountParam_0 = eventParam_0.getInteger("eventCount");
        //去redis中查询该用户，该条件的实际发生次数
        String realCountStr_0 = jedis.hget(ruleId + ":" + conditionId_0, guid + "");
        int realCount_0 = Integer.parseInt(realCountStr_0==null?"0":realCountStr_0);
        //判断该用户，该条件的实际发生次数是否满足要求
        boolean res_0 = realCount_0>=eventCountParam_0;
        //取出第n个条件参数
        JSONObject eventParam_1 = eventParams.getJSONObject(1);
        //取出该条件的id
        Integer conditionId_1 = eventParam_1.getInteger("conditionId");
        //取出该事件要求发生的次数
        Integer eventCountParam_1 = eventParam_1.getInteger("eventCount");
        //去redis中查询该用户，该条件的实际发生次数
        String realCountStr_1 = jedis.hget(ruleId + ":" + conditionId_1, guid + "");
        int realCount_1 = Integer.parseInt(realCountStr_1==null?"0":realCountStr_1);
        //判断该用户，该条件的实际发生次数是否满足要求
        boolean res_1 = realCount_1>=eventCountParam_1;
        //取出第n个条件参数
        JSONObject eventParam_2 = eventParams.getJSONObject(2);
        //取出该条件的id
        Integer conditionId_2 = eventParam_2.getInteger("conditionId");
        //取出该事件要求发生的次数
        Integer eventCountParam_2 = eventParam_2.getInteger("eventCount");
        //去redis中查询该用户，该条件的实际发生次数
        String realCountStr_2 = jedis.hget(ruleId + ":" + conditionId_2, guid + "");
        int realCount_2 = Integer.parseInt(realCountStr_2==null?"0":realCountStr_2);
        //判断该用户，该条件的实际发生次数是否满足要求
        boolean res_2 = realCount_2>=eventCountParam_2;
        return res_0 && res_1 && res_2;
    }
}