package cn.rulemgmt.pojo;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventBean {
    private int guid;
    private String eventId;
    private Map<String,String> properties;
    private long eventTime;
}
