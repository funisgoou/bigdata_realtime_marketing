package cn.rulemgmt.dao;

import org.roaringbitmap.RoaringBitmap;

import java.sql.SQLException;

public interface DorisQueryDao {
    void queryActionCount(String sql, String ruleId, String conditionId, RoaringBitmap bitmap) throws SQLException;
}
