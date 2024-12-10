package cn.rtmk.dao;

import org.roaringbitmap.RoaringBitmap;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface RuleSystemMetaDao {
    String getSqlTemplateByTemplateName(String conditionTemplateName) throws SQLException;

    String queryGroovyTemplateByModelId(int ruleModelId) throws SQLException;

    boolean insertRuleInfo( String rule_id,
                         int rule_model_id,
                         byte[] rule_profile_user_bitmap,
                         String caculator_groovy_code,
                         String rule_param_json,
                         String creator_name,
                         int rule_status,
                         Timestamp create_time,
                         Timestamp update_time) throws SQLException;
}
