package cn.rtmk.service;

import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.sql.SQLException;

public interface RuleSystemMetaService {
    String findRuleModelGroovyTemplate(int ruleModelId) throws SQLException;
    void publishRuleInstance(String rule_id,
                                    int rule_model_id,
                                    RoaringBitmap profileUsersBitmap,
                                    String caculator_groovy_code,
                                    String rule_param_json,
                                    String creator_name,
                                    int rule_status) throws IOException, SQLException;
}
