package cn.rtmk.engine.pojo;

import cn.rtmk.commom.interfaces.RuleConditionCaculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.roaringbitmap.RoaringBitmap;

/**
 * -- auto-generated definition
 * create table rule_instance_definition
 * (
 *     id                       int auto_increment
 *         primary key,
 *     rule_id                  varchar(50)  null,
 *     rule_model_id            int          null,
 *     rule_profile_user_bitmap longblob     null,
 *     caculator_groovy_code    text         null,
 *     rule_param_json          text         null,
 *     creator_name             varchar(255) null,
 *     rule_status              int          null,
 *     create_time              datetime     null,
 *     update_time              datetime     null
 * )
 *     charset = utf8;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleMetaBean {
    private String operateType; // insert, update, delete
    private String ruleId;
    private int ruleModelId;
    private RoaringBitmap profileUserBitmap;
    private String caculatorGroovyCode;
    private String ruleParamJson;
    private String creatorName;
    private int ruleStatus;
    private RuleConditionCaculator ruleConditionCaculator;
}
