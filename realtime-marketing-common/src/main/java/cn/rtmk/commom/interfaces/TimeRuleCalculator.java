package cn.rtmk.commom.interfaces;

import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.streaming.api.TimerService;

import java.text.ParseException;
import java.util.List;

/**
 * 需要用到定时器功能规则模型运算机接口
 */
public abstract class TimeRuleCalculator implements RuleCalculator{
    public abstract void setTimeService(MapState<String,Long> timerState,TimerService timerService);
    public abstract List<JSON> onTimer();

    @Override
    public void calc(UserEvent userEvent) throws ParseException {

    }

    @Override
    public boolean isMatch(int guid) {
        return false;
    }
}
