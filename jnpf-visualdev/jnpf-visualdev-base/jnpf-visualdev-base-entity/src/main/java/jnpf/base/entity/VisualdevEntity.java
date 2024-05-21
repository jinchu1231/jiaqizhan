package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 *
 * 可视化开发功能表
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-02
 */
@Data
@TableName("base_visual_dev")
public class VisualdevEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {
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
     * 状态(0-暂存（默认），1-发布)
     */
    @TableField("F_STATE")
    private Integer state;

    /**
     * 类型(1-应用开发,2-移动开发,3-流程表单,4-Web表单,5-App表单)
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 关联的表
     */
    @TableField("F_TABLES_DATA")
    @JSONField(name = "tables")
    private String visualTables;

    /**
     * 分类（数据字典）
     */
    @TableField("F_CATEGORY")
    private String category;

    /**
     * 表单配置JSON
     */
    @TableField("F_FORM_DATA")
    private String formData;

    /**
     * 列表配置JSON
     */
    @TableField("F_COLUMN_DATA")
    private String columnData;

    /**
     * 关联数据连接id
     */
    @TableField("F_DB_LINK_ID")
    private String dbLinkId;

    /**
     * 页面类型（1、纯表单，2、表单加列表，3、表单列表工作流，4、数据视图）
     */
    @TableField("F_WEB_TYPE")
    private Integer webType;

    /**
     * 关联工作流连接id
     */
    @TableField("F_FLOW_ID")
    private String flowId;

    /**
     * app列表配置JSON
     */
    @TableField("F_APP_COLUMN_DATA")
    private String appColumnData;

    /**
     * 启用流程
     */
    @TableField("F_ENABLE_FLOW")
    private Integer enableFlow;

    /**
     * 接口id
     */
    @TableField("F_INTERFACE_ID")
    private String interfaceId;

    /**
     * 接口参数
     */
    @TableField("F_INTERFACE_PARAM")
    private String interfaceParam;
}
