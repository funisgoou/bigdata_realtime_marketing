package cn.rtmk.engine.utils;

import cn.rtmk.engine.pojo.RuleMetaBean;
import org.apache.flink.api.common.state.MapStateDescriptor;

public class FlinkStateDescriptors {
    public static MapStateDescriptor<String, RuleMetaBean> ruleMetaBeanMapStateDescriptor = new MapStateDescriptor<>("rule-meta-state", String.class, RuleMetaBean.class);
}
