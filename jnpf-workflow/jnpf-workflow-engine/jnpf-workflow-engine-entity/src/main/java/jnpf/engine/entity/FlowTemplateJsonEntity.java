package jnpf.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022年7月11日 上午9:18
 */
@Data
@TableName("flow_template_json")
public class FlowTemplateJsonEntity extends SuperExtendEntity.SuperExtendEnabledEntity<String> {

    /**
     * 流程模板id
     */
    @TableField("F_TEMPLATE_ID")
    private String templateId;

    /**
     * 流程名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 可见类型 0-全部可见、1-指定经办
     */
    @TableField("F_VISIBLE_TYPE")
    private Integer visibleType;

    /**
     * 流程模板
     */
    @TableField("F_FLOW_TEMPLATE_JSON")
    private String flowTemplateJson;

    /**
     * 流程版本
     */
    @TableField("F_VERSION")
    private String version;

    /**
     * 分组id
     */
    @TableField("F_GROUP_ID")
    private String groupId;

    /**
     * 发送配置
     */
    @TableField("F_SEND_CONFIG_IDS")
    private String sendConfigIds;

}
