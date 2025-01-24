package cn.rtmk.rulemodel.template_test;

import cn.rtmk.commom.interfaces.RuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import groovy.lang.GroovyClassLoader;
import org.roaringbitmap.RoaringBitmap;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.util.HashMap;

public class Test_Rulemodel_02_caculatorTemplate {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException {
        String ruleDefine="{\n" +
                "  \"ruleModelId\": \"2\",\n" +
                "  \"ruleId\": \"2-rule001\",\n" +
                "  \"ruleTrigEvent\": {\n" +
                "    \"eventId\": \"e5\",\n" +
                "    \"attributeParams\": [\n" +
                "      {\n" +
                "        \"attributeName\": \"pageId\",\n" +
                "        \"compareType\": \"=\",\n" +
                "        \"compareValue\": \"page001\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "    \"windowEnd\": \"2022-08-30 12:00:00\"\n" +
                "  },\n" +
                "  \"profileCondition\": [\n" +
                "    {\n" +
                "      \"tagId\": \"tg01\",\n" +
                "      \"compareType\": \"between\",\n" +
                "      \"compareValue\": \"2,20\"\n" +
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
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page001\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 3,\n" +
                "        \"conditionId\": 1,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e3\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page002\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"attributeName\": \"itemId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"item003\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 1,\n" +
                "        \"conditionId\": 2,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e2\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page001\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "        \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "        \"eventCount\": 2,\n" +
                "        \"conditionId\": 3,\n" +
                "        \"dorisQueryTemplate\": \"action_count\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"combineExpr\": \" res_0 && res_1 && res_2\"\n" +
                "  },\n" +
                "  \"actionSeqCondition\": {\n" +
                "    \"eventParams\": [\n" +
                "      {\n" +
                "        \"eventId\": \"e1\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page001\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e3\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page002\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"attributeName\": \"itemId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"item003\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"eventId\": \"e2\",\n" +
                "        \"attributeParams\": [\n" +
                "          {\n" +
                "            \"attributeName\": \"pageId\",\n" +
                "            \"compareType\": \"=\",\n" +
                "            \"compareValue\": \"page001\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"windowStart\": \"2022-08-01 12:00:00\",\n" +
                "    \"windowEnd\": \"2022-08-30 12:00:00\",\n" +
                "    \"conditionId\": 4,\n" +
                "    \"dorisQueryTemplate\": \"action_seq\",\n" +
                "    \"seqCount\": 2\n" +
                "  },\n" +
                "  \"rule_match_count\": 2,\n" +
                "  \"combineExpr\": \"res_0 && res_1\"\n" +
                "}";

        Template template = Engine.use().getTemplate("E:\\coding\\bigdata\\rule_model_resources\\templates\\rule_calculator\\rulemodel_02_calculator.template");

        HashMap<String, Object> data = new HashMap<>();

        JSONObject ruleDefineJsonObject = JSONObject.parseObject(ruleDefine);
        JSONObject actionCountCondition = ruleDefineJsonObject.getJSONObject("actionCountCondition");
        //事件条件的个数
        JSONArray eventParams = actionCountCondition.getJSONArray("eventParams");
        int eventCount = eventParams.size();
        //事件条件的组合布尔表达式
        String cntConditionCombineExpr = actionCountCondition.getString("combineExpr");
        data.put("eventParams", new int[eventCount]);
        data.put("cntConditionCombineExpr", cntConditionCombineExpr);
        data.put("ruleCombineExpr",ruleDefineJsonObject.getString("combineExpr"));

        String code = template.renderToString(data);

        System.out.println(code);

        System.out.println("---------编译加载代码，进行调用");
        Jedis jedis = new Jedis("doitedu", 6379);

        Class aClass = new GroovyClassLoader().parseClass(code);
        RuleCalculator caculator = (RuleCalculator) aClass.newInstance();

        //先初始化
        caculator.init(jedis, ruleDefineJsonObject, RoaringBitmap.bitmapOf(1,2,3,4,5),null);
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
        UserEvent e1 = new UserEvent(1, "e1", properties, 1660838400000L);

        HashMap<String, String> properties5 = new HashMap<>();
        properties.put("p1","v1");
        properties.put("p2","v3");
        UserEvent e5 = new UserEvent(1, "e5", properties, 1660838400000L);

        long start=System.currentTimeMillis();
        for(int i=0;i<=1;i++){
            caculator.process(e1);
        }

        long end=System.currentTimeMillis();
        //做匹配判断
        System.out.println(caculator.isMatch(1));
        System.out.println(end-start);
    }
}
