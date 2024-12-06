package cn.rtmk.dao;

import java.sql.SQLException;

public interface RuleSystemMetaDao {
    String getSqlTemplateByTemplateName(String conditionTemplateName) throws SQLException;
}
