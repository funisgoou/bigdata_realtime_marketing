package cn.rtmk.service;

import cn.rtmk.commom.pojo.ActionSeqParam;
import cn.rtmk.commom.pojo.AttributeParam;
import cn.rtmk.commom.pojo.EventParam;
import cn.rtmk.dao.DorisQueryDao;
import cn.rtmk.dao.RuleSystemMetaDao;
import cn.rtmk.pojo.ActionAttributeParam;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class ActionConditionQueryServiceImpl implements ActionConditionQueryService {
	@Autowired
	RuleSystemMetaDao ruleSystemMetaDao;
    @Autowired
	DorisQueryDao dorisQueryDao;
    public ActionConditionQueryServiceImpl() throws SQLException {
    }

    @Override
	public void processActionCountCondition(JSONObject eventParamJsonObject, String ruleId, RoaringBitmap bitmap) throws SQLException {
        //从事件次数条件中，取出各条件参数
        String eventId=eventParamJsonObject.getString("eventId");
        String windowStart=eventParamJsonObject.getString("windowStart");
        String windowEnd=eventParamJsonObject.getString("windowEnd");
		String conditionId=eventParamJsonObject.getString("conditionId");
        JSONArray attributeParams =eventParamJsonObject.getJSONArray("attributeParams");
		ArrayList<ActionAttributeParam> attrParamList = new ArrayList<>();
		for(int i=0;i<attributeParams.size();i++){
			JSONObject paramsObject = attributeParams.getJSONObject(i);
			ActionAttributeParam param = new ActionAttributeParam(paramsObject.getString("attributeName"), paramsObject.getString("compareType"), paramsObject.getString("compareValue"));
			attrParamList.add(param);
		}
		//构造模板渲染用的数据封装
		HashMap<String,Object> data=new HashMap<>();
		data.put("eventId", eventId);
		data.put("windowStart",windowStart);
		data.put("windowEnd",windowEnd);
		data.put("attrParamList", attrParamList);
		//调用dao类，来查询规则系统的元数据库中,行为次数条件所对应的doris查询模板
		String sqlTemplateStr = ruleSystemMetaDao.getSqlTemplateByTemplateName(eventParamJsonObject.getString("dorisQueryTemplate"));
		//利用enjoy模板引擎，根据条件的具体参数，动态拼接真正的查询sql
		Template template = Engine.use().getTemplateByString(sqlTemplateStr);
		String sql = template.renderToString(data);
		//调用doris查询dao，查询出结果
		dorisQueryDao.queryActionCount(sql, ruleId, conditionId+"",bitmap);
    }

	@Override
	public void processActionSeqCondition(ActionSeqParam actionSeqParam, String ruleId, RoaringBitmap bitmap) throws SQLException {


		HashMap<Object, Object> data = new HashMap<>();

		//将需要的数据放入enjoy引擎的渲染数据载体中
		data.put("windowStart",actionSeqParam.getWindowStart());
		data.put("windowEnd",actionSeqParam.getWindowEnd());
		data.put("evnentParams",actionSeqParam.getEventParams());

		//利用enjoy模板引擎，根据条件的具体参数，动态拼接真正的查询sql
		//调用dao类，来查询规则系统的元数据库中,行为次数条件所对应的doris查询模板
		String sqlTemplateStr = ruleSystemMetaDao.getSqlTemplateByTemplateName(actionSeqParam.getDorisQueryTemplate());
		Template template = Engine.use().getTemplateByString(sqlTemplateStr);
		String sql = template.renderToString(data);
		//调用doris查询dao，查询出结果
		dorisQueryDao.queryActionSeq(sql, ruleId, actionSeqParam,bitmap);
	}
	public static void main(String[] args) throws SQLException {
		ActionConditionQueryService service=new ActionConditionQueryServiceImpl();
		String conditionJson="     {\n" +
				"     \t\"eventId\":\"Share\",\n" +
				"     \t\"attributeParams\":[\n" +
				"     \t\t        {\n" +
				"     \t\t\t\"attributeName\":\"PageId\",\n" +
				"     \t\t\t\"compareType\":\"eq\",\n" +
				"     \t\t\t\"compareValue\":\"page001\"\n" +
				"            },\n" +
				"            {\n" +
				"     \t\t\t\"attributeName\":\"itemId\",\n" +
				"     \t\t\t\"compareType\":\"eq\",\n" +
				"     \t\t\t\"compareValue\":\"item002\"\n" +
				"            },\n" +
				"     \t],\n" +
				"     \t\"windowStart\":\"2024-11-22 12:00:00\",\n" +
				"     \t\"windowEnd\":\"2024-11-23 12:00:00\",\n" +
				"     \t\"eventCount\":3,\n" +
				"     \t\"ConditionId\":1,\n" +
				"\t    \"dorisQueryTemplate\":\"action_count\"\n" +
				"     }";
	}
}
