package cn.rtmk.commom.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private int guid;
    private String eventId;
    private Map<String,String> properties;
    private long eventTime;
}
