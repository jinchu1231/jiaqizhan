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
@TableName("flow_template")
public class FlowTemplateEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 流程编码
     */
    @TableField("F_EN_CODE")
    private String enCode;

    /**
     * 流程名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 流程类型(0.发起流程 1.功能流程)
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 流程分类
     */
    @TableField("F_CATEGORY")
    private String category;

    /**
     * 图标
     */
    @TableField("F_ICON")
    private String icon;

    /**
     * 图标背景色
     */
    @TableField("F_ICON_BACKGROUND")
    private String iconBackground;


}
