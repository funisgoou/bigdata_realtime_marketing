package cn.rulemgmt.dao;

import java.sql.SQLException;

public interface RuleSystemMetaDao {
    String getSqlTemplateByTemplateName(String conditionTemplateName) throws SQLException;
}
