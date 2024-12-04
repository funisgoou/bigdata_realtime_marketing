package cn.rulemgmt.controller;

import cn.rulemgmt.service.ActionConditionQueryService;
import cn.rulemgmt.service.ProfileConditionQueryService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class RuleManagementController { 
    @Autowired
    ProfileConditionQueryService profileConditionQueryService;
    
    @Autowired
    ActionConditionQueryService actionConditionQueryService;
//    public static void main(String[] args) throws IOException {
//        UserProfileQueryController controller = new UserProfileQueryController();
//        String webFrontJson="      {\"ruleId\":\"rule01\",\n" +
//                "\"profileCondition\":[{\"tagId\":\"tg01\",\"compareType\":\"gt\",\"compareValue\":\"2\"},{\"tagId\":\"tg04\",\"compareType\":\"match\",\"compareValue\":\"股权\"}]\n" +
//                "}";
//        controller.publishRule(webFrontJson);
//    }

    /**
     * {"ruleId":"rule01",
     *  "profileCondition":[{"tagId":"tg01","compareType":"eq","compareValue":"3"},{"tagId":"tg04","compareType":"match","compareValue":"运动"}]
     * }
     * 
     * 
     */
    //从前端页面接收规则定义的参数json，并发布规则
    @RequestMapping("/api/publish/addrule")
    public void publishRule(@RequestBody String ruleDefine) throws IOException, SQLException {
        JSONObject ruleDefineJsonObject = JSON.parseObject(ruleDefine);
        JSONArray profileCondition = ruleDefineJsonObject.getJSONArray("profileCondition");
        String ruleId = ruleDefineJsonObject.getString("ruleId");
        /**
         * 一、 人群画像处理
         */
        System.out.println("------查询画像人群 开始----");
        RoaringBitmap bitmap = profileConditionQueryService.queryProfileUsers(profileCondition);
        System.out.println(bitmap.contains(3));
        System.out.println(bitmap.contains(5));
        System.out.println("------查询画像人群 完成----\n\n");

        /**
         * 二、规则的行为条件历史值处理
         */
        //解析出行为次数条件，到doris中去查询条件的历史值,并发布到redis
        System.out.println("------查询行为次数条件的历史值 开始----\n\n");
        JSONObject actionCountConditionJsonObject = ruleDefineJsonObject.getJSONObject("actionCountCondition");
        JSONArray eventParams = actionCountConditionJsonObject.getJSONArray("eventParams");
        //遍历每个事件次数条件，并进行历史数据查询，且顺便发布到redis
        for(int i = 0;i<eventParams.size();i++){
            JSONObject eventParam = eventParams.getJSONObject(i);
            actionConditionQueryService.queryActionCount(eventParam,ruleId,bitmap);
        }
        System.out.println("------查询行为次数条件的历史值 结束----\n\n");
        /**
         * 三、 规则的groovy运算代码处理
         */

    }
}
