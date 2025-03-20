package cn.rtmk.controller;

import cn.rtmk.commom.pojo.ActionSeqParam;
import cn.rtmk.commom.pojo.AttributeParam;
import cn.rtmk.commom.pojo.EventParam;
import cn.rtmk.service.ActionConditionQueryService;
import cn.rtmk.service.ProfileConditionQueryService;
import cn.rtmk.service.RuleSystemMetaService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class RuleManagementController { 

    private final ProfileConditionQueryService profileConditionQueryService;


    private final ActionConditionQueryService actionConditionQueryService;

    private final RuleSystemMetaService ruleSystemMetaService;

    public RuleManagementController(ProfileConditionQueryService profileConditionQueryService,
                                    ActionConditionQueryService actionConditionQueryService,
                                    RuleSystemMetaService ruleSystemMetaService) {
        this.profileConditionQueryService = profileConditionQueryService;
        this.actionConditionQueryService = actionConditionQueryService;
        this.ruleSystemMetaService = ruleSystemMetaService;
    }


    //从前端页面接收规则定义的参数json，并发布规则01
    @RequestMapping("/api/publish/addrule/model01")
    public void publishRuleModel01(@RequestBody String ruleDefine) throws IOException, SQLException {
        JSONObject ruleDefineJsonObject = JSON.parseObject(ruleDefine);
        JSONArray profileCondition = ruleDefineJsonObject.getJSONArray("profileCondition");
        String ruleId = ruleDefineJsonObject.getString("ruleId");
        /**
         * 一、 人群画像处理
         */
        System.out.println("------查询画像人群 开始----");
        RoaringBitmap bitmap = profileConditionQueryService.queryProfileUsers(profileCondition);
        System.out.println("圈中人群:\n"+bitmap);

        /**
         * 二、规则的行为条件历史值处理
         */
        //解析出行为次数条件，到doris中去查询条件的历史值,并发布到redis
        JSONObject actionCountConditionJsonObject = ruleDefineJsonObject.getJSONObject("actionCountCondition");
        JSONArray eventParams = actionCountConditionJsonObject.getJSONArray("eventParams");
        //遍历每个事件次数条件，并进行历史数据查询，且顺便发布到redis
        for(int i = 0;i<eventParams.size();i++){
            JSONObject eventParam = eventParams.getJSONObject(i);
            actionConditionQueryService.processActionCountCondition(eventParam,ruleId,bitmap);
        }
        System.out.println("------查询行为次数条件的历史值 结束----\n\n");
        /**
         * 三、 规则的groovy运算代码处理
         */
        String ruleModelGroovyTemplate = ruleSystemMetaService.findRuleModelGroovyTemplate(ruleDefineJsonObject.getInteger("ruleModelId"));
        Template template = Engine.use().getTemplateByString(ruleModelGroovyTemplate);
        //取出规则实例定义参数中的事件次数条件参数
        JSONObject actionCountCondition = ruleDefineJsonObject.getJSONObject("actionCountCondition");
        //从事件条件参数中取出事件条件的个数
        int eventCount = actionCountCondition.getJSONArray("eventParams").size();
        //从事件条件参数中取出事件条件的组合布尔表达式
        String combineExpr = actionCountCondition.getString("combineExpr");
        //放入一个hashmap中，作为模板的参数
        HashMap<String, Object> data = new HashMap<>();
        data.put("eventParams", new int[eventCount]);
        data.put("combineExpr", combineExpr);
        //渲染模板，得到groovy代码
        String groovyCaculatorCode = template.renderToString(data);

        /**
         * 四、正式发布规则
         * 把3类信息，放入规则平台的元数据库
         * 人群bitmap
         * 规则参数(大json)
         * 规则运算的groovy代码
         */
        ruleSystemMetaService.publishRuleInstance(ruleId, ruleDefineJsonObject.getInteger("ruleModelId"),
                bitmap, groovyCaculatorCode, ruleDefine, "hugo", 1);

    }

    //从前端页面接收规则定义的参数json，并发布规则02
    @RequestMapping("/api/publish/addrule/model02")
    public void publishRuleModel02(@RequestBody String ruleDefine) throws IOException, SQLException {
        JSONObject ruleDefineJsonObject = JSON.parseObject(ruleDefine);
        String ruleId = ruleDefineJsonObject.getString("ruleId");
        /**
         * 一、 人群画像处理
         */
        System.out.println("------查询画像人群 开始----");
        JSONArray profileCondition = ruleDefineJsonObject.getJSONArray("profileCondition");
        RoaringBitmap bitmap = profileConditionQueryService.queryProfileUsers(profileCondition);
        System.out.println("圈中人群:\n"+bitmap);

        /**
         * 二、规则的行为条件历史值处理
         */
        //解析出行为次数条件，到doris中去查询条件的历史值,并发布到redis
        JSONObject actionCountConditionJsonObject = ruleDefineJsonObject.getJSONObject("actionCountCondition");//整个规则的参数
        JSONArray eventParams = actionCountConditionJsonObject.getJSONArray("eventParams");  //事件次数条件的参数
        //遍历每个事件次数条件，并进行历史数据查询，且顺便发布到redis
        for(int i = 0;i<eventParams.size();i++){
            JSONObject eventParam = eventParams.getJSONObject(i);
            actionConditionQueryService.processActionCountCondition(eventParam,ruleId,bitmap);
        }
        System.out.println("------查询行为次数条件的历史值 结束----\n\n");

        /**
         * 三、规则的受众行为序列条件历史值处理
         */
        JSONObject actionSeqConditionJSONObject = ruleDefineJsonObject.getJSONObject("actionSeqCondition");
        //json参数转换JavaBean操作
        //取出序列条件中的事件列表
        JSONArray evnetParams = actionSeqConditionJSONObject.getJSONArray("eventParams");
        ArrayList<EventParam> eventParamList = new ArrayList<>();

        //将事件列表中的各事件定义参数，转换成javabean的List
        for(int i=0;i<evnetParams.size();i++){
            JSONObject evnetParamsJSONObject = evnetParams.getJSONObject(i);
            JSONArray attributeParamsJSONArray = evnetParamsJSONObject.getJSONArray("attributeParams");
            ArrayList<AttributeParam> attributeParamList = new ArrayList<>();
            for(int j=0;j<attributeParamsJSONArray.size();j++){
                JSONObject attributeParamJsonObject = attributeParamsJSONArray.getJSONObject(j);
                attributeParamList.add(new AttributeParam(attributeParamJsonObject.getString("attributeName"),
                        attributeParamJsonObject.getString("compareType"),
                        attributeParamJsonObject.getString("compareValue")));
            }
            String eventId = evnetParamsJSONObject.getString("eventId");
            eventParamList.add(new EventParam(eventId, attributeParamList));
        }

        //组装最外层的事件序列条件参数对象
        ActionSeqParam actionSeqParam = new ActionSeqParam(actionSeqConditionJSONObject.getString("windowStart"),
                actionSeqConditionJSONObject.getString("windowEnd"),
                actionSeqConditionJSONObject.getInteger("conditionId"),
                actionSeqConditionJSONObject.getString("dorisQueryTemplate"),
                actionSeqConditionJSONObject.getInteger("seqCount"),
                eventParamList);

        actionConditionQueryService.processActionSeqCondition(actionSeqParam,ruleId,bitmap);
        /**
         * 四、 规则的groovy运算代码处理
         */
        String ruleModelGroovyTemplate = ruleSystemMetaService.findRuleModelGroovyTemplate(ruleDefineJsonObject.getInteger("ruleModelId"));
        Template template = Engine.use().getTemplateByString(ruleModelGroovyTemplate);

        //准备一个渲染groovy运算机模板的数据载体
        HashMap<String, Object> data = new HashMap<>();

        //行为次数类条件的事件定义个数
        data.put("eventParams", new int[eventParams.size()]);
        //行为次数条件的布尔组合表达式
        data.put("cntConditionCombineExpr", actionCountConditionJsonObject.getString("combineExpr"));
        //整个规则的各大条件之间的布尔组合表达式
        data.put("ruleCombineExpr",ruleDefineJsonObject.getString("combineExpr"));
        //渲染模板，得到groovy代码
        String groovyCaculatorCode = template.renderToString(data);

        /**
         * 五、正式发布规则
         * 把3类信息，放入规则平台的元数据库
         * 人群bitmap
         * 规则参数(大json)
         * 规则运算的groovy代码
         */
        ruleSystemMetaService.publishRuleInstance(ruleId, ruleDefineJsonObject.getInteger("ruleModelId"),
                bitmap, groovyCaculatorCode, ruleDefine, "hugo", 1);

    }
}
