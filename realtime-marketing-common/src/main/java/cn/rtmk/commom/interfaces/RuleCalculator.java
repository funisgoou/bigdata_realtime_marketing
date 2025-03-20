package cn.rtmk.commom.interfaces;


import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONObject;
import org.roaringbitmap.RoaringBitmap;

import java.text.ParseException;
import java.util.List;

/**
 * 规则运算机的统一接口
 */
public interface RuleCalculator {
    /**
     * 运算机初始化
     * @param ruleDefineParamJsonObject 规则参数Json对象
     * @param profileUserBitmap  人群画像bitmap
     */

    void init(JSONObject ruleDefineParamJsonObject, RoaringBitmap profileUserBitmap);

    /**
     * 对输入事件进行规则处理的入口方法
     * @param userEvent  输入的用户行为事件
     */
    List<JSONObject> process(UserEvent userEvent) throws Exception;

    /**
     * 规则条件运算
     * @param userEvent  用户事件
     */
    void calc(UserEvent userEvent) throws ParseException;

    /**
     * 规则条件是否满足的判断逻辑
     * @param guid 用户标识
     * @return 是否满足
     */
    boolean isMatch(int guid);
}
