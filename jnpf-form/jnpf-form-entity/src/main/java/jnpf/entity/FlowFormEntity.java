package jnpf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jnpf.base.entity.SuperExtendEntity;
import lombok.Data;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.4.2
 * @copyright 引迈信息技术有限公司
 * @date 2022/6/30 17:44
 */
@Data
@TableName("flow_form")
public class FlowFormEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    /**
     * 表单编码
     */
    @TableField("F_EN_CODE")
    private String enCode;

    /**
     * 表单状态
     */
    @TableField("F_STATE")
    private Integer state;

    /**flow_task
     * 表单名称
     */
    @TableField("F_FULL_NAME")
    private String fullName;

    /**
     * 流程类型：0-发起流程，1-功能流程
     */
    @TableField("F_FLOW_TYPE")
    private Integer flowType;
    /**
     * 表单类型：1-系统表单，2-自定义表单
     */
    @TableField("F_FORM_TYPE")
    private Integer formType;
    /**
     * 表单分类
     */
    @TableField("F_CATEGORY")
    private String category;
    /**
     * Web地址
     */
    @TableField("F_URL_ADDRESS")
    private String urlAddress;
    /**
     * APP地址
     */
    @TableField("F_APP_URL_ADDRESS")
    private String appUrlAddress;
    /**
     * 接口路径
     */
    @TableField("F_INTERFACE_URL")
    private String interfaceUrl;
    /**
     * 属性字段
     */
    @TableField("F_PROPERTY_JSON")
    private String propertyJson;

    /**
     * 草稿版本json
     */
    @TableField("F_DRAFT_JSON")
    private String draftJson;
    /**
     * 关联数据连接id
     */
    @TableField("F_DB_LINK_ID")
    private String dbLinkId;
    /**
     * 关联的表
     */
    @TableField("F_TABLE_JSON")
    private String tableJson;

    /**
     * 关联流程id
     */
    @TableField(value = "F_FLOW_ID",fill= FieldFill.UPDATE)
    private String flowId;

}

