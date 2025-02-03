package cn.rtmk.commom.utils;

import cn.rtmk.commom.pojo.UserEvent;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class UserEventComparator {
    public static boolean userEventIsEqualParam(UserEvent userEvent, JSONObject eventParam) {
        String eventId = eventParam.getString("eventId");
        JSONArray attributeParams = eventParam.getJSONArray("attributeParams");

        if (eventId.equals(userEvent.getEventId())) {
            for (int j = 0; j < attributeParams.size(); j++) {
                JSONObject attributeParam = attributeParams.getJSONObject(j);
                String attributeName = attributeParam.getString("attributeName");
                String compareType = attributeParam.getString("compareType");
                String compareValue = attributeParam.getString("compareValue");
                String eventAttributeValue = userEvent.getProperties().get(attributeName);
                if (eventAttributeValue != null) {
                    if ("=".equals(compareType) && !(compareValue.compareTo(eventAttributeValue) == 0)) {
                        return false;
                    }
                    if (">".equals(compareType) && !(compareValue.compareTo(eventAttributeValue) > 0)) {
                        return false;
                    }
                    if ("<".equals(compareType) && !(compareValue.compareTo(eventAttributeValue) < 0)) {
                        return false;
                    }
                    if (">=".equals(compareType) && !(compareValue.compareTo(eventAttributeValue) >= 0)) {
                        return false;
                    }
                    if ("<=".equals(compareType) && !(compareValue.compareTo(eventAttributeValue) <= 0)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
