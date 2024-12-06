package cn.rulemgmt.groovy;

import cn.rulemgmt.pojo.EventBean;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * 事件次数类条件的运算机逻辑
 */
public class EventCountConditionCaculator implements ConditionCaculator {
    private Jedis jedis;
    private JSONObject ruleDefineParamJsonObject;
    private JSONObject eventCountConditionParam;
    private String ruleId;

    @Override
    public void init(Jedis jedis, JSONObject ruleDefineParamJsonObject){
        this.jedis = jedis;
        this.ruleDefineParamJsonObject = ruleDefineParamJsonObject;
        //获取规则id
        ruleId = ruleDefineParamJsonObject.getString("ruleId");
        //获取事件次数条件
        this.eventCountConditionParam = ruleDefineParamJsonObject.getJSONObject("actionCountCondition");
    }
    @Override
    public void calc(EventBean eventBean){
        JSONArray eventParams = eventCountConditionParam.getJSONArray("eventParams");

        for (int i = 0; i < eventParams.size(); i++){
            JSONObject eventParam = eventParams.getJSONObject(i);
            //判断当前输入的事件，是否满足该事件条件的参数要求
            if (eventBean.getEventId().equals(eventParam.getString("eventId"))){
                //判断当前输入事件的事件属性是否等于条件中要求的事件属性
                JSONArray attributeParams = eventParam.getJSONArray("attributeParams");
                //对每一个属性条件进行判断
                for (int j = 0; j < attributeParams.size(); j++){
                    JSONObject attributeParam = attributeParams.getJSONObject(j);
                    String attributeName = attributeParam.getString("attributeName");
                    String compareType = attributeParam.getString("compareType");
                    String compareValue = attributeParam.getString("compareValue");
                    String eventAttributeValue=eventBean.getProperties().get(attributeName);
                    if("=".equals(compareType)&& !(compareValue.compareTo(eventAttributeValue)==0)){
                        break;
                    }
                    if (">".equals(compareType)&& !(compareValue.compareTo(eventAttributeValue)>0)){
                        break;
                    }
                    if ("<".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)<0)){
                        break;
                    }
                    if (">=".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)>=0)){
                        break;
                    }
                    if ("<=".equals(compareType)&&!(compareValue.compareTo(eventAttributeValue)<=0)){
                        break;
                    }

                }
                //如果代码到了这里，说明当前输入的事件id和属性都满足了该事件条件的所有属性条件
                //那么就需要去redis中，给这个用户的，这个规则的，这个条件的次数+1

                }
            }
        }


    /**
     * 判断某用户是否满足了该条件
     * @param guid
     * @return
     */
    @Override
    public boolean isMatch(int guid){
        return false;
    }
}
