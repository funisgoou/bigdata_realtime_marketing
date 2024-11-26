import cn.rulemgmt.pojo.ActionAttributeParam;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class EnjoyHelloWorld {
    public static void main(String[] args) {
        //demo1
//        Template template = Engine.use().getTemplateByString("i am #(name)");
//        Kv data = Kv.by("name", "taoge");
//        String s = template.renderToString(data);
//        System.out.println(s);
        
        //demo2
//        Template template = Engine.use().getTemplateByString("i am #if(name.equals(\"taoge\"))#(name)本尊 #else #(name) 小可爱 #end");
//        Kv data = Kv.by("name", "yaomei");
//        String s = template.renderToString(data);
//        System.out.println(s);
        
        //demo3
//        Template template = Engine.use().getTemplateByString("我的学生有:\n"+
//                "#for(stu : students)\n"+
//                "    学生#(for.count):#(stu)\n"+
//                "#end");
//        Kv data = Kv.by("students", Arrays.asList("yaomei","xiaogang","laioo","hugo"));
//        String s = template.renderToString(data);
//        System.out.println(s);
        
        //demo4
        String sqlTemplateStr="select\n" +
                "guid,\n" +
                "count(1) as cnt\n" +
                "from mall_app_events_detail\n" +
                "where 1=1\n" +
                "#if(windowStart != null)\n" +
                "and event_time>='#(windowStart)'\n" +
                "#end\n" +
                "#if(windowEnd != null)\n" +
                "and event_time<='#(windowEnd)'\n" +
                "#end\n" +
                "#if(eventId != null)\n" +
                "and event_id = '#(eventId)'\n" +
                "#end\n" +
                "#for(attrParam:attrParamList)\n" +
                "and get_json_string(propJson,'$.#(attrParam.attributeName)') #(attrParam.compareType)'#(attrParam.compareValue)'\n" +
                "#end\n" +
                "group by guid";
        Template template = Engine.use().getTemplateByString(sqlTemplateStr);
        //        Kv data = Kv.by("name", "yaomei");
//        String s = template.renderToString(data);
        ActionAttributeParam p1 = new ActionAttributeParam("pageId", "=", "page002");
//        ActionAttributeParam p2 = new ActionAttributeParam("itemId", "=", "item002");
//        ActionAttributeParam p3 = new ActionAttributeParam("attr2", ">", 3);
        HashMap<String,Object> data=new HashMap<>();
        data.put("eventId", "e1");
        data.put("windowStart","2022-08-01 00:00:00");
        data.put("windowEnd","2022-08-31 12:00:00");
        data.put("attrParamList", Arrays.asList(p1));
        String s = template.renderToString(data);
      System.out.println(s);
    }
}
