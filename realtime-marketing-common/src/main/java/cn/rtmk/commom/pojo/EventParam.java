package cn.rtmk.commom.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * [
 *  *       {
 *  *         "eventId": "e1",
 *  *         "attributeParams": [
 *  *           {
 *  *             "attributeName": "pageId",
 *  *             "compareType": "=",
 *  *             "compareValue": "page001"
 *  *           }
 *  *         ]
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventParam {
    private String eventId;
    private List<AttributeParam> attributeParams;
}
