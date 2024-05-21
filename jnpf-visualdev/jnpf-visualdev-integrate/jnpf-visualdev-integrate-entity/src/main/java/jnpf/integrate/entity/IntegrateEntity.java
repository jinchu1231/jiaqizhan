package jnpf.integrate.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 *
 * @version V3.4.5
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 */
@Data
@TableName("base_integrate")
public class IntegrateEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 编码
     */
    @TableField("F_EN_CODE")
    private String enCode;

    /**
     * 模板
     */
    @TableField("F_TEMPLATE_JSON")
    private String templateJson;

    /**
     * 类型(1-事件，2-定时 )
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 开始表单id
     */
    @TableField("F_FORM_ID")
    private String formId;

    /**
     * 类型 (1.新增 2.修改 3.删除)
     */
    @TableField("F_TRIGGER_TYPE")
    private Integer triggerType;
}
