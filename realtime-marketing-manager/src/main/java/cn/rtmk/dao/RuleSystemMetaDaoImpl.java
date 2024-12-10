package cn.rtmk.dao;

import org.roaringbitmap.RoaringBitmap;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class RuleSystemMetaDaoImpl implements RuleSystemMetaDao {
    Connection conn;
    public RuleSystemMetaDaoImpl() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://doitedu:3306/rtmk", "root", "root");
    }
    @Override
    public String getSqlTemplateByTemplateName(String conditionTemplateName) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("select template_sql from condition_doris_sql_template where template_name=?");
        preparedStatement.setString(1,conditionTemplateName);
        ResultSet resultSet = preparedStatement.executeQuery();
        String templateSql=null;
        while (resultSet.next()) {
            templateSql=resultSet.getString("template_sql");
        }
        return templateSql;
    }

    @Override
    public String queryGroovyTemplateByModelId(int ruleModelId) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("select caculator_groovy_template from rulemodel_calculator_templates where rule_model_id=? and status=1");
        preparedStatement.setInt(1,ruleModelId);
        ResultSet resultSet = preparedStatement.executeQuery();
        String groovyTemplate=null;
        while (resultSet.next()) {
            groovyTemplate=resultSet.getString("caculator_groovy_template");
        }
        return groovyTemplate;
    }

    @Override
    public boolean insertRuleInfo(
            String rule_id,
            int rule_model_id,
            byte[] rule_profile_user_bitmap,
            String caculator_groovy_code,
            String rule_param_json,
            String creator_name,
            int rule_status,
            Timestamp create_time,
            Timestamp update_time) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement
                ("insert into rule_instance_definition\n" +
                        "(rule_id, rule_model_id, rule_profile_user_bitmap, caculator_groovy_code,\n" +
                        "rule_param_json, creator_name, rule_status, create_time, update_time)\n" +
                        "values (?,?,?,?,?,?,?,?,?);");
        preparedStatement.setString(1,rule_id);
        preparedStatement.setInt(2,rule_model_id);
        preparedStatement.setBytes(3,rule_profile_user_bitmap);
        preparedStatement.setString(4,caculator_groovy_code);
        preparedStatement.setString(5,rule_param_json);
        preparedStatement.setString(6,creator_name);
        preparedStatement.setInt(7,rule_status);
        preparedStatement.setTimestamp(8,create_time);
        preparedStatement.setTimestamp(9,update_time);
        boolean b = preparedStatement.execute();
        return b;
    }
}
