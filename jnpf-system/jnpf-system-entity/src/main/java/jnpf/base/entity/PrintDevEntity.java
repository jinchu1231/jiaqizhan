package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 打印模板-实体类
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Data
@EqualsAndHashCode
@TableName("base_print_template")
public class PrintDevEntity extends SuperExtendEntity.SuperExtendDEEntity<String> {

    private static final long serialVersionUID = 1L;

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
     * 分类
     */
    @TableField("F_CATEGORY")
    private String category;

    /**
     * 类型(1-流程表单 2-功能表单)
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 连接数据 _id
     */
    @TableField("F_DB_LINK_ID")
    private String dbLinkId;

    /**
     * sql语句
     */
    @TableField("F_SQL_TEMPLATE")
    private String sqlTemplate;

    /**
     * 左侧字段
     */
    @TableField("F_LEFT_FIELDS")
    private String leftFields;

    /**
     * 打印模板
     */
    @TableField("F_PRINT_TEMPLATE")
    private String printTemplate;

    /**
     * 纸张参数
     */
    @TableField("F_PAGE_PARAM")
    private String pageParam;
}
