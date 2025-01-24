package cn.rtmk.rulemodel.template_test;

import cn.rtmk.commom.pojo.ActionSeqParam;
import cn.rtmk.commom.pojo.AttributeParam;
import cn.rtmk.commom.pojo.EventParam;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.util.Arrays;
import java.util.HashMap;




public class Test_EventSeqQueryTemplate {
    public static void main(String[] args) {
        Template template = Engine.use().getTemplate("E:\\coding\\bigdata\\rule_model_resources\\templates\\doris_sql\\action_seq_condition_query.sql.enjoy");
        AttributeParam p11 = new AttributeParam("pageId", "=", "page001");
        AttributeParam p12 = new AttributeParam("itemId", "=", "item003");
        EventParam e1 = new EventParam("e1", Arrays.asList(p11, p12));

        AttributeParam p21 = new AttributeParam("pageId", "=", "page002");
        AttributeParam p22 = new AttributeParam("itemId", "=", "item003");
        EventParam e2 = new EventParam("e2", Arrays.asList(p21, p22));

        AttributeParam p31 = new AttributeParam("itemId", "=", "item005");
        EventParam e3 = new EventParam("e3", Arrays.asList(p31));

        ActionSeqParam seqParam = new ActionSeqParam("2022-08-01 00:00:00", "2024-08-01 00:00:00", 3, "xxx", 2, Arrays.asList(e1, e2, e3));
        HashMap<String,Object> data=new HashMap<>();
        data.put("windowStart",seqParam.getWindowStart());
        data.put("windowEnd",seqParam.getWindowEnd());
        data.put("evnentParams",seqParam.getEventParams());

        String sql = template.renderToString(data);

        System.out.println(sql);


    }
}
