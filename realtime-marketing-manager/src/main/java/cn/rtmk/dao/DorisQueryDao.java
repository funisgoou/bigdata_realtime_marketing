package cn.rtmk.dao;

import cn.rtmk.commom.pojo.ActionSeqParam;
import org.roaringbitmap.RoaringBitmap;

import java.sql.SQLException;

public interface DorisQueryDao {
    void queryActionCount(String sql, String ruleId, String conditionId, RoaringBitmap bitmap) throws SQLException;

    void queryActionSeq(String sql, String ruleId, ActionSeqParam actionSeqParam, RoaringBitmap bitmap) throws SQLException;
}
