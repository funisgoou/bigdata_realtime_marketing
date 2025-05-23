package cn.rtmk.engine.functions;


import cn.rtmk.commom.interfaces.RuleCalculator;
import cn.rtmk.commom.interfaces.TimeRuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import cn.rtmk.engine.pojo.RuleMetaBean;
import cn.rtmk.engine.utils.FlinkStateDescriptors;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

@Slf4j
public class RuleMatchProcessFunction extends KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject> {

    MapState<String, Long> timeState;

    @Override
    public void open(Configuration parameters) throws Exception {
        timeState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Long>("timerState", String.class, long.class));
    }

    /**
     * 处理用户事件流
     *
     * @param userEvent
     * @throws Exception
     */
    @Override
    public void processElement(UserEvent userEvent, KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject>.ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
        ReadOnlyBroadcastState<String, RuleMetaBean> broadcastState = ctx.getBroadcastState(FlinkStateDescriptors.ruleMetaBeanMapStateDescriptor);
        Iterable<Map.Entry<String, RuleMetaBean>> ruleEntries = broadcastState.immutableEntries();
        //遍历每一个规则，进行相应处理
        for (Map.Entry<String, RuleMetaBean> ruleEntry : ruleEntries) {
            //取出规则的封装对象
            RuleMetaBean ruleMetaBean = ruleEntry.getValue();

            //调用规则的运算机，对输入事件进行处理
            RuleCalculator ruleCalculator = ruleMetaBean.getRuleConditionCaculator();

            //如果该规则的运算机是需要用到定时器功能的
            List<JSONObject> caculatorResponse;
            if (ruleCalculator instanceof TimeRuleCalculator) {
                TimeRuleCalculator timeRuleCalculator = (TimeRuleCalculator) ruleCalculator;
                caculatorResponse = timeRuleCalculator.process(userEvent, timeState, ctx.timerService());
            } else {
                caculatorResponse = ruleCalculator.process(userEvent);
            }
            //输出运算机的响应数据
            for (JSONObject resObject : caculatorResponse) {
                if ("match".equals(resObject.getString("resType"))) {
                    out.collect(resObject);
                } else {
                    ctx.output(new OutputTag<>("ruleStatInfo", TypeInformation.of(JSONObject.class)), resObject);
                }
            }

        }
    }

    @Override
    public void onTimer(long timestamp, KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject>.OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
        for (Map.Entry<String, Long> entry : timeState.entries()) {
            //ruleId+":"+checkEventAttributeValue
            String key = entry.getKey();
            String ruleId = key.split(":")[0];
            Long registerTime = entry.getValue();
            if (registerTime != null && registerTime == timestamp) {
                RuleCalculator ruleCalculator = ctx.getBroadcastState(FlinkStateDescriptors.ruleMetaBeanMapStateDescriptor).get(ruleId).getRuleConditionCaculator();
                if (ruleCalculator instanceof TimeRuleCalculator) {
                    TimeRuleCalculator timeRuleCalculator = (TimeRuleCalculator) ruleCalculator;
                    List<JSONObject> resJsonObject = timeRuleCalculator.onTimer(timestamp, ctx.getCurrentKey(),timeState, ctx.timerService());
                    for (JSONObject resObject : resJsonObject) {
                        if ("match".equals(resObject.getString("resType"))) {
                            out.collect(resObject);
                        } else {
                            ctx.output(new OutputTag<>("ruleStatInfo", TypeInformation.of(JSONObject.class)), resObject);
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理规则元信息流广播流
     * 也就是规则引擎的规则注入模块
     *
     * @param ruleMetaBean
     * @param context
     * @param out
     * @throws Exception
     */
    @Override
    public void processBroadcastElement(RuleMetaBean ruleMetaBean, KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject>.Context context, Collector<JSONObject> out) throws Exception {
        BroadcastState<String, RuleMetaBean> broadcastState = context.getBroadcastState(FlinkStateDescriptors.ruleMetaBeanMapStateDescriptor);
        //根据收到的规则管理的操作类型，去操作广播状态
        try {
            String operateType = ruleMetaBean.getOperateType();
            if ("INSERT".equals(operateType) || "UPDATE".equals(operateType)) {
                //把规则的运算机groovy代码，动态编译加载并反射成具体的运算机对象
                Class aClass = new GroovyClassLoader().parseClass(ruleMetaBean.getCaculatorGroovyCode());
                RuleCalculator ruleConditionCaculator = (RuleCalculator) aClass.newInstance();
                //对规则运算器做初始化
                ruleConditionCaculator.init(JSON.parseObject(ruleMetaBean.getRuleParamJson()), ruleMetaBean.getProfileUserBitmap());

                //如果该规则的运算机是需要用到定时器功能的
//                if (ruleConditionCaculator instanceof TimeRuleCalculator) {
//                    TimeRuleCalculator timeRuleCalculator = (TimeRuleCalculator) ruleConditionCaculator;
//                    timeRuleCalculator.setTimeService(timeState, null);
//                }

                //然后将创建好的运算机对象，填充到ruleMetaBean
                ruleMetaBean.setRuleConditionCaculator(ruleConditionCaculator);
                //再把ruleMetaBean放入广播状态中
                broadcastState.put(ruleMetaBean.getRuleId(), ruleMetaBean);
                log.info("接收到一个规则管理信息，操作类型是:{},所属的规则模型是:{},创建人是：{}", ruleMetaBean.getOperateType(), ruleMetaBean.getRuleModelId(), ruleMetaBean.getCreatorName());
            } else {
                //从广播状态中，删除掉该规则的ruleMetaBean
                broadcastState.remove(ruleMetaBean.getRuleId());
                log.info("接收到一个规则管理信息，操作类型是删除，删除的规则id是:{};", ruleMetaBean.getRuleId());
            }
        } catch (Exception e) {
            log.info("接收到一个规则管理信息，但规则信息构建失败:{}", e.getMessage());
        }


    }
}
