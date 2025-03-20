package cn.rtmk.service;

import cn.rtmk.commom.pojo.ActionSeqParam;
import com.alibaba.fastjson.JSONObject;
import org.roaringbitmap.RoaringBitmap;

import java.sql.SQLException;

public interface ActionConditionQueryService {
    void processActionCountCondition(JSONObject eventParamJsonObject, String ruleId, RoaringBitmap bitmap) throws SQLException;

    void processActionSeqCondition(ActionSeqParam actionSeqParam, String ruleId, RoaringBitmap bitmap) throws SQLException;
}
