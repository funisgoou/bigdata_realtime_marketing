package cn.rtmk.dao;

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
}
