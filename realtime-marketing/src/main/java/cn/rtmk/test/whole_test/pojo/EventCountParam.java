package cn.rtmk.test.whole_test.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class EventCountParam {
    private String eventId;
    private String count;
    private List<PropertyParam> propertyParams;
    private String windowStart;
    private String windowEnd;
    private String paramId;
    public String getParamId(){
        if(this.paramId==null){
            this.paramId=toString().hashCode()+"";
        }
        return this.paramId;
    }
}
