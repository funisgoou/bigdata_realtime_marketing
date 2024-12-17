package cn.rtmk.rulemodel.template_test;

import cn.rtmk.commom.interfaces.RuleConditionCaculator;
import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import groovy.lang.GroovyClassLoader;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.HashMap;

public class Test_Rulemodel_01_caculatorTemplate {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        String ruleDefine="{\n" +
                "  \"ruleModelId\": \"1\",\n" +
                "  \"ruleId\": \"rule001\",\n" +
                "  \"profileCondition\": [\n" +
                "    {\n" +
                "      \"tagId\": \"tg01\",\n" +
                "      \"compareType\": \"gt\",\n" +
                "      \"compareValue\": \"3\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"tagId\": \"tg04\",\n" +
                "      \"compareType\": \"match\",\n" +
                "      \"compareValue\": \"汽车\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"actionCountCondition\": {\n" +
                "    \"eventParams\": [\n" +
                "      {\n" +
                "        \"eventId\": \"e1\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"p1\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"v1\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 3,\n" +
                "        \"conditionId\": 1,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e2\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"p1\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"v2\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"attributeName\": \"p2\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"v3\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 1,\n" +
                "        \"conditionId\": 2,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e3\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"p1\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"v1\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 2,\n" +
                "        \"conditionId\": 3,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"combineExpr\": \" res0 && (res1 || res2) \"\n" +
                "  }\n" +
                "}";

        Template template = Engine.use().getTemplate("E:\\coding\\bigdata\\rule_model_resources\\caculator_groovy_templates\\rulemodel_01_caculator.template");

        HashMap<String, Object> data = new HashMap<>();

        JSONObject ruleDefineJsonObject = JSONObject.parseObject(ruleDefine);
        JSONArray eventParams = ruleDefineJsonObject.getJSONObject("actionCountCondition").getJSONArray("eventParams");
        int eventCount = eventParams.size();
        data.put("eventParams", new int[eventCount]);
        data.put("combineExpr", "res_0 && (res_1 || res_2)");

        String code = template.renderToString(data);

        System.out.println(code);

        System.out.println("---------编译加载代码，进行调用");

        Jedis jedis = new Jedis("doitedu", 6379);
        Class aClass = new GroovyClassLoader().parseClass(code);
        RuleConditionCaculator caculator = (RuleConditionCaculator) aClass.newInstance();

        //先初始化
        caculator.init(jedis, ruleDefineJsonObject);
        //做运算
        /**
         * e1 p1=v1 ,>=3
         * e2 p1=v2,p2=v3,>=1
         * e3 p1=v1,>=2
         *
         * res0 && (res1 || res2)
         */
        HashMap<String, String> properties = new HashMap<>();
        properties.put("p1","v1");
        properties.put("p2","v3");
        UserEvent e1 = new UserEvent(1, "e1", properties, 1000000);
        long start=System.currentTimeMillis();
        for(int i=0;i<=1000;i++){
            caculator.calc(e1);
        }

        long end=System.currentTimeMillis();
        //做匹配判断
        System.out.println(caculator.isMatch(1));
        System.out.println(end-start);
    }
}
