package cn.rtmk.test.whole_test.publisher;

import cn.rtmk.test.whole_test.pojo.EventCountParam;
import cn.rtmk.test.whole_test.pojo.PropertyParam;
import cn.rtmk.test.whole_test.pojo.RuleInfo;

import java.util.Arrays;
import java.util.Collections;

public class SimplePublishMoni {
    public static void main(String[] args) throws Exception
    {
        /**
         * 假设从前端接受到了营销人员的定义信息
         */
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setRuleId("rule_001");
        
        //前端传入的触发条件
        EventCountParam triggerEventCondition = new EventCountParam();
        triggerEventCondition.setEventId("submitOrder");
        ruleInfo.setTriggerEventCondition(triggerEventCondition);

        //前端传入的参数条件
        PropertyParam tagParam1 = new PropertyParam("tag01", "eq", "C");
        PropertyParam tagParam2 = new PropertyParam("tag03", "lt", "5");
        ruleInfo.setProfileCondition(Arrays.asList(tagParam1, tagParam2));
        
        //前端传入的事件次数条件
        EventCountParam eventCountParam = new EventCountParam();
        eventCountParam.setCount("3");
        eventCountParam.setEventId("addcart");
        PropertyParam propParam = new PropertyParam("itemId", "eq", "123");
        eventCountParam.setPropertyParams( Collections.singletonList(propParam));
        eventCountParam.setWindowStart("2024-11-20 00:00:00");
        eventCountParam.setWindowEnd("2024-11-21 00:00:00");
        eventCountParam.setParamId("1");
        ruleInfo.setEventCondition(Collections.singletonList(eventCountParam));

        /**
         * 2.根据前端传入的画像条件，去es中圈选人群，并生成bitmap,并填充到ruleInfo对象中
         */

        /**
         * 3.找到本规则模板对应的groovy运算模型，填充到ruleInfo对象中
         */

        /**
         * 4.根据前端传入的规则的事件行为次数受众条件，去doris中查询统计所有用户的结果,并将结果按groovy模板中的数据结构要求，写入redis
         */


    }
}
