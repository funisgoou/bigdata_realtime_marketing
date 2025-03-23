package cn.rtmk.service;

import cn.rtmk.dao.RuleSystemMetaDao;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

@Service
public class RuleSystemMetaServiceImpl implements RuleSystemMetaService {
    RuleSystemMetaDao ruleSystemMetaDao;

    @Autowired
    public RuleSystemMetaServiceImpl(RuleSystemMetaDao ruleSystemMetaDao){
        this.ruleSystemMetaDao = ruleSystemMetaDao;
    }

    /**
     * 根据规则模型id，查询该模型对应的groovy运算代码模板
     * @param ruleModelId
     * @return
     * @throws SQLException
     */
    @Override
    public String findRuleModelGroovyTemplate(int ruleModelId) throws SQLException {
        String template=ruleSystemMetaDao.queryGroovyTemplateByModelId(ruleModelId);
        return template;
    }

    /**
     * 发布规则到元数据库中
     * @param rule_id
     * @param rule_model_id
     * @param profileUsersBitmap
     * @param caculator_groovy_code
     * @param rule_param_json
     * @param creator_name
     * @param rule_status
     * @throws IOException
     */
    public void publishRuleInstance(String rule_id,
                                    int rule_model_id,
                                    RoaringBitmap profileUsersBitmap,
                                    String caculator_groovy_code,
                                    String rule_param_json,
                                    String creator_name,
                                    int rule_status) throws IOException, SQLException {
        byte[] bitmapBytes = null;
        if (profileUsersBitmap != null) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream dao = new DataOutputStream(bao);
            profileUsersBitmap.serialize(dao);
            bitmapBytes = bao.toByteArray();
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ruleSystemMetaDao.insertRuleInfo(rule_id,rule_model_id,
                bitmapBytes,caculator_groovy_code,rule_param_json,creator_name,rule_status,
                timestamp,timestamp);

    }
}
