package cn.rtmk.test.whole_test.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyParam {
    private String propName;
    private String compareType;
    private String compareValue;
}
