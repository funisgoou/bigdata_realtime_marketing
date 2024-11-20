package cn.rtmk.test.whole_test.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.roaringbitmap.RoaringBitmap;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleInfo {
    //1.规则id
    private String ruleId;
    //2.人群圈选条件
    private List<PropertyParam> profileCondition;
    //3.事件次数
    private List<EventCountParam> eventCondition;
    //4.人群圈选Bitmap
    private RoaringBitmap profileUsersBitmap;
    //5.规则条件运算groovy代码
    private String ruleCaculatorCode;
    //6.触发条件
    private EventCountParam triggerEventCondition;
} 
