package cn.rtmk.engine.functions;


import cn.rtmk.commom.interfaces.RuleCalculator;
import cn.rtmk.commom.pojo.UserEvent;
import cn.rtmk.commom.utils.UserEventComparator;
import cn.rtmk.engine.pojo.RuleMatchResult;
import cn.rtmk.engine.pojo.RuleMetaBean;

import cn.rtmk.engine.utils.FlinkStateDescriptors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.roaringbitmap.RoaringBitmap;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Slf4j
public class RuleMatchProcessFunctionOld extends KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject> {
    private Jedis jedis;

    @Override
    public void open(Configuration parameters) throws Exception {
        jedis = new Jedis("doitedu", 6379);
        super.open(parameters);
    }

    /**
     * 处理用户事件流
     *
     * @param userEvent
     * @param readOnlyContext
     * @param collector
     * @throws Exception
     */
    @Override
    public void processElement(UserEvent userEvent, KeyedBroadcastProcessFunction<Integer, UserEvent, RuleMetaBean, JSONObject>.ReadOnlyContext readOnlyContext, Collector<JSONObject> collector) throws Exception {
        ReadOnlyBroadcastState<String, RuleMetaBean> broadcastState = readOnlyContext.getBroadcastState(FlinkStateDescriptors.ruleMetaBeanMapStateDescriptor);
        Iterable<Map.Entry<String, RuleMetaBean>> ruleEntries = broadcastState.immutableEntries();
        //遍历每一个规则，进行相应处理
        for (Map.Entry<String, RuleMetaBean> ruleEntry : ruleEntries) {
            //取出规则的封装对象
            RuleMetaBean ruleMetaBean = ruleEntry.getValue();
            //取出规则的画像人群bitmap
            RoaringBitmap profileUserBitmap = ruleMetaBean.getProfileUserBitmap();

            //判断本事件的行为人，是否属于本规则的画像人群
            if (profileUserBitmap.contains(userEvent.getGuid())) {

                //取出规则的参数Json
                JSONObject ruleParamJsonObj = JSON.parseObject(ruleMetaBean.getRuleParamJson());
                //取出规则的触发事件条件参数Json
                JSONObject ruleTrigEvent = ruleParamJsonObj.getJSONObject("ruleTrigEvent");
                //判断用户行为事件，是否是本规则的触发事件，则进行规则的匹配判断
                if (UserEventComparator.userEventIsEqualParam(userEvent, ruleTrigEvent)) {
                    //如果是触发事件，则判断本行为人是否满足了本规则的所有条件
                    boolean isMatch = ruleMetaBean.getRuleConditionCaculator().isMatch(userEvent.getGuid());
                    log.info("用户:{},触发事件{},规则:{},规则匹配结果:{}",userEvent.getGuid(),userEvent.getEventId(),ruleEntry.getKey(),isMatch);
                    //如果已满足，则输出本规则的触达信息
                    if (isMatch) {
                        RuleMatchResult res = new RuleMatchResult(userEvent.getGuid(), ruleEntry.getKey(), System.currentTimeMillis());
                        collector.collect(JSON.parseObject(JSON.toJSONString(res)));
                    }
                }
                //判断用户行为事件，如果本事件不是规则的触发事件，则进行规则的条件统计运算
                else {
                    //做规则运算
                    ruleMetaBean.getRuleConditionCaculator().calc(userEvent);
                    log.info("收到用户:{},行为事件:{}，处理规则:{}", userEvent.getGuid(), userEvent.getEventId(), ruleEntry.getKey());
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
            if ("INSERT".equals(operateType) || "UPDATE".equals(operateType))  {
                //把规则的运算机groovy代码，动态编译加载并反射成具体的运算机对象
                Class aClass = new GroovyClassLoader().parseClass(ruleMetaBean.getCaculatorGroovyCode());
                RuleCalculator ruleConditionCaculator = (RuleCalculator) aClass.newInstance();
                //对规则运算器做初始化
                ruleConditionCaculator.init(jedis, JSON.parseObject(ruleMetaBean.getRuleParamJson()),ruleMetaBean.getProfileUserBitmap(),out);
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
