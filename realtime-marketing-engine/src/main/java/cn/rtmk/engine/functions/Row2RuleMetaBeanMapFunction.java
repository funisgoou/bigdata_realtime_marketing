package cn.rtmk.engine.functions;

import cn.rtmk.engine.pojo.RuleMetaBean;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * id                       int auto_increment
 * primary key,
 * rule_id                  varchar(50)  null,
 * rule_model_id            int          null,
 * rule_profile_user_bitmap longblob     null,
 * caculator_groovy_code    text         null,
 * rule_param_json          text         null,
 * creator_name             varchar(255) null,
 * rule_status              int          null,
 * create_time              datetime     null,
 * update_time              datetime     null
 */
public class Row2RuleMetaBeanMapFunction implements MapFunction<Row, RuleMetaBean> {
    @Override
    public RuleMetaBean map(Row row) throws Exception {
        RuleMetaBean ruleMetaBean = new RuleMetaBean();
        if (row.getKind() == RowKind.INSERT) {
            ruleMetaBean.setOperateType("INSERT");
            setRuleMetaBeanAttributes(ruleMetaBean, row);
        } else if (row.getKind() == RowKind.UPDATE_AFTER) {
            ruleMetaBean.setOperateType("UPDATE");
            setRuleMetaBeanAttributes(ruleMetaBean, row);
        } else if (row.getKind() == RowKind.DELETE) {
            ruleMetaBean.setOperateType("DELETE");
            String ruleId = row.getFieldAs("rule_id");
            ruleMetaBean.setRuleId(ruleId);
        } else {
            return null;
        }
        return ruleMetaBean;
    }

    public void setRuleMetaBeanAttributes(RuleMetaBean ruleMetaBean, Row row) throws IOException {
        String ruleId = row.getFieldAs("rule_id");
        int ruleModelId = row.getFieldAs("rule_model_id");
        byte[] bitmapBytes = row.getFieldAs("rule_profile_user_bitmap");
        RoaringBitmap bitmap = RoaringBitmap.bitmapOf();
        bitmap.deserialize(ByteBuffer.wrap(bitmapBytes));

        String caculatorGroovyCode = row.getFieldAs("caculator_groovy_code");
        String ruleParamJson = row.getFieldAs("rule_param_json");
        String creatorName = row.getFieldAs("creator_name");
        int ruleStatus = row.getFieldAs("rule_status");

        ruleMetaBean.setRuleId(ruleId);
        ruleMetaBean.setRuleModelId(ruleModelId);
        ruleMetaBean.setProfileUserBitmap(bitmap);
        ruleMetaBean.setCaculatorGroovyCode(caculatorGroovyCode);
        ruleMetaBean.setRuleParamJson(ruleParamJson);
        ruleMetaBean.setCreatorName(creatorName);
        ruleMetaBean.setRuleStatus(ruleStatus);
    }
}
